package com.mobiversa.ezy2pay.ui.settings.productDetail


import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.mobiversa.ezy2pay.MainActivity

import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.base.BaseFragment
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.Constants.Companion.EzyMoto
import com.mobiversa.ezy2pay.utils.Constants.Companion.Product
import kotlinx.android.synthetic.main.fragment_profile_product_detail.view.*

/**
 * A simple [Fragment] subclass.
 */
class ProfileProductDetailFragment : BaseFragment() {

    var deviceId = ""
    var tidStr = ""
    var ezyAuthEnabled = ""
    var expiryDateStr = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_profile_product_detail, container, false)

        //Basic Setup
        (activity as MainActivity).supportActionBar?.title = "Product Details"
        setTitle("Product Detail", false)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        initialize( arguments!!.getString(Product),rootView)
        return rootView
    }

    @SuppressLint("SetTextI18n")
    private fun initialize(productName: String?, rootView: View){

        when(productName){
            Constants.Ezywire -> {
                rootView.prof_product_img.setImageResource(getProductList()[0].productImage)
                rootView.txt_prod_name.text = getProductList()[0].productName
                rootView.prod_desc.text = "EZYWIRE is a Mobile Point of Sale \n" +
                        "Terminal for merchants who \n" +
                        "want accept card payments."
                deviceId = getProductResponse().deviceId
                tidStr = getProductResponse().tid
                ezyAuthEnabled = getProductResponse().enableEzywire
                expiryDateStr = getProductResponse().deviceExpiry
            }
            Constants.Boost -> {
                rootView.prof_product_img.setImageResource(R.drawable.boost)
                rootView.txt_prod_name.text = getProductList()[1].productName
                rootView.prod_desc.text = ""
                deviceId = getProductResponse().mid
                tidStr = getProductResponse().tid
                ezyAuthEnabled = "YES"
                expiryDateStr = getProductResponse().deviceExpiry
            }
            Constants.GrabPay -> {
                rootView.prof_product_img.setImageResource(getProductList()[2].productImage)
                rootView.txt_prod_name.text = getProductList()[2].productName
                rootView.prod_desc.text = ""
                deviceId = getProductResponse().mid
                tidStr = getProductResponse().tid
                ezyAuthEnabled = "YES"
                expiryDateStr = getProductResponse().deviceExpiry
            }
        }

        rootView.txt_prod_detail_device_id.text = deviceId
        rootView.txt_prod_detail_tid.text = tidStr
        rootView.txt_prod_detail_ezyauth_enabled.text = ezyAuthEnabled
        rootView.txt_prod_detail_expiry_date.text = expiryDateStr

    }

    override fun onResume() {
        super.onResume()
        //Basic Setup
        (activity as MainActivity).supportActionBar?.title = "Product Details"
        setTitle("Product Detail", false)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val activity = activity as? MainActivity
        return when (item.itemId) {
            android.R.id.home -> {
                setTitle("Product Details", true)
                (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
                fragmentManager?.popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
