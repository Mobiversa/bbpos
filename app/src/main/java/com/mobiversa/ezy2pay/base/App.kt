package com.mobiversa.ezy2pay.base

import android.app.Application
import org.acra.ACRA
import org.acra.ReportField
import org.acra.ReportingInteractionMode
import org.acra.annotation.ReportsCrashes

@ReportsCrashes(
    mailTo = "sampath.chinnaraj@gmail.com",
    customReportContent = [ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME, ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL, ReportField.CUSTOM_DATA, ReportField.STACK_TRACE, ReportField.LOGCAT],
    mode = ReportingInteractionMode.SILENT
)
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        // The following line triggers the initialization of ACRA
        ACRA.init(this)
    }

    companion object {
        lateinit var instance: App
            private set
    }
}