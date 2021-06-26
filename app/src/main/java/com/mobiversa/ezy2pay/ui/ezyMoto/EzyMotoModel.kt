package com.mobiversa.ezy2pay.ui.ezyMoto

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.mobiversa.ezy2pay.network.ApiService
import com.mobiversa.ezy2pay.network.response.CountryList
import com.mobiversa.ezy2pay.network.response.EzyMotoRecPayment
import com.mobiversa.ezy2pay.network.response.SuccessModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EzyMotoModel {

    private val deliveryApiService = ApiService.serviceRequest()
    private val portalApiService = ApiService.externalRequest()
    val data = MutableLiveData<CountryList>()
    val payment = MutableLiveData<EzyMotoRecPayment>()
    val mobiLiteBalance = MutableLiveData<SuccessModel>()
    val upgradeData = MutableLiveData<SuccessModel>()

    fun countryList(postParam: HashMap<String, String>): MutableLiveData<CountryList> {
        deliveryApiService.getCountryList(postParam).enqueue(object :
            Callback<CountryList> {
            override fun onFailure(call: Call<CountryList>, t: Throwable) {
                notificationFailureResponse(data)
            }

            override fun onResponse(call: Call<CountryList>, response: Response<CountryList>) {
                if (response.isSuccessful) {
                    data.postValue(response.body())
                } else {
                    notificationFailureResponse(data)
                }
            }
        })
        return data
    }

    fun ezyMotoRecPayment(urlStr : String,postParam: HashMap<String, String>): MutableLiveData<EzyMotoRecPayment> {
        deliveryApiService.getEzyMotoRecPayment(urlStr,postParam).enqueue(object :
            Callback<EzyMotoRecPayment> {
            override fun onFailure(call: Call<EzyMotoRecPayment>, t: Throwable) {
                notificationFailureResponse(data)
            }

            override fun onResponse(call: Call<EzyMotoRecPayment>, response: Response<EzyMotoRecPayment>) {
                if (response.isSuccessful) {
                    payment.postValue(response.body())
                } else {
                    notificationFailureResponse(data)
                }
            }
        })
        return payment
    }

    fun ezyMotoPaymentCheck(postParam: HashMap<String, String>): MutableLiveData<SuccessModel> {
        deliveryApiService.geMobiLiteBalance(postParam).enqueue(object :
            Callback<SuccessModel> {
            override fun onFailure(call: Call<SuccessModel>, t: Throwable) {
                notificationFailureResponse(data)
            }

            override fun onResponse(call: Call<SuccessModel>, response: Response<SuccessModel>) {
                if (response.isSuccessful) {
                    mobiLiteBalance.postValue(response.body())
                } else {
                    notificationFailureResponse(data)
                }
            }
        })
        return mobiLiteBalance
    }

    fun upgradeEzymoto(postParam: HashMap<String, String>): MutableLiveData<SuccessModel> {
        portalApiService.upgradeUser(postParam).enqueue(object :
            Callback<SuccessModel> {
            override fun onFailure(call: Call<SuccessModel>, t: Throwable) {
                notificationFailureResponse(data)
            }

            override fun onResponse(call: Call<SuccessModel>, response: Response<SuccessModel>) {
                if (response.isSuccessful) {
                    upgradeData.postValue(response.body())
                } else {
                    notificationFailureResponse(data)
                }
            }
        })
        return upgradeData
    }

    private fun notificationFailureResponse(data: MutableLiveData<CountryList>) {
        Log.e("NotificationVM ", ""+data)
    }
}