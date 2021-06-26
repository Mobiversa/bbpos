package com.mobiversa.ezy2pay.ui.settings.profileProductList


import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
import kotlinx.android.synthetic.main.fragment_keyboard.view.*
import kotlinx.android.synthetic.main.fragment_profile_product_list.view.*

/**
 * A simple [Fragment] subclass.
 */
class KeyboardFragment : BaseFragment() {

    var bundle = Bundle()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_keyboard, container, false)

        //Basic Setup
        (activity as MainActivity).supportActionBar?.title = "Keyboard Settings"
        setTitle("Keyboard Settings", false)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        rootView.open_settings_btn.setOnClickListener {
            val enableIntent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
            enableIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            this.startActivity(enableIntent)
        }

        return rootView
    }

    override fun onResume() {
        super.onResume()
        //Basic Setup
        (activity as MainActivity).supportActionBar?.title = "Keyboard Settings"
        setTitle("Keyboard Settings", false)
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

}
