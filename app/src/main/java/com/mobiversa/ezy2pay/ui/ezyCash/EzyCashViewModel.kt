package com.mobiversa.ezy2pay.ui.ezyCash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobiversa.ezy2pay.network.response.CallAckModel

class EzyCashViewModel : ViewModel() {
    private val service = EzyCashModel()
    var callAckData = MutableLiveData<CallAckModel>()

    fun requestCallAck(keyParams: HashMap<String, String>) {
        callAckData = service.getCallAck(keyParams)
    }

}
