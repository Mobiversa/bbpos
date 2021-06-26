package com.mobiversa.ezy2pay.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobiversa.ezy2pay.databinding.NotificationListItemBinding
import com.mobiversa.ezy2pay.network.response.NotificationList
import com.mobiversa.ezy2pay.ui.notifications.NotificationsFragment
import kotlinx.android.synthetic.main.list_notification.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class NotificationListAdapter(
    val productList: ArrayList<NotificationList.Response>,
    private val context: Context,
    private val fragment: NotificationsFragment
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

//    var items = productList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return NotificationListViewHolder(NotificationListItemBinding.inflate(layoutInflater))
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as NotificationListViewHolder).bind(context,productList[position],fragment)
    }


    fun removeItem(position: Int) {
        productList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun restoreItem(item: NotificationList.Response, position: Int) {
        productList.add(position, item)
        notifyItemInserted(position)
    }

    fun getData(): ArrayList<NotificationList.Response> {
        return productList
    }
}

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class NotificationListViewHolder(private val binding: NotificationListItemBinding) : RecyclerView.ViewHolder(binding.root){

    @SuppressLint("SimpleDateFormat")
    fun bind(
        context: Context,
        notificationData: NotificationList.Response,
        fragment: NotificationsFragment
    ){
        binding.notificationItem = notificationData

        //Aug 5, 2019 6:49:06 PM
        var dateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.US)
        val date = dateFormat.parse(notificationData.createdDate )

        dateFormat = SimpleDateFormat("dd MMM yyyy")
        val dateStr = dateFormat.format(date)
        binding.root.txt_date.text = dateStr
        dateFormat = SimpleDateFormat("hh:mm a")
        val timeStr = dateFormat.format(date)
        binding.root.txt_time.text = timeStr

        binding.root.list_notify_relative.setOnClickListener {

            fragment.addFragment(notificationData, notificationData.msgRead)
            if (!notificationData.msgRead)
            notificationData.msgRead = true

        }
    }
}