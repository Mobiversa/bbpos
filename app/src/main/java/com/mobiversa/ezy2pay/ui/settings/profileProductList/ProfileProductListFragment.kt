package com.mobiversa.ezy2pay.ui.settings.profileProductList


import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobiversa.ezy2pay.MainActivity
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.adapter.ProfileProductListAdapter
import com.mobiversa.ezy2pay.base.BaseFragment
import com.mobiversa.ezy2pay.ui.settings.SettingsFragment
import com.mobiversa.ezy2pay.ui.settings.productDetail.ProfileProductDetailFragment
import com.mobiversa.ezy2pay.utils.Constants.Companion.Product
import kotlinx.android.synthetic.main.fragment_profile_product_list.view.*

/**
 * A simple [Fragment] subclass.
 */
class ProfileProductListFragment : BaseFragment() {

    var bundle = Bundle()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_profile_product_list, container, false)

        //Basic Setup
        (activity as MainActivity).supportActionBar?.title = "Product Details"
        setTitle("Product Details", false)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        initialize(rootView)

        return rootView
    }

    private fun initialize(rootView: View){

        val productList = getProductList()
        rootView.profile_product_recycler.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        val productListAdapter = ProfileProductListAdapter(productList,context!!,this)
        rootView.profile_product_recycler.adapter = productListAdapter

    }

    override fun onResume() {
        super.onResume()
        //Basic Setup
        (activity as MainActivity).supportActionBar?.title = "Product Details"
        setTitle("Product Details", false)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val activity = activity as? MainActivity
        return when (item.itemId) {
            android.R.id.home -> {
                val fragment = SettingsFragment()
                addFragment(fragment,bundle, Product)
                setTitle("Profile", true)
//                (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
//                fragmentManager?.popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun productDetails(productName : String){

        bundle.clear()
        bundle.putString(Product, productName)
        val fragment = ProfileProductDetailFragment()
        addFragment(fragment,bundle, Product)

    }

}
