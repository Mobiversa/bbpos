package com.mobiversa.ezy2pay.ui.settings

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.mobiversa.ezy2pay.network.ApiService
import com.mobiversa.ezy2pay.network.response.MerchantDetailModel
import com.mobiversa.ezy2pay.network.response.ProfileProductList
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingsModel {

    private val deliveryApiService = ApiService.serviceRequest()
    val data = MutableLiveData<ProfileProductList>()
    val merchantData = MutableLiveData<MerchantDetailModel>()

    fun getProfileProductList(postParam: HashMap<String, String>): MutableLiveData<ProfileProductList> {
        deliveryApiService.getProductList(postParam).enqueue(object :
            Callback<ProfileProductList> {
            override fun onFailure(call: Call<ProfileProductList>, t: Throwable) {
                productFailureResponse(data)
            }

            override fun onResponse(call: Call<ProfileProductList>, response: Response<ProfileProductList>) {
                if (response.isSuccessful) {
                    data.postValue(response.body())
                } else {
                    productFailureResponse(data)
                }
            }
        })
        return data
    }
    fun getMerchantDetail(postParam: HashMap<String, String>): MutableLiveData<MerchantDetailModel> {
        deliveryApiService.getMerchantDetails(postParam).enqueue(object :
            Callback<MerchantDetailModel> {
            override fun onFailure(call: Call<MerchantDetailModel>, t: Throwable) {
                merchantFailureResponse(merchantData)
            }

            override fun onResponse(call: Call<MerchantDetailModel>, response: Response<MerchantDetailModel>) {
                if (response.isSuccessful) {
                    merchantData.postValue(response.body())
                } else {
                    merchantFailureResponse(merchantData)
                }
            }
        })
        return merchantData
    }

    private fun productFailureResponse(data: MutableLiveData<ProfileProductList>) {
        Log.e("ProfileProductList Fail", ""+data)
    }

    private fun merchantFailureResponse(data: MutableLiveData<MerchantDetailModel>) {
        Log.e("MerchantDetail Fail", ""+data)
    }
}