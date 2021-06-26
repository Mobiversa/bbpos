package com.mobiversa.ezy2pay.ui.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobiversa.ezy2pay.network.response.MerchantDetailModel
import com.mobiversa.ezy2pay.network.response.ProfileProductList

class SettingsViewModel : ViewModel() {

    private val service = SettingsModel()
    var settingsModel = MutableLiveData<ProfileProductList>()
    var merchantDetailModel = MutableLiveData<MerchantDetailModel>()

    fun profileProductList(postParam: HashMap<String, String>) {
        settingsModel = service.getProfileProductList(postParam)
    }
    fun getMerchantDetail(postParam: HashMap<String, String>) {
        merchantDetailModel = service.getMerchantDetail(postParam)
    }

}

