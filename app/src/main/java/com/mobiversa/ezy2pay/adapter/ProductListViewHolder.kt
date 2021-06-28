package com.mobiversa.ezy2pay.adapter

import androidx.recyclerview.widget.RecyclerView
import com.mobiversa.ezy2pay.databinding.ProductListDataItemBinding
import com.mobiversa.ezy2pay.network.response.ProductList

class ProductListViewHolder(private val binding: ProductListDataItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        productList: ProductList
    ) {
        binding.productItem = productList

        binding.listProdImg.setOnClickListener {
            // Listener
        }
        binding.listProdName.setOnClickListener {
            // Listener
        }
    }
}
