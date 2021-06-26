package com.mobiversa.ezy2pay.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobiversa.ezy2pay.network.response.SuccessModel
import com.mobiversa.ezy2pay.network.response.TransactionHistoryModel
import com.mobiversa.ezy2pay.network.response.VoidHistoryModel

class HistoryViewModel : ViewModel() {

    private val service = HistoryModel()
    var transactionHistoryList = MediatorLiveData<TransactionHistoryModel>()
    var userVerification = MutableLiveData<SuccessModel>()
    var setVoidHistory = MutableLiveData<VoidHistoryModel>()
    var settlementData = MutableLiveData<SuccessModel>()
    var saleData = MutableLiveData<SuccessModel>()

    fun getTransactionHistory(historyParam : HashMap<String,String>){
        transactionHistoryList = service.getTransactionHistory(historyParam)
    }
    fun getUserVerification(historyParam : HashMap<String,String>){
        userVerification = service.getUserVerification(historyParam)
    }
    fun setVoidHistory(pathStr : String,historyParam : HashMap<String,String>){
        setVoidHistory = service.setVoidHistory(pathStr,historyParam)
    }
    fun getSettlement(historyParam : HashMap<String,String>){
        settlementData = service.getSettlement(historyParam)
    }
    fun setSale(saleParam : HashMap<String,String>){
        saleData = service.setSale(saleParam)
    }

    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text
}