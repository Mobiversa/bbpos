package com.mobiversa.ezy2pay.ui.history

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.gson.Gson
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.base.BaseActivity
import com.mobiversa.ezy2pay.network.response.Country
import com.mobiversa.ezy2pay.ui.ezyMoto.EzyMotoViewModel
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.Fields
import com.toptoche.searchablespinnerlibrary.SearchableSpinner


class CountryCodeActivity : BaseActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var motoViewModel: EzyMotoViewModel
    private val countryArray = ArrayList<String>()
    private lateinit var countryList: ArrayList<Country>
    private lateinit var countryAdapter: ArrayAdapter<String>
    private lateinit var countrySpinner: SearchableSpinner

    private lateinit var edtPhoneNumReceipt: EditText
    private lateinit var edtCountryCodeReceipt: EditText
    private var spinnerPosition = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_country_code)
        motoViewModel = ViewModelProviders.of(this).get(EzyMotoViewModel::class.java)

        countrySpinner = findViewById(R.id.spinner_country_receipt)
        edtPhoneNumReceipt = findViewById(R.id.edt_phone_num_receipt)
        edtCountryCodeReceipt = findViewById(R.id.edt_country_code_receipt)
        findViewById<AppCompatTextView>(R.id.btn_cancel_slip).setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
        findViewById<AppCompatTextView>(R.id.btn_receipt_slip).setOnClickListener {
            if (edtPhoneNumReceipt.text.isNotEmpty()) {
                val resultIntent = Intent()
                resultIntent.putExtra(
                    Constants.PHONE,
                    edtCountryCodeReceipt.text.toString() + edtPhoneNumReceipt.text.toString()
                )
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
        val productParams = HashMap<String, String>()
        productParams[Fields.Service] = Fields.CountryList
        jsonCountryList(productParams)

    }
    private fun jsonCountryList(countryParams: HashMap<String, String>) {
        showDialog("Processing...")
        motoViewModel.countryList(countryParams)
        motoViewModel.countryList.observe(this, Observer {

            if (it.responseCode.equals("0000", true)) {
                countryArray.clear()
                countryList = it.responseData.country
                for ((index, value) in it.responseData.country.withIndex()) {
                    countryArray.add(value.countryName)
                    if (value.countryName.equals("Malaysia", false))
                        spinnerPosition = index
                }
                val gson = Gson()
                val json = gson.toJson(it.responseData)
                putSharedString(Constants.CountryResponse, json)
                setUpCountrySpinner()

            } else {
                finish()
            }
            cancelDialog()
        })
    }
    private fun setUpCountrySpinner() {

        countryAdapter = ArrayAdapter(
            this@CountryCodeActivity,
            R.layout.support_simple_spinner_dropdown_item,
            countryArray
        )
        countrySpinner.adapter = countryAdapter
        countrySpinner.setTitle("Select Countries")
        countrySpinner.setPositiveButton("Done")
        countrySpinner.setSelection(spinnerPosition)

        countrySpinner.onItemSelectedListener = this
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    @SuppressLint("SetTextI18n")
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val spinner = parent as Spinner
        when (spinner.id) {
            R.id.spinner_country_receipt -> {
                edtCountryCodeReceipt.setText("" + countryList[position].phoneCode)
            }
        }
    }
}