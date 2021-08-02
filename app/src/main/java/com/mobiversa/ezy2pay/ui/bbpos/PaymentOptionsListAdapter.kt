package com.mobiversa.ezy2pay.ui.bbpos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobiversa.ezy2pay.adapter.ProfileProductListViewHolder
import com.mobiversa.ezy2pay.databinding.ProfileProductListItemBinding
import com.mobiversa.ezy2pay.network.response.ProductList

class PaymentOptionsListAdapter(
    private val productList: List<ProductList>,
    private val listener: ProfileProductListViewHolder.ProductListSelectionListener
) : RecyclerView.Adapter<ProfileProductListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileProductListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ProfileProductListViewHolder(
            ProfileProductListItemBinding.inflate(
                layoutInflater,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ProfileProductListViewHolder, position: Int) {
        holder.bind(productList[position], null, null, listener)
    }

    override fun getItemCount(): Int = productList.size
}