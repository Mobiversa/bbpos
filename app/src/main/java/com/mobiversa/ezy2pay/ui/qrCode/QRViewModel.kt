package com.mobiversa.ezy2pay.ui.qrCode

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobiversa.ezy2pay.network.response.QRModel

class QRViewModel : ViewModel() {

    private val service = QRModel()
    var qrModel = MutableLiveData<QRModel>()

    fun boostQR(urlStr : String, postParam: HashMap<String, String>) {
        qrModel = service.qrJson(urlStr,postParam)
    }
}
