package com.mobiversa.ezy2pay.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.mobiversa.ezy2pay.databinding.ProfileProductListItemBinding
import com.mobiversa.ezy2pay.network.response.ProductList
import com.mobiversa.ezy2pay.ui.settings.profileProductList.ProfileProductListFragment
import com.mobiversa.ezy2pay.utils.Constants.Companion.ProductDisable

class ProfileProductListAdapter(
    private val productList: List<ProductList>,
    private val context: Context,
    private val homeFragment: ProfileProductListFragment
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ProfileProductListViewHolder(
            ProfileProductListItemBinding.inflate(
                layoutInflater,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ProfileProductListViewHolder).bind(productList[position], context, homeFragment)
    }
}

class ProfileProductListViewHolder(private val binding: ProfileProductListItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        productList: ProductList,
        context: Context?,
        profProdListFragment: ProfileProductListFragment?,
        listener: ProductListSelectionListener? = null
    ) {
        binding.productItem = productList

        binding.profProductLinear.setOnClickListener {
            if (productList.isEnable)
                profProdListFragment?.productDetails(productList.productName)
            else
                Toast.makeText(context, ProductDisable, Toast.LENGTH_SHORT).show()
        }
        listener?.let {
            binding.root.setOnClickListener {
                listener.onProductSelected(productList)
            }
        }
    }

    interface ProductListSelectionListener {
        fun onProductSelected(productList: ProductList)
    }
}
