package com.dengtacj.recorder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.*
import com.dengtacj.recorder.view.DtWebView
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.system.exitProcess


/**
 * 安卓屏幕录制（视频+音频）
 * @see android.media.MediaRecorder.AudioSource.MIC 第三方app可用
 * @see android.media.MediaRecorder.AudioSource.REMOTE_SUBMIX 仅系统级应用才可以使用内录
 *
 * 某宝黑科技保真内录插头，可将音频信号直接输入到麦克风，所以音频源使用MediaRecorder.AudioSource.MIC
 *
 */
class MainActivity : AppCompatActivity(), ReceiverCallback {
    private var duration: Int = -1
    private var screenRecordHelper: ScreenRecordHelper? = null
    private lateinit var button: Button
    private lateinit var webView: DtWebView

    private val host = "http://192.168.11.109:8080"
//    private var url = "$host/#/Home"

    //    private var url = "$host/#/MainFund"
//    private var url = "$host/#/Forecast"
//    private var url = "$host/#/Template4"
//    private var url = "$host/#/Template5"
    private var url = "https://m.jd.com"
    val defaultExecutor: Executor = Executors.newCachedThreadPool()

    companion object {
        const val key_duration = "duration"
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        LogUtils.d("attachBaseContext")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ScreenUtils.setFullScreen(this)
//adb shell am start -n com.dengtacj.recorder/com.dengtacj.recorder.MainActivity -ei duration 5



//强制关闭
//adb shell am force-stop com.dengtacj.recorder
//启动页面
//adb shell am start -n com.dengtacj.recorder/com.dengtacj.recorder.MainActivity -d https://m.jd.com
//开始录制，毫秒
//adb shell am broadcast -a "com.dengtacj.recorder.start" --ei duration 8500
// 点击弹窗上的立即开始按钮
//adb shell input tap 790 2222
//手动停止录制
//adb shell am broadcast -a "com.dengtacj.recorder.stop"
//拷贝录屏视频
//adb pull /sdcard/Android/data/com.dengtacj.recorder/record.mp4  d:\record\xxxxxx.mp4
        val startTime = System.currentTimeMillis()
        setContentView(R.layout.activity_main)
        deleteCache()
        LogUtils.i("start cost ${System.currentTimeMillis() - startTime}")
        initData()
        initView()
        register()
        initPermission()
    }

    private fun deleteCache() {
        defaultExecutor.execute {
            val files = FileUtils.listFilesInDir(PathUtils.getExternalAppDataPath())
            files.forEach {
                if (it.name.contains("mp4")) {
                    val delete = FileUtils.delete(it)
                    LogUtils.v("fileName:${it.name} delete:$delete")
                }
            }
        }
    }

    private fun initData() {
        val dataString = intent.dataString
        LogUtils.d("onCreate() dataString = $dataString")
        if (dataString.isNullOrEmpty().not()) {
            url = dataString!!
        }
    }

    private fun initPermission() {
        PermissionUtils.permission(PermissionConstants.STORAGE, PermissionConstants.MICROPHONE)
            .callback(object : PermissionUtils.SimpleCallback {
                override fun onGranted() {

                }

                override fun onDenied() {

                }
            })
            .request()
    }

    private var receiver: RecordBroadcastReceiver? = null
    private fun register() {
        if (receiver == null) {
            receiver = RecordBroadcastReceiver()
            receiver!!.receiverCallback = this
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(action_start)
        intentFilter.addAction(action_stop)
        registerReceiver(receiver, intentFilter)
    }


    private fun initView() {
        button = findViewById(R.id.button)
        webView = findViewById(R.id.webView)
        webView.post {
            LogUtils.d("loadUrl: $url")
            webView.loadUrl(url)
        }

        button.setOnClickListener {
            val recording = screenRecordHelper?.isRecording

            if (recording == null || !recording) {
                startRecord(duration)
            } else {
                stopRecord()
            }
        }
    }

    private fun stopRecord() {
        handler.removeCallbacksAndMessages(null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            screenRecordHelper?.apply {
                if (isRecording) {
                    stopRecord()
                }
            }
        }
        resetButton()
    }

    val handler = Handler(Looper.getMainLooper())
    private fun startRecord(duration: Int) {
        button.text = "stop"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(Date())
//            val dir = SimpleDateFormat("yyyyMMdd", Locale.CHINA).format(Date())
            val name = "record"
            if (screenRecordHelper == null) {
                LogUtils.d("startRecord $name")
                screenRecordHelper =
                    ScreenRecordHelper(this, object : ScreenRecordHelper.OnVideoRecordListener {
                        override fun onBeforeRecord() {
                            button.isEnabled = false
                            LogUtils.d("onBeforeRecord() called")
                            handler.postDelayed({
//                                button.visibility = View.GONE
//                                moveTaskToBack(false)
                            }, 10)
                        }

                        override fun onStartRecord() {
                            button.isEnabled = true
                            LogUtils.d("onStartRecord() called")
                            if (duration > 0) {
                                handler.postDelayed({ stopRecord() }, duration * 1L)
                            }
                        }

                        override fun onCancelRecord() {
                            resetButton()
                            LogUtils.d("onCancelRecord() called")
                        }

                        override fun onEndRecord() {
                            resetButton()
                            LogUtils.d("onEndRecord() called")
                        }

                        override fun onRecordError(string: String) {
                            resetButton()
                        }

                    }, PathUtils.getExternalAppDataPath(), saveName = name)
            }
            screenRecordHelper?.apply {
                if (!isRecording) {
                    // 如果你想录制音频（一定会有环境音量），你可以打开下面这个限制,并且使用不带参数的 stopRecord()
                    recordAudio = true
                    saveName = name
                    startRecord()
                }
            }
        } else {
            ToastUtils.showShort("Android 5.1以下不支持录屏")
        }
    }

    private fun resetButton() {
        button.isEnabled = true
        button.text = "start"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && data != null) {
            screenRecordHelper?.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onResume() {
        super.onResume()
        LogUtils.d("onResume")
        webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        LogUtils.d("onPause")
//        mAgentWeb.webLifeCycle.onPause()
        webView.onPause()
    }

    override fun onDestroy() {
        release()
        super.onDestroy()
        unregister()
    }

    private fun release() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            screenRecordHelper?.clearAll()
        }
    }

    private fun unregister() {
        if (receiver != null) {
            unregisterReceiver(receiver)
        }
    }

    override fun onReceive(intent: Intent) {
        val action = intent.action
        if (action_start == action) {
            duration = intent.getIntExtra(key_duration, -1)
            LogUtils.i("duration:$duration")
            val recording = screenRecordHelper?.isRecording
            if (recording != null && recording) return
            startRecord(duration)
        } else if (action_stop == action) {
            stopRecord()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            exitProcess(0)
        }
    }

}

const val action_start = "com.dengtacj.recorder.start"
const val action_stop = "com.dengtacj.recorder.stop"

class RecordBroadcastReceiver :
    BroadcastReceiver() {

    var receiverCallback: ReceiverCallback? = null

    override fun onReceive(context: Context?, intent: Intent) {
        LogUtils.d("onReceive() $context, intent = ${intent.action}")
        intent.action.let { receiverCallback?.onReceive(intent) }
    }
}

interface ReceiverCallback {
    fun onReceive(intent: Intent)
}