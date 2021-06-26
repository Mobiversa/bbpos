package com.mobiversa.ezy2pay.ui.loginActivity

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.mobiversa.ezy2pay.network.ApiService
import com.mobiversa.ezy2pay.network.response.*
import com.mobiversa.ezy2pay.utils.Fields
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginModel {
    private val deliveryApiService = ApiService.serviceRequest()
    private val registerApiService = ApiService.externalRequest()
    private val bankApiService = ApiService.bankRequest()
    private val stateApiService = ApiService.stateRequest()
    val data = MutableLiveData<LoginResponse>()
    val otpData = MutableLiveData<OTPResponse>()
    val userData = MutableLiveData<RegisterDetailResponse>()
    val verifyUserResponse = MutableLiveData<VerifyUserModel>()
    val successData = MutableLiveData<SuccessModel>()
    val merchantData = MutableLiveData<SuccessModel>()
    val updateData = MutableLiveData<SuccessModel>()
    val bankData = MutableLiveData<BankListModel>()
    val stateData = MutableLiveData<StateModel>()
    val cityData = MutableLiveData<StateModel>()

    fun login(postParam: HashMap<String, String>): MutableLiveData<LoginResponse> {
        deliveryApiService.login(postParam).enqueue(object : Callback<LoginResponse> {
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                loginFailedResponse(data)
            }
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    data.postValue(response.body())
                } else {
                    loginFailedResponse(data)
                }
            }
        })
        return data
    }
    fun getBankList(postParam: HashMap<String, String>): MutableLiveData<BankListModel> {
        bankApiService.getBankList(postParam).enqueue(object : Callback<BankListModel> {
            override fun onFailure(call: Call<BankListModel>, t: Throwable) {

            }
            override fun onResponse(call: Call<BankListModel>, response: Response<BankListModel>) {
                if (response.isSuccessful) {
                    bankData.postValue(response.body())
                } else {

                }
            }
        })
        return bankData
    }
    fun getStateList(postParam: HashMap<String, String>): MutableLiveData<StateModel> {
        stateApiService.getStateList(postParam).enqueue(object : Callback<StateModel> {
            override fun onFailure(call: Call<StateModel>, t: Throwable) {

            }
            override fun onResponse(call: Call<StateModel>, response: Response<StateModel>) {
                if (response.isSuccessful) {
                    if (postParam[Fields.StateOrCountry].equals(Fields.State))
                    stateData.postValue(response.body())
                    else {
                        cityData.postValue(response.body())
                    }
                }
            }
        })
        return if (postParam[Fields.StateOrCountry].equals(Fields.State))
            stateData
        else {
            cityData
        }
    }
    fun getOTP(postParam: HashMap<String, String>): MutableLiveData<OTPResponse> {
        deliveryApiService.getOTP(postParam).enqueue(object : Callback<OTPResponse> {
            override fun onFailure(call: Call<OTPResponse>, t: Throwable) {
                otpFailedResponse(otpData)
            }
            override fun onResponse(call: Call<OTPResponse>, response: Response<OTPResponse>) {
                if (response.isSuccessful) {
                    otpData.postValue(response.body())
                } else {
                    otpFailedResponse(otpData)
                }
            }
        })
        return otpData
    }
    fun sendUserDetails(postParam: HashMap<String, String>): MutableLiveData<RegisterDetailResponse> {
        deliveryApiService.sendUserDetail(postParam).enqueue(object : Callback<RegisterDetailResponse> {
            override fun onFailure(call: Call<RegisterDetailResponse>, t: Throwable) {
                userDetailFailedResponse(""+t.message)
            }
            override fun onResponse(call: Call<RegisterDetailResponse>, response: Response<RegisterDetailResponse>) {
                if (response.isSuccessful) {
                    userData.postValue(response.body())
                } else {
                    userDetailFailedResponse(""+ (response.body()?.responseDescription ?: ""))
                }
            }
        })
        return userData
    }
    fun verifyUserDetails(postParam: HashMap<String, String>): MutableLiveData<VerifyUserModel> {
        deliveryApiService.verifyUserDetail(postParam).enqueue(object : Callback<VerifyUserModel> {
            override fun onFailure(call: Call<VerifyUserModel>, t: Throwable) {
                userDetailFailedResponse(""+t.message)
            }
            override fun onResponse(call: Call<VerifyUserModel>, response: Response<VerifyUserModel>) {
                if (response.isSuccessful) {
                    verifyUserResponse.postValue(response.body())
                } else {
                    userDetailFailedResponse(""+ (response.body()?.responseDescription ?: ""))
                }
            }
        })
        return verifyUserResponse
    }
    fun registerMerchant(postParam: HashMap<String, String>): MutableLiveData<SuccessModel> {
        deliveryApiService.registerUser(postParam).enqueue(object : Callback<SuccessModel> {
            override fun onFailure(call: Call<SuccessModel>, t: Throwable) {
                userRegisterFailedResponse(successData)
            }
            override fun onResponse(call: Call<SuccessModel>, response: Response<SuccessModel>) {
                if (response.isSuccessful) {
                    successData.postValue(response.body())
                } else {
                    userRegisterFailedResponse(successData)
                }
            }
        })
        return successData
    }

    fun registerLiteUser(postParam: HashMap<String, String>): MutableLiveData<SuccessModel> {
        registerApiService.registerLiteUser(postParam).enqueue(object : Callback<SuccessModel> {
            override fun onFailure(call: Call<SuccessModel>, t: Throwable) {
                userRegisterFailedResponse(successData)
            }
            override fun onResponse(call: Call<SuccessModel>, response: Response<SuccessModel>) {
                if (response.isSuccessful) {
                    successData.postValue(response.body())
                } else {
                    userRegisterFailedResponse(successData)
                }
            }
        })
        return successData
    }

    fun updateMerchant(postParam: HashMap<String, String>): MutableLiveData<SuccessModel> {
        deliveryApiService.updateMerchant(postParam).enqueue(object : Callback<SuccessModel> {
            override fun onFailure(call: Call<SuccessModel>, t: Throwable) {
                userRegisterFailedResponse(updateData)
            }
            override fun onResponse(call: Call<SuccessModel>, response: Response<SuccessModel>) {
                if (response.isSuccessful) {
                    updateData.postValue(response.body())
                } else {
                    userRegisterFailedResponse(updateData)
                }
            }
        })
        return updateData
    }

    fun confirmMerchant(postParam: HashMap<String, String>): MutableLiveData<SuccessModel> {
        deliveryApiService.registerUser(postParam).enqueue(object : Callback<SuccessModel> {
            override fun onFailure(call: Call<SuccessModel>, t: Throwable) {
                userRegisterFailedResponse(merchantData)
            }

            override fun onResponse(call: Call<SuccessModel>, response: Response<SuccessModel>) {
                if (response.isSuccessful) {
                    merchantData.postValue(response.body())
                } else {
                    loginFailedResponse(data)
                }
            }
        })
        return merchantData
    }

    fun loginFailedResponse(data: MutableLiveData<LoginResponse>) {
        Log.e("LoginModel Fail", "" + data)
    }
    fun otpFailedResponse(data: MutableLiveData<OTPResponse>) {
        Log.e("LoginModel Fail", "" + data)
    }
    fun userDetailFailedResponse(data: String) {
        Log.e("LoginModel Fail", "" + data)
    }
    fun userRegisterFailedResponse(data: MutableLiveData<SuccessModel>) {
        Log.e("LoginModel Fail", "" + data)
    }

}