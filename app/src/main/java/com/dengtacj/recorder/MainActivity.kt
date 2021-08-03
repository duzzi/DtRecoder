package com.dengtacj.recorder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.*
import com.dengtacj.recorder.view.DtWebView
import java.text.SimpleDateFormat
import java.util.*
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

//    private var url = "http://192.168.11.144:8080/#/Home"
//    private var url = "http://192.168.11.144:8080/#/MainFund"
//    private var url = "http://192.168.11.144:8080/#/Forecast"
//    private var url = "http://192.168.11.144:8080/#/Template4"
//    private var url = "http://192.168.11.144:8080/#/Template5"
    private var url = "https://m.jd.com"

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

        //adb shell am start -n com.dengtacj.recorder/com.dengtacj.recorder.MainActivity -d xxxxxxxx
        //adb shell am start -n com.dengtacj.recorder/com.dengtacj.recorder.MainActivity -ei duration 5
        //adb shell am start -n com.dengtacj.recorder/com.dengtacj.recorder.MainActivity
        //adb shell am broadcast -a "com.dengtacj.recorder.start"
        //adb shell am broadcast -a "com.dengtacj.recorder.stop"
        //adb shell am force-stop com.dengtacj.recorder
        val startTime = System.currentTimeMillis()
        setContentView(R.layout.activity_main)
        LogUtils.i("start cost ${System.currentTimeMillis()-startTime}")
        initData()
        initView()
        register()
        initPermission()

    }

    private fun initData() {
        val dataString = intent.dataString
        LogUtils.d("onCreate() dataString = $dataString")
        if (dataString.isNullOrEmpty().not()) {
            url = dataString!!
        }
        duration = intent.getIntExtra("duration", -1)
//        if (duration > 0) {
//            startRecord(duration)
//        }
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
        webView.post{ webView.loadUrl(url) }


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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            screenRecordHelper?.apply {
                if (isRecording) {
                    stopRecord()
                }
            }
        }
        button.isEnabled = true
        button.text = "start"
    }

    val handler = Handler(Looper.getMainLooper())
    private fun startRecord(duration: Int) {
        button.text = "stop"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val name = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.CHINA).format(Date())
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
                                handler.postDelayed({ stopRecord() }, duration * 1000L)
                            }
                        }

                        override fun onCancelRecord() {
                            button.isEnabled = true
                            LogUtils.d("onCancelRecord() called")
                        }

                        override fun onEndRecord() {
                            button.isEnabled = true
                            button.visibility = View.VISIBLE
                            LogUtils.d("onEndRecord() called")
                        }

                    }, PathUtils.getInternalAppDataPath() + "/1_dengtacj", saveName = name)
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
            ToastUtils.showShort(R.string.phone_not_support_screen_record)
        }
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

    override fun onReceive(action: String?) {
        if (action_start == action) {
            val recording = screenRecordHelper?.isRecording
            if (recording != null && recording) return
            startRecord(duration)
        } else if (action_stop == action) {
            stopRecord()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        exitProcess(0)
    }

}

const val action_start = "com.dengtacj.recorder.start"
const val action_stop = "com.dengtacj.recorder.stop"

class RecordBroadcastReceiver :
    BroadcastReceiver() {

    var receiverCallback: ReceiverCallback? = null

    override fun onReceive(context: Context?, intent: Intent) {
        LogUtils.d("onReceive() $context, intent = ${intent.action}")
        intent.action.let { receiverCallback?.onReceive(it) }
    }
}

interface ReceiverCallback {
    fun onReceive(action: String?)
}