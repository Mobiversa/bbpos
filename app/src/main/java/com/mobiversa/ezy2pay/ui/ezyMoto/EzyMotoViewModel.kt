package com.mobiversa.ezy2pay.ui.ezyMoto

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobiversa.ezy2pay.network.response.CountryList
import com.mobiversa.ezy2pay.network.response.EzyMotoRecPayment
import com.mobiversa.ezy2pay.network.response.SuccessModel

class EzyMotoViewModel : ViewModel() {

    private val service = EzyMotoModel()
    var countryList = MutableLiveData<CountryList>()
    var ezyMotoRecPayment = MutableLiveData<EzyMotoRecPayment>()
    var ezyPayment = MutableLiveData<SuccessModel>()
    var upgradeData = MutableLiveData<SuccessModel>()

    fun countryList(postParam: HashMap<String, String>) {
        countryList = service.countryList(postParam)
    }
    fun ezyMotoRecPayment(urlStr : String,postParam: HashMap<String, String>) {
        ezyMotoRecPayment = service.ezyMotoRecPayment(urlStr,postParam)
    }
    fun ezyMotoPaymentCheck(postParam: HashMap<String, String>) {
        ezyPayment = service.ezyMotoPaymentCheck(postParam)
    }
    fun upgradeEzymoto(postParam: HashMap<String, String>) {
        upgradeData = service.upgradeEzymoto(postParam)
    }
}
