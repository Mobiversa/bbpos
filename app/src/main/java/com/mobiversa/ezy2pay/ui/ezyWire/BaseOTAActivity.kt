package com.mobiversa.ezy2pay.ui.ezyWire

import android.app.Dialog
import android.app.ProgressDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import com.bbpos.bbdevice.BBDeviceController
import com.bbpos.bbdevice.ota.BBDeviceOTAController
import com.bbpos.bbdevice.ota.BBDeviceOTAController.BBDeviceOTAControllerListener
import com.bbpos.bbdevice.ota.BBDeviceOTAController.OTAResult
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.base.BaseActivity
import java.util.*


open class BaseOTAActivity : BaseActivity() {
    private lateinit var bbDeviceController: BBDeviceController
    var listener: MyBBPosController? = null
    var progressCount = 0
    var pref: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (bbDeviceController.isSessionInitialized) {
            listener = MyBBPosController()
            otaListener = MyBBDeviceOTAControllerListener()
            bbDeviceController = BBDeviceController.getInstance(getApplicationContext(), listener)
            otaController = BBDeviceOTAController.getInstance( applicationContext, otaListener )
            BBDeviceController.setDebugLogEnabled(true)
            bbDeviceController.detectAudioDevicePlugged = true
        }
        pref = applicationContext.getSharedPreferences("Version", MODE_PRIVATE)
    }

    inner class MyBBDeviceOTAControllerListener :
        BBDeviceOTAControllerListener {
        override fun onReturnRemoteKeyInjectionResult(
            otaResult: OTAResult,
            message: String
        ) {
            dismissProgressDialog()
            var content = ""
            when (otaResult) {
                OTAResult.BATTERY_LOW_ERROR -> content += getString(R.string.remote_key_injection_result) + getString(
                    R.string.battery_low_error) + "\n"
                OTAResult.DEVICE_COMM_ERROR -> content += getString(R.string.remote_key_injection_result) + getString(
                    R.string.device_comm_error) + "\n"
                OTAResult.FAILED -> content += getString(R.string.remote_key_injection_result) + getString(
                    R.string.failed) + "\n"
                OTAResult.NO_UPDATE_REQUIRED -> content += getString(R.string.remote_key_injection_result) + getString(
                    R.string.no_update_required) + "\n"
                OTAResult.SERVER_COMM_ERROR -> content += getString(R.string.remote_key_injection_result) + getString(
                    R.string.server_comm_error) + "\n"
                OTAResult.SETUP_ERROR -> content += getString(R.string.remote_key_injection_result) + getString(
                    R.string.setup_error) + "\n"
                OTAResult.STOPPED -> content += getString(R.string.remote_key_injection_result) + getString(
                    R.string.stopped) + "\n"
                OTAResult.SUCCESS -> content += getString(R.string.remote_key_injection_result) + getString(
                    R.string.success) + "\n"
                else -> {
                    showLog("OTA ", "Error in OTA")
                }
            }
            content += getString(R.string.response_message).toString() + message
            showLog("OTA ", content)
        }

        override fun onReturnRemoteFirmwareUpdateResult(
            otaResult: OTAResult,
            message: String
        ) {
            dismissProgressDialog()
            var content = ""
            when (otaResult) {
                OTAResult.BATTERY_LOW_ERROR -> content += getString(R.string.remote_firmware_update_result) + getString(
                    R.string.battery_low_error) + "\n"
                OTAResult.DEVICE_COMM_ERROR -> content += getString(R.string.remote_firmware_update_result) + getString(
                    R.string.device_comm_error) + "\n"
                OTAResult.FAILED -> content += getString(R.string.remote_firmware_update_result) + getString(
                    R.string.failed) + "\n"
                OTAResult.NO_UPDATE_REQUIRED -> content += getString(R.string.remote_firmware_update_result) + getString(
                    R.string.no_update_required) + "\n"
                OTAResult.SERVER_COMM_ERROR -> content += getString(R.string.remote_firmware_update_result) + getString(
                    R.string.server_comm_error) + "\n"
                OTAResult.SETUP_ERROR -> content += getString(R.string.remote_firmware_update_result) + getString(
                    R.string.setup_error) + "\n"
                OTAResult.STOPPED -> content += getString(R.string.remote_firmware_update_result) + getString(
                    R.string.stopped) + "\n"
                OTAResult.SUCCESS -> content += getString(R.string.remote_firmware_update_result) + getString(
                    R.string.success) + "\n"
                else -> {}
            }
            content += getString(R.string.response_message) + message
            showLog("OTA ", content)
        }

        override fun onReturnRemoteConfigUpdateResult(
            otaResult: OTAResult,
            message: String
        ) {
            dismissProgressDialog()
            var content = ""
            when (otaResult) {
                OTAResult.BATTERY_LOW_ERROR -> content += getString(R.string.remote_config_update_result) + getString(
                    R.string.battery_low_error) + "\n"
                OTAResult.DEVICE_COMM_ERROR -> content += getString(R.string.remote_config_update_result) + getString(
                    R.string.device_comm_error) + "\n"
                OTAResult.FAILED -> content += getString(R.string.remote_config_update_result) + getString(
                    R.string.failed) + "\n"
                OTAResult.NO_UPDATE_REQUIRED -> content += getString(R.string.remote_config_update_result) + getString(
                    R.string.no_update_required) + "\n"
                OTAResult.SERVER_COMM_ERROR -> content += getString(R.string.remote_config_update_result) + getString(
                    R.string.server_comm_error) + "\n"
                OTAResult.SETUP_ERROR -> content += getString(R.string.remote_config_update_result) + getString(
                    R.string.setup_error) + "\n"
                OTAResult.STOPPED -> content += getString(R.string.remote_config_update_result) + getString(
                    R.string.stopped) + "\n"
                OTAResult.SUCCESS -> content += getString(R.string.remote_config_update_result) + getString(
                    R.string.success) + "\n"
                else -> {
                }
            }
            content += getString(R.string.response_message).toString() + message
            showLog("OTA ", content)
        }

        override fun onReturnLocalConfigUpdateResult( otaResult: OTAResult, message: String) {
            dismissProgressDialog()
            var content = ""
            when (otaResult) {
                OTAResult.BATTERY_LOW_ERROR -> content += getString(R.string.local_config_update_result) + getString(
                    R.string.battery_low_error) + "\n"
                OTAResult.DEVICE_COMM_ERROR -> content += getString(R.string.local_config_update_result) + getString(
                    R.string.device_comm_error) + "\n"
                OTAResult.FAILED -> content += getString(R.string.local_config_update_result) + getString(
                    R.string.failed) + "\n"
                OTAResult.NO_UPDATE_REQUIRED -> content += getString(R.string.local_config_update_result) + getString(
                    R.string.no_update_required) + "\n"
                OTAResult.SERVER_COMM_ERROR -> content += getString(R.string.local_config_update_result) + getString(
                    R.string.server_comm_error) + "\n"
                OTAResult.SETUP_ERROR -> content += getString(R.string.local_config_update_result) + getString(
                    R.string.setup_error) + "\n"
                OTAResult.STOPPED -> content += getString(R.string.local_config_update_result) + getString(
                    R.string.stopped) + "\n"
                OTAResult.SUCCESS -> content += getString(R.string.local_config_update_result) + getString(
                    R.string.success
                ) + "\n"
                else -> {
                }
            }
            content += getString(R.string.response_message) + message
            showLog("OTA ", content)
        }

        override fun onReturnLocalFirmwareUpdateResult(
            otaResult: OTAResult,
            message: String
        ) {
            dismissProgressDialog()
            var content = ""
            when (otaResult) {
                OTAResult.BATTERY_LOW_ERROR -> content += getString(R.string.local_firmware_update_result) + getString(
                    R.string.battery_low_error
                ) + "\n"
                OTAResult.DEVICE_COMM_ERROR -> content += getString(R.string.local_firmware_update_result) + getString(
                    R.string.device_comm_error) + "\n"
                OTAResult.FAILED -> content += getString(R.string.local_firmware_update_result) + getString(
                    R.string.failed) + "\n"
                OTAResult.NO_UPDATE_REQUIRED -> content += getString(R.string.local_firmware_update_result) + getString(
                    R.string.no_update_required
                ) + "\n"
                OTAResult.SERVER_COMM_ERROR -> content += getString(R.string.local_firmware_update_result) + getString(
                    R.string.server_comm_error) + "\n"
                OTAResult.SETUP_ERROR -> content += getString(R.string.local_firmware_update_result) + getString(
                    R.string.setup_error
                ) + "\n"
                OTAResult.STOPPED -> content += getString(R.string.local_firmware_update_result) + getString(
                    R.string.stopped
                ) + "\n"
                OTAResult.SUCCESS -> content += getString(R.string.local_firmware_update_result) + getString(
                    R.string.success
                ) + "\n"
                else -> showLog("OTA Error", content)
            }
            content += getString(R.string.response_message) + message
            showLog("OTA ", content)
        }

        override fun onReturnTargetVersionResult(
            otaResult: OTAResult,
            data: Hashtable<String, String>
        ) {
            dismissProgressDialog()
            var content: String? = ""
            if (data.isEmpty) {
                showLog("BaseOTA Activity", "OTA Empty")
            } else {
                showLog("OTA Firmware  ", "" + data)
                showLog("OTA Firmware  ", "" + data["firmwareVersion"])
                showLog("OTA deviceSettingVer  ", "" + data["deviceSettingVersion"])
                showLog("OTA terminalSetVer  ", "" + data["terminalSettingVersion"])
                val editor = pref!!.edit()
                editor.putString( "firmwareVersion_OTA", data["firmwareVersion"]) // Saving string
                editor.putString("deviceSettingVersion_OTA", data["deviceSettingVersion"])
                editor.apply()
            }
            val keys: Array<Any> = data.keys.toTypedArray()
            Arrays.sort(keys)
            for (key in keys) {
                content += "\n" + key as String + " : "
                val obj: Any? = data[key]
                if (obj is String) {
                    content += obj as String?
                } else if (obj is Boolean) {
                    content += obj.toString() + ""
                }
            }
            showLog("OTA ", ""+content)
        }

        override fun onReturnTargetVersionListResult(otaResult: OTAResult,data: List<Hashtable<String, String>>, message: String) {
            dismissProgressDialog()
            showLog("OTA ","otaResult : $otaResult, data : $data, message : $message")
        }

        override fun onReturnSetTargetVersionResult(otaResult: OTAResult,message: String) {
            dismissProgressDialog()
            showLog("OTA ", "otaResult : $otaResult, message : $message")
        }

        override fun onReturnOTAProgress(percentage: Double) {
            progressCount = percentage.toInt()
            showLog("Progress", " $percentage")
            if (progressDialog != null) {
                progressDialog!!.progress = percentage.toInt()
            }
        }
    }

    fun dismissProgressDialog() {
        progressCount = 0
        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }
    }

    companion object {
        var otaController: BBDeviceOTAController? = null
        var otaListener: MyBBDeviceOTAControllerListener? = null
        var dialog: Dialog? = null
        var progressDialog: ProgressDialog? = null
    }
}
