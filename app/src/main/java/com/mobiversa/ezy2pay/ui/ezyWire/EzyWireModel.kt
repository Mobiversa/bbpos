package com.mobiversa.ezy2pay.ui.ezyWire

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.mobiversa.ezy2pay.network.ApiService
import com.mobiversa.ezy2pay.network.response.CallAckModel
import com.mobiversa.ezy2pay.network.response.KeyInjectModel
import com.mobiversa.ezy2pay.network.response.PaymentInfoModel
import com.mobiversa.ezy2pay.network.response.SendDeclineNotifyModel
import retrofit2.Call
import retrofit2.Response

class EzyWireModel {
    private val deliverApiService = ApiService.serviceRequest()
    val keyInjectData = MutableLiveData<KeyInjectModel>()
    val paymentInfoData = MutableLiveData<PaymentInfoModel>()
    val callAckData = MutableLiveData<CallAckModel>()
    val sendDeclineNotifyData = MutableLiveData<SendDeclineNotifyModel>()

    fun getKeyInjection(keyParams: HashMap<String, String>): MutableLiveData<KeyInjectModel> {

        deliverApiService.run {
            getKeyInjection(keyParams).enqueue(object : retrofit2.Callback<KeyInjectModel> {
                override fun onFailure(call: Call<KeyInjectModel>, t: Throwable) {
                    keyInjectFailedResponse(keyInjectData)
                }

                override fun onResponse(
                    call: Call<KeyInjectModel>,
                    response: Response<KeyInjectModel>
                ) {
                    if (response.isSuccessful) {
                        keyInjectData.postValue(response.body())
                    } else {
                        keyInjectFailedResponse(keyInjectData)
                    }
                }
            })
        }

        return keyInjectData
    }
    fun getPaymentInfo(keyParams: HashMap<String, String>): MutableLiveData<PaymentInfoModel> {

        deliverApiService.run {
            getPaymentInfo(keyParams).enqueue(object : retrofit2.Callback<PaymentInfoModel> {
                override fun onFailure(call: Call<PaymentInfoModel>, t: Throwable) {
                    keyInjectFailedResponse(keyInjectData)
                }
                override fun onResponse(
                    call: Call<PaymentInfoModel>,
                    response: Response<PaymentInfoModel>
                ) {
                    if (response.isSuccessful) {
                        paymentInfoData.postValue(response.body())
                    } else {
                        PaymentInfoFailedResponse(paymentInfoData)
                    }
                }
            })
        }

        return paymentInfoData
    }

    fun getCallAck(keyParams: HashMap<String, String>): MutableLiveData<CallAckModel> {

        deliverApiService.run {
            getCallAckModel(keyParams).enqueue(object : retrofit2.Callback<CallAckModel> {
                override fun onFailure(call: Call<CallAckModel>, t: Throwable) {
                    keyInjectFailedResponse(keyInjectData)
                }
                override fun onResponse(
                    call: Call<CallAckModel>,
                    response: Response<CallAckModel>
                ) {
                    if (response.isSuccessful) {
                        callAckData.postValue(response.body())
                    } else {
                        CallAckFailedResponse(callAckData)
                    }
                }
            })
        }

        return callAckData
    }

    fun getSendNotifyDecline(keyParams: HashMap<String, String>): MutableLiveData<SendDeclineNotifyModel> {

        deliverApiService.run {
            getDeclineNotifyData(keyParams).enqueue(object : retrofit2.Callback<SendDeclineNotifyModel> {
                override fun onFailure(call: Call<SendDeclineNotifyModel>, t: Throwable) {
                    keyInjectFailedResponse(keyInjectData)
                }
                override fun onResponse(
                    call: Call<SendDeclineNotifyModel>,
                    response: Response<SendDeclineNotifyModel>
                ) {
                    if (response.isSuccessful) {
                        sendDeclineNotifyData.postValue(response.body())
                    } else {
                        sendNotifyDeclineFailedResponse(sendDeclineNotifyData)
                    }
                }
            })
        }

        return sendDeclineNotifyData
    }

    fun keyInjectFailedResponse(data: MutableLiveData<KeyInjectModel>) {
        Log.e("EzywireModel ", "" + data)
    }

    fun PaymentInfoFailedResponse(data: MutableLiveData<PaymentInfoModel>) {
        Log.e("PaymentInfoModel ", "" + data)
    }

    fun CallAckFailedResponse(data: MutableLiveData<CallAckModel>) {
        Log.e("CallAckModel ", "" + data)
    }

    fun sendNotifyDeclineFailedResponse(data: MutableLiveData<SendDeclineNotifyModel>) {
        Log.e("NotifyDeclineModel ", "" + data)
    }
}