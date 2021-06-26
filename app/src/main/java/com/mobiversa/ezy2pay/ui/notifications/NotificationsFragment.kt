package com.mobiversa.ezy2pay.ui.notifications

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.view.menu.MenuBuilder
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.adapter.NotificationListAdapter
import com.mobiversa.ezy2pay.base.BaseFragment
import com.mobiversa.ezy2pay.network.response.NotificationList
import com.mobiversa.ezy2pay.ui.notifications.notificationDetail.NotificationDetailFragment
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.Fields
import com.mobiversa.ezy2pay.utils.Fields.Companion.Service
import com.mobiversa.ezy2pay.utils.Fields.Companion.muid
import com.mobiversa.ezy2pay.utils.MarginItemDecoration
import kotlinx.android.synthetic.main.fragment_notifications.*
import kotlinx.android.synthetic.main.fragment_notifications.view.*

class NotificationsFragment : BaseFragment() {

    private lateinit var notificationsViewModel: NotificationsViewModel
    var notificationList = ArrayList<NotificationList.Response>()
    lateinit var notificationListAdapter: NotificationListAdapter
    private lateinit var notifyRecyclerView: RecyclerView
    var sessionId = ""
    var muId = ""

    //Fragment Navigation
    private val notificationDetailFragment = NotificationDetailFragment()
    val bundle = Bundle()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        notificationsViewModel =
            ViewModelProviders.of(this).get(NotificationsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_notifications, container, false)
        notifyRecyclerView = root.rcy_notification_list
//        val textView: TextView = root.findViewById(R.id.text_notifications)
        setTitle("Notification", true)
        setHasOptionsMenu(true)

        notifyRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
//        notifyRecyclerView.addItemDecoration( DividerItemDecoration( context, LinearLayoutManager.VERTICAL ))
        notifyRecyclerView.addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.xxhdpi_5).toInt())
        )
        notificationListAdapter = NotificationListAdapter(notificationList, context!!, this)
        notifyRecyclerView.adapter = notificationListAdapter

        val loginResponse = getLoginResponse()

        val notificationParams = HashMap<String, String>()
        notificationParams[muid] = loginResponse.mobileUserId
        notificationParams[Service] = Fields.NotificationList
        notificationParams[Fields.username] = getSharedString(Constants.UserName)
        notificationParams[Fields.sessionId] = loginResponse.sessionId
        jsonNotifyList(notificationParams)
        enableSwipeToDeleteAndUndo() //Swipe Listener

        sessionId = loginResponse.sessionId
        muId = loginResponse.mobileUserId

//        notificationsViewModel.text.observe(this, Observer {
//            textView.text = it
//        })
        return root
    }

    override fun onResume() {
        super.onResume()
        notificationListAdapter.notifyDataSetChanged()
    }

    private fun jsonNotifyList(notificationParams: HashMap<String, String>) {
        showDialog("Loading in...")
        notificationsViewModel.notificationList(notificationParams)
        notificationsViewModel.notificationList.observe(viewLifecycleOwner, Observer {
            cancelDialog()
            if (it.responseCode.equals("0000", true)) {

                when(notificationParams[Service]){
                    Fields.NotificationList -> {
                        if (it.responseDescription.equals("No notifications found", true)) {
                            Toast.makeText(context, it.responseDescription, Toast.LENGTH_SHORT).show()
                        }
                        if (it.responseList.count() > 0) {
                            notificationList.clear()
                            notificationList.addAll(it.responseList)
                            Log.e("NotifyList", "" + it.responseList.count())
                            notificationListAdapter.notifyDataSetChanged()

                        } else {
                            Toast.makeText(context, it.responseDescription, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            Log.e("Notification List ", it.responseDescription)
        })
    }

    fun jsonUpdateNotification(notificationParams: HashMap<String, String>) {
        showDialog("Processing in...")
        notificationsViewModel.notificationTask(notificationParams)
        notificationsViewModel.notificationTask.observe(this, Observer {
            cancelDialog()
            if (it.responseCode.equals("0000", true)) {

                when(notificationParams[Service]){
                    Fields.NotificationClearAll -> {
                        notificationList.clear()
                        notificationListAdapter.notifyDataSetChanged()
                        shortToast(it.responseDescription)
                    }
                    Fields.NotificationReadAll -> {
                        for (notifyData in notificationList){
                            notifyData.msgRead = true
                        }
                        notificationListAdapter.notifyDataSetChanged()
                    }
                    Fields.NotificationUnReadAll -> {
                        for (notifyData in notificationList){
                            notifyData.msgRead = false
                        }
                        notificationListAdapter.notifyDataSetChanged()
                    }
                    Fields.NotificationUpdate -> {
                        shortToast(it.responseDescription)
                    }
                }
            }

            Log.e("Notification List ", it.responseDescription)
        })
    }

    private fun enableSwipeToDeleteAndUndo() {
        val swipeToDeleteCallback = object : SwipeToDeleteCallback(context) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {
                // i = 4 = delete
                //i = 8 =  mark read
                Log.e("Direction", " $i")
                if (i == 4) {
                    val position = viewHolder.adapterPosition
                    val item = notificationListAdapter.getData()[position]
                    notificationListAdapter.removeItem(position)
                    val snackBar = Snackbar
                        .make(
                            coordinatorLayout,
                            "Item was removed from the list.",
                            Snackbar.LENGTH_LONG
                        )
                    snackBar.setAction("UNDO") {
                        item.let { it1 ->
                            notificationListAdapter.restoreItem(it1, position)
                        }
                        notifyRecyclerView.scrollToPosition(position)
                    }
                    snackBar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        override fun onShown(transientBottomBar: Snackbar?) {
                            super.onShown(transientBottomBar)
                            notificationListAdapter.productList.remove(item)
                            notificationListAdapter.notifyDataSetChanged()
                        }

                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            super.onDismissed(transientBottomBar, event)

                            if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                                // Snackbar closed on its own
                                val notificationParams = HashMap<String, String>()
                                notificationParams["hid"] = item.hid.toString()
                                notificationParams["service"] = Fields.NotificationDelete
                                notificationParams["sessionId"] = sessionId
                                //Get Notification List
                                jsonUpdateNotification(notificationParams)

                                notificationListAdapter.productList.remove(item)
                                notificationListAdapter.notifyDataSetChanged()
                            }
                        }
                    })
                    snackBar.setActionTextColor(Color.YELLOW)
                    snackBar.show()
                } else if (i == 8) {

                    val position = viewHolder.adapterPosition
                    val item = notificationListAdapter.getData()[position]

                    item.msgRead = true

                    val notificationParams = HashMap<String, String>()
                    notificationParams["hid"] = item.hid.toString()
                    notificationParams["service"] = Fields.NotificationUpdate
                    notificationParams["sessionId"] = sessionId

                    jsonUpdateNotification(notificationParams)

                    notificationListAdapter.notifyDataSetChanged()
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(notifyRecyclerView)
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_notification, menu)
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_read -> {
                val params = HashMap<String, String>()
                params["muid"] = muId
                params["service"] = Fields.NotificationReadAll
                params["sessionId"] = sessionId
                alertDialog(params)

                super.onOptionsItemSelected(item)
            }
            R.id.action_unread -> {
                val params = HashMap<String, String>()
                params["muid"] = muId
                params["service"] = Fields.NotificationUnReadAll
                params["sessionId"] = sessionId
                alertDialog(params)
                super.onOptionsItemSelected(item)
            }
            R.id.action_delete -> {
                val params = HashMap<String, String>()
                params["muid"] = muId
                params["service"] = Fields.NotificationClearAll
                params["sessionId"] = sessionId
                alertDialog(params)
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun addFragment(data: NotificationList.Response,msgRead: Boolean) {

        if (!msgRead){
            val notificationParams = HashMap<String, String>()
            notificationParams["hid"] = data.hid.toString()
            notificationParams["service"] = Fields.NotificationUpdate
            notificationParams["sessionId"] = sessionId

            jsonUpdateNotification(notificationParams)

            notificationListAdapter.notifyDataSetChanged()
        }

        bundle.putSerializable("Notification", data)
        addFragment(notificationDetailFragment, bundle,"Notification")
    }

    private fun alertDialog(
        params: HashMap<String, String>
    ) {

        var titleString = ""
        var messageString = ""
        var btnString = ""

        when(params[Service]){
            Fields.NotificationReadAll -> {
                titleString = "Read all notification"
                messageString = "Are you sure to delete all the notifications?"
                btnString = "Read All"
            }
            Fields.NotificationUnReadAll -> {
                titleString = "Unread all notification"
                messageString = "Are you sure to delete all the notifications?"
                btnString = "Unread All"
            }
            Fields.NotificationClearAll -> {
                titleString = "Clear all notification"
                messageString = "Are you sure to delete all the notifications?"
                btnString = "Clear All"
            }
        }

        val builder = AlertDialog.Builder(getActivity())
        builder.setTitle(titleString)
        builder.setMessage(messageString)

        builder.setPositiveButton(btnString) { dialog, which ->
            //Clear Notification List
            jsonUpdateNotification(params)
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
}