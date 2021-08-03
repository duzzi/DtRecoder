package com.dengtacj.recorder

import android.app.Application
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import com.tencent.smtt.export.external.TbsCoreSettings
import com.tencent.smtt.sdk.QbSdk
import com.tencent.smtt.sdk.TbsListener
import java.util.*


class App:Application(), QbSdk.PreInitCallback, TbsListener {

    override fun onCreate() {
        val startTime = System.currentTimeMillis()
        Utils.init(this)
        LogUtils.getConfig().globalTag = "dt_record"
        // 在调用TBS初始化、创建WebView之前进行如下配置
        val map: HashMap<String, Any> = HashMap()
        map[TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER] = true
        map[TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE] = true
        QbSdk.initTbsSettings(map)
        QbSdk.initX5Environment(this,this)
        QbSdk.setTbsListener(this)
        LogUtils.i("start cost ${System.currentTimeMillis()-startTime}")
        super.onCreate()
    }

    override fun onCoreInitFinished() {
        LogUtils.i("onCoreInitFinished")
    }

    override fun onViewInitFinished(p0: Boolean) {
        LogUtils.i("onViewInitFinished$p0 ")
    }

    override fun onDownloadFinish(p0: Int) {
        LogUtils.i("onCoreInitFinished $p0")
    }

    override fun onInstallFinish(p0: Int) {
        LogUtils.i("onInstallFinish $p0")
    }

    override fun onDownloadProgress(p0: Int) {
        LogUtils.i("onDownloadProgress $p0")
    }
}