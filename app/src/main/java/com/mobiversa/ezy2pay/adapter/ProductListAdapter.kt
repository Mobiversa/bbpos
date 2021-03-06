package com.mobiversa.ezy2pay.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobiversa.ezy2pay.databinding.ProductListDataItemBinding
import com.mobiversa.ezy2pay.network.response.ProductList

class ProductListAdapter(
    private val productList: List<ProductList>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ProductListViewHolder(ProductListDataItemBinding.inflate(layoutInflater, parent, false))
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ProductListViewHolder).bind(productList[position])
    }
}
