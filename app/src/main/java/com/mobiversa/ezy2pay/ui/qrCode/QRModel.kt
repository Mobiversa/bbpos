package com.mobiversa.ezy2pay.ui.qrCode

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.mobiversa.ezy2pay.network.ApiService
import com.mobiversa.ezy2pay.network.response.QRModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class QRModel {

    private val deliveryApiService = ApiService.serviceRequest()
    val data = MutableLiveData<QRModel>()

    fun qrJson(urlStr : String, postParam: HashMap<String, String>): MutableLiveData<QRModel> {
        deliveryApiService.getQRJson(urlStr,postParam).enqueue(object :
            Callback<QRModel> {
            override fun onFailure(call: Call<QRModel>, t: Throwable) {
                qrFailureResponse(data)
            }

            override fun onResponse(call: Call<QRModel>, response: Response<QRModel>) {
                if (response.isSuccessful) {
                    data.postValue(response.body())
                } else {
                    qrFailureResponse(data)
                }
            }
        })
        return data
    }

    private fun qrFailureResponse(data: MutableLiveData<QRModel>) {
        Log.e("QRModel Failure ", ""+data)
    }
}