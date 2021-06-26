package com.mobiversa.ezy2pay.ui.receipt

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.mobiversa.ezy2pay.network.ApiService
import com.mobiversa.ezy2pay.network.response.ReceiptModel
import retrofit2.Call
import retrofit2.Response

class PrintReceiptModel {
    private val deliverApiService = ApiService.serviceRequest()
    val printReceiptData = MutableLiveData<ReceiptModel>()

    fun getPrintReceipt(keyParams: HashMap<String, String>): MutableLiveData<ReceiptModel> {

        deliverApiService.run {
            getReceiptData(keyParams).enqueue(object : retrofit2.Callback<ReceiptModel> {
                override fun onFailure(call: Call<ReceiptModel>, t: Throwable) {
                    receiptFailedResponse(printReceiptData)
                }
                override fun onResponse(
                    call: Call<ReceiptModel>,
                    response: Response<ReceiptModel>
                ) {
                    if (response.isSuccessful) {
                        printReceiptData.postValue(response.body())
                    } else {
                        receiptFailedResponse(printReceiptData)
                    }
                }
            })
        }

        return printReceiptData
    }
    fun receiptFailedResponse(data: MutableLiveData<ReceiptModel>) {
        Log.e("ReceiptModel ", "" + data)
    }
}