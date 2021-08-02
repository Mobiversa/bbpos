package com.mobiversa.ezy2pay.ui.bbpos

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.mobiversa.ezy2pay.MainActivity
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.base.BaseFragment
import com.mobiversa.ezy2pay.databinding.FragmentBbPinEntryBinding
import com.mobiversa.ezy2pay.utils.Fields

class PinEntryFragment : BaseFragment() {
    private var bundle = Bundle()
    private var amountToBePaid: String = ""
    private var invoiceId: String = ""
    private lateinit var binding: FragmentBbPinEntryBinding
    private var pintDetails: ArrayList<String> = ArrayList()
//    private lateinit var pinAdapter: PinEntryAdapter
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
        binding = FragmentBbPinEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.clearTxt.setOnClickListener { onInputClick(it) }
        binding.deleteTxt.setOnClickListener { onInputClick(it) }
        binding.oneTxt.setOnClickListener { onInputClick(it) }
        binding.twoTxt.setOnClickListener { onInputClick(it) }
        binding.threeTxt.setOnClickListener { onInputClick(it) }
        binding.fourTxt.setOnClickListener { onInputClick(it) }
        binding.fiveTxt.setOnClickListener { onInputClick(it) }
        binding.sixTxt.setOnClickListener { onInputClick(it) }
        binding.sevenTxt.setOnClickListener { onInputClick(it) }
        binding.eightTxt.setOnClickListener { onInputClick(it) }
        binding.nineTxt.setOnClickListener { onInputClick(it) }
        binding.zeroTxt.setOnClickListener { onInputClick(it) }
        binding.continuePayment.setOnClickListener {
            sendPinEntry()
        }
//        pinAdapter = PinEntryAdapter(pintDetails)
//        binding.pinEntryContainer.adapter = pinAdapter
    }
    private fun sendPinEntry() {
    }
    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun onInputClick(view: View?) {
        if (view != null) {
            if (view.id == R.id.clear_txt) {
                pintDetails.clear()
            } else {
                val pressedVal = (view as TextView).text.toString()
                if (view.id == R.id.delete_txt) {
                    pintDetails.removeAt(pintDetails.size - 1)
                } else {
                    pintDetails.add(pressedVal)
                }
            }
        }
//        pinAdapter.notifyDataSetChanged()
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
}
