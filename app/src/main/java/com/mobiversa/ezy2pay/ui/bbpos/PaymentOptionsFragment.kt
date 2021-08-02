package com.mobiversa.ezy2pay.ui.bbpos


import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.mobiversa.ezy2pay.MainActivity
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.adapter.ProfileProductListViewHolder
import com.mobiversa.ezy2pay.base.BaseFragment
import com.mobiversa.ezy2pay.network.response.ProductList
import com.mobiversa.ezy2pay.ui.ezyWire.EzyWireActivity
import com.mobiversa.ezy2pay.ui.qrCode.QRFragment
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.Fields

/**
 * A fragment representing a list of Items.
 */
class PaymentOptionsFragment : BaseFragment() {
    private var bundle = Bundle()
    private var amountToBePaid: String = ""
    private var invoiceId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            amountToBePaid = it.getString(Fields.Amount) ?: ""
            invoiceId = it.getString(Fields.InvoiceId) ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment_options_list, container, false)
        val paymentMethodList = view.findViewById<RecyclerView>(R.id.payment_method_list)
        // Set the adapter
        with(paymentMethodList) {
            adapter = PaymentOptionsListAdapter(
                getProductList().filter { it.isEnable },
                object :
                    ProfileProductListViewHolder.ProductListSelectionListener {
                    override fun onProductSelected(productList: ProductList) {
                        productDetails(productList.productName)
                    }
                }
            )
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        setTitle("Mobi", false)
        setHasOptionsMenu(true)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                fragmentManager?.popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    fun productDetails(productName: String) {
        bundle.clear()
        bundle.putString(Fields.Amount, amountToBePaid)
        bundle.putString(Fields.InvoiceId, invoiceId)
        when (productName) {
            Constants.Ezywire -> {
                if (amountToBePaid.toDouble() < 5) {
                    shortToast("Enter Amount more than 5 RM")
                    return
                } else {
                    startActivity(
                        Intent(context, EzyWireActivity::class.java).apply {
                            putExtra(Fields.Service, Fields.START_PAY)
                            putExtra(Fields.Amount, amountToBePaid)
                            putExtra(Fields.InvoiceId, invoiceId)
                        }
                    )
                }
            }
            Constants.Boost -> {
                val fragment = QRFragment()
                bundle.putString(Fields.Service, Fields.BoostQR)
                addFragment(fragment, bundle, Constants.Boost)
            }
            Constants.GrabPay -> {
                val fragment = QRFragment()
                bundle.putString(Fields.Service, Fields.GPayQR)
                addFragment(fragment, bundle, Constants.GrabPay)
            }
        }
    }
}