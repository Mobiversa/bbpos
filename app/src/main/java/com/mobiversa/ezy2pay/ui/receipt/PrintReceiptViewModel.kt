package com.mobiversa.ezy2pay.ui.receipt

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobiversa.ezy2pay.network.response.ReceiptModel

class PrintReceiptViewModel : ViewModel() {
    private val service = PrintReceiptModel()
    var printReceiptData = MutableLiveData<ReceiptModel>()

    fun getReceipt(receiptParam : HashMap<String,String>){
        printReceiptData = service.getPrintReceipt(receiptParam)
    }
}
