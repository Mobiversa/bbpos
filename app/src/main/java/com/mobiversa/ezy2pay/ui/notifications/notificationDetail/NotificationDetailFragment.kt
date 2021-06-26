package com.mobiversa.ezy2pay.ui.notifications.notificationDetail


import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment

import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.base.BaseFragment
import com.mobiversa.ezy2pay.network.response.NotificationList
import kotlinx.android.synthetic.main.fragment_notification_detail.view.*
import java.text.SimpleDateFormat
import android.view.*
import androidx.annotation.RequiresApi
import com.mobiversa.ezy2pay.MainActivity
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class NotificationDetailFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_notification_detail, container, false)
        val notificationData = arguments!!.getSerializable("Notification") as NotificationList.Response?

        setTitle("Notification", false)
        setHasOptionsMenu(true)
        (activity as MainActivity).supportActionBar?.title = "Notification"
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        rootView.notify_title_txt.text = notificationData?.msgTitle
        rootView.notify_desc_txt.text = notificationData?.msgDetails

        //Aug 5, 2019 6:49:06 PM
        var dateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.US)
        val date = dateFormat.parse(notificationData?.createdDate)

        dateFormat = SimpleDateFormat("dd MMM yyyy")
        val dateStr = dateFormat.format(date)
        rootView.notify_date_txt.text = dateStr
        dateFormat = SimpleDateFormat("hh:mm a")
        val timeStr = dateFormat.format(date)
        rootView.notify_time_txt.text = timeStr

        Log.e("Notification", " "+notificationData?.msgTitle)
        return rootView
    }

    override fun onResume() {
        super.onResume()
        setTitle("Notification", false)
        setHasOptionsMenu(true)
        (activity as MainActivity).supportActionBar?.title = "Notification"
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val activity = activity as? MainActivity
        return when (item.itemId) {
            android.R.id.home -> {
                (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
                fragmentManager?.popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
