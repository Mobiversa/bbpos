package com.mobiversa.ezy2pay.ui.loginActivity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobiversa.ezy2pay.network.ApiService
import com.mobiversa.ezy2pay.network.response.*
import com.mobiversa.ezy2pay.utils.Fields

class LoginViewModel : ViewModel() {
    private val service = LoginModel()
    var loginResponse = MutableLiveData<LoginResponse>()
    var otpResponse = MutableLiveData<OTPResponse>()
    var userDetailResponse = MutableLiveData<RegisterDetailResponse>()
    var verifyUserResponse = MutableLiveData<VerifyUserModel>()
    var successResponse = MutableLiveData<SuccessModel>()
    var updateResponse = MutableLiveData<SuccessModel>()
    var stateResponse = MutableLiveData<StateModel>()
    var cityResponse = MutableLiveData<StateModel>()
    var confirmMerchantResponse = MutableLiveData<SuccessModel>()
    var bankData = MutableLiveData<BankListModel>()


    fun requestLogin(postParam: HashMap<String, String>) {
        loginResponse = service.login(postParam)
    }
    fun getOTP(postParam: HashMap<String, String>) {
        otpResponse = service.getOTP(postParam)
    }
    fun setUserDetails(postParam: HashMap<String, String>) {
        userDetailResponse = service.sendUserDetails(postParam)
    }
    fun checkUserType(postParam: HashMap<String, String>) {
        verifyUserResponse = service.verifyUserDetails(postParam)
    }
    fun registerUser(postParam: HashMap<String, String>) {
        successResponse = service.registerMerchant(postParam)
    }
    fun registerLiteUser(postParam: HashMap<String, String>) {
        successResponse = service.registerLiteUser(postParam)
    }
    fun updateMerchant(postParam: HashMap<String, String>) {
        updateResponse = service.updateMerchant(postParam)
    }

    fun confirmMerchant(postParam: HashMap<String, String>) {
        confirmMerchantResponse = service.confirmMerchant(postParam)
    }

    fun getBankList(postParam: HashMap<String, String>) {
        bankData = service.getBankList(postParam)
    }

    fun getStateList(postParam: HashMap<String, String>) {
        if (postParam[Fields.StateOrCountry].equals(Fields.State)) {
            stateResponse = service.getStateList(postParam)
        }else{
            cityResponse = service.getStateList(postParam)
        }
    }

}