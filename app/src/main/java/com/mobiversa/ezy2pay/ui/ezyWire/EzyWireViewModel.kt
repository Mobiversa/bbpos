package com.mobiversa.ezy2pay.ui.ezyWire

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobiversa.ezy2pay.network.response.*

class EzyWireViewModel : ViewModel() {

    private val service= EzyWireModel()
    var keyInjectResponse = MutableLiveData<KeyInjectModel>()
    var paymentInfoData = MutableLiveData<PaymentInfoModel>()
    var callAckData = MutableLiveData<CallAckModel>()
    var sendDeclineNotifyData = MutableLiveData<SendDeclineNotifyModel>()

    fun requestKeyInjection(keyParams : HashMap<String,String>) : LiveData<KeyInjectModel> {
        keyInjectResponse = service.getKeyInjection(keyParams)

        return keyInjectResponse
    }
    fun requestPaymentInfo(keyParams : HashMap<String,String>){
        paymentInfoData = service.getPaymentInfo(keyParams)
    }
    fun requestCallAck(keyParams : HashMap<String,String>){
        callAckData = service.getCallAck(keyParams)
    }
    fun requestSendNotifyDecline(keyParams : HashMap<String,String>){
        sendDeclineNotifyData = service.getSendNotifyDecline(keyParams)
    }
}