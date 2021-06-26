package com.mobiversa.ezy2pay.ui.ezyCash

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.mobiversa.ezy2pay.network.ApiService
import com.mobiversa.ezy2pay.network.response.CallAckModel
import retrofit2.Call
import retrofit2.Response

class EzyCashModel {
    private val deliverApiService = ApiService.serviceRequest()
    val callAckData = MutableLiveData<CallAckModel>()

    fun getCallAck(keyParams: HashMap<String, String>): MutableLiveData<CallAckModel> {

        deliverApiService.run {
            getCallAckModel(keyParams).enqueue(object : retrofit2.Callback<CallAckModel> {
                override fun onFailure(call: Call<CallAckModel>, t: Throwable) {
                    CallAckFailedResponse(callAckData)
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
    fun CallAckFailedResponse(data: MutableLiveData<CallAckModel>) {
        Log.e("CallAckModel ", "" + data)
    }
}