package com.mobiversa.ezy2pay.adapter

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.mobiversa.ezy2pay.databinding.ProductListDataItemBinding
import com.mobiversa.ezy2pay.network.response.ProductList
import com.mobiversa.ezy2pay.ui.home.HomeFragment


class ProductListViewHolder(private val binding: ProductListDataItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        productList: ProductList,
        context: Context,
        homeFragment: HomeFragment
    ) {
        binding.productItem = productList

        binding.listProdImg.setOnClickListener {
            Log.e("List ", productList.productName)
            if (productList.isEnable)
                homeFragment.productDetails(productList.productName)
            else
                Toast.makeText(context,"You are not Subscribed for ${productList.productName}",Toast.LENGTH_SHORT).show()
        }
        binding.listProdName.setOnClickListener {
            Log.e("List ", productList.productName)
            if (productList.isEnable)
                homeFragment.productDetails(productList.productName)
            else
                Toast.makeText(context,"You are not Subscribed for ${productList.productName}",Toast.LENGTH_SHORT).show()
        }
    }
}