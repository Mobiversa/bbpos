package com.mobiversa.ezy2pay.ui.ezyWire


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.mobiversa.ezy2pay.R

/**
 * A simple [Fragment] subclass.
 */
class SignatureFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_signature, container, false)

        return rootView
    }


}
