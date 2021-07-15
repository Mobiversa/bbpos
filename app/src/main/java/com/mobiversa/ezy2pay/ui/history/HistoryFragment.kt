package com.mobiversa.ezy2pay.ui.history

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobiversa.ezy2pay.MainActivity
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.adapter.TransactionHistoryAdapter
import com.mobiversa.ezy2pay.base.BaseFragment
import com.mobiversa.ezy2pay.network.ApiService
import com.mobiversa.ezy2pay.network.response.ForSettlement
import com.mobiversa.ezy2pay.network.response.TransactionHistoryModel
import com.mobiversa.ezy2pay.network.response.VoidHistoryModel
import com.mobiversa.ezy2pay.ui.history.historyDetail.HistoryDetailFragment
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.Constants.Companion.UserName
import com.mobiversa.ezy2pay.utils.Fields
import com.mobiversa.ezy2pay.utils.Fields.Companion.PREAUTH
import com.mobiversa.ezy2pay.utils.MarginItemDecoration
import com.mobiversa.ezy2pay.utils.PreferenceHelper
import com.mobiversa.ezy2pay.utils.PreferenceHelper.get
import de.adorsys.android.finger.Finger
import de.adorsys.android.finger.FingerListener
import kotlinx.android.synthetic.main.fragment_history.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class HistoryFragment : BaseFragment(), View.OnClickListener {

    private var position: Int? = null

    private lateinit var historyViewModel: HistoryViewModel
    var trxType = Fields.CARD
    private lateinit var trxTypeSpinner: NDSpinner
    private lateinit var trxTypeAdapter: ArrayAdapter<String>
    private var historyList = ArrayList<ForSettlement>()
    private var rereshOnResume: Boolean = false
    private lateinit var historyAdapter: TransactionHistoryAdapter
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historySearch: SearchView
    private lateinit var btnSettlementHistory: RelativeLayout

    private lateinit var historyData: ForSettlement
    private var historyType: String = ""
    var firstTimeJson = true

    var transactionType: String = ""
    val requestData = HashMap<String, String>()
    private lateinit var customPrefs: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        historyViewModel.transactionHistoryList.observeForever(dataObserver)

    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).supportActionBar?.show()
        setTitle("Transactions", true)
        if (rereshOnResume) {
            transactionHistory()
        }
        rereshOnResume = false
    }

    override fun onPause() {
        super.onPause()
        rereshOnResume = true
    }

    //Fragment Navigation
    private val historyDetailFragment = HistoryDetailFragment()
    val bundle = Bundle()
    private lateinit var finger: Finger

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        historyViewModel =
            ViewModelProviders.of(this.getActivity()!!).get(HistoryViewModel::class.java)
        val rootView = inflater.inflate(R.layout.fragment_history, container, false)

        initialize(rootView)

        showLog("TId ", getLoginResponse().tid)
        return rootView
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    fun initialize(rootView: View) {
        setTitle("Transactions", true)
        (activity as MainActivity).supportActionBar?.show()
        //To set the Traansaction type
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        customPrefs = PreferenceHelper.customPrefs(context!!, "REMEMBER")

        checkAndRequestPermissions()

        trxTypeSpinner = rootView.trx_type_spnr
        historyRecyclerView = rootView.rcy_history_list
        historySearch = rootView.history_search
        btnSettlementHistory = rootView.btn_settlement_history
        btnSettlementHistory.setOnClickListener(this)

        btnSettlementHistory.visibility = View.GONE

        finger = Finger(this.context!!)
        searchView()

        historyRecyclerView.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        historyRecyclerView.addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.xxhdpi_10).toInt()
            )
        )
        /*historyRecyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
            )
        )*/
        historyAdapter = TransactionHistoryAdapter(historyList, context!!, this)
        historyRecyclerView.adapter = historyAdapter
        enableVoidOption()

        trxTypeAdapter = ArrayAdapter(context!!, R.layout.spinner_item, getTrxList())
        trxTypeSpinner.adapter = trxTypeAdapter
        trxTypeSpinner.dropDownWidth = width

        trxTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long
            ) {

                showLog("Selected", getTrxList()[position])

                //To make text clear
                (parentView?.getChildAt(0) as TextView?)?.setTextColor(0xFFFFFF)

                trxType = when (getTrxList()[position]) {
                    Fields.EZYWIRE -> Fields.CARD
                    else -> getTrxList()[position]
                }

                transactionHistory()

            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // your code here
            }

        }


    }

    fun transactionHistory() {

        val historyParam = HashMap<String, String>()

        historyParam[Fields.username] = customPrefs[UserName]!!
        historyParam[Fields.sessionId] = getLoginResponse().sessionId
        historyParam[Fields.MerchantId] = getLoginResponse().merchantId
        historyParam[Fields.HostType] = getLoginResponse().hostType
        historyParam[Fields.TRX_TYPE] = trxType
        historyParam[Fields.Service] = Fields.TRX_HISTORY
        if (trxType.equals(Fields.GRABPAY, ignoreCase = true)) {
            historyParam[Fields.tid] = getLoginResponse().gpayTid
        } else {
            historyParam[Fields.tid] = getTidValue()
        }

        if (!getLoginResponse().type.equals(Constants.Normal, true)) {
            historyParam[Fields.liteMid] = getLoginResponse().liteMid
            historyParam[Fields.Service] = Fields.LITE_TXN_HISTORY
        }


        historyParam[Fields.Type] = getLoginResponse().type.toUpperCase(Locale.ROOT)
        jsonHistoryListEnque(historyParam)
    }

    private fun preAuthTransHistory(historyType: String) {

        this.historyType = historyType

        val historyParam = HashMap<String, String>()

        historyParam[Fields.sessionId] = getLoginResponse().sessionId
        historyParam[Fields.MerchantId] = getLoginResponse().merchantId
        historyParam[Fields.HostType] = getLoginResponse().hostType
        historyParam[Fields.TRX_TYPE] = trxType
        historyParam[Fields.Service] = Fields.PERAUTHHIST

        if (historyType.equals(Fields.EZYMOTO, true)) {
            historyParam[Fields.tid] = getLoginResponse().motoTid
        } else {
            historyParam[Fields.tid] = getLoginResponse().tid
        }

        jsonHistoryListEnque(historyParam)
    }

    private fun jsonHistoryListEnque(historyParam: HashMap<String, String>) {
        showDialog("Loading History...")
        transactionType = historyParam[Fields.Service]!!
        val apiResponse = ApiService.serviceRequest()
        apiResponse.getTransactionHistory(historyParam).enqueue(object :
            Callback<TransactionHistoryModel> {
            override fun onFailure(call: Call<TransactionHistoryModel>, t: Throwable) {
                cancelDialog()
            }

            override fun onResponse(
                call: Call<TransactionHistoryModel>,
                response: Response<TransactionHistoryModel>
            ) {
                cancelDialog()
                if (response.isSuccessful) {
                    historyObserveData(response.body()!!)
                }
            }
        })
    }

    private val dataObserver = Observer<TransactionHistoryModel> { data ->
        historyObserveData(data)
    }

    private fun historyObserveData(it: TransactionHistoryModel) {
        var count = 0
        var completedCount = 0
        if (it.responseCode.equals("0000", true)) {
            showLog("Auth", trxType)
            if (trxType.equals(PREAUTH, true)) {
                val authSize = it.responseData.preAuthorization?.size ?: 0
                if (authSize > 0) {
                    historyList.clear()
                    it.responseData.preAuthorization?.let { it1 -> historyList.addAll(it1) }
                    historyAdapter.notifyDataSetChanged()
                    count = 0

                    for (historyData in historyList) {
                        showLog("History", historyData.status)

                        if (historyData.status.equals("PENDING", false)) {
                            count++
                        }
                        if (historyData.status.equals("E", false)) {
                            completedCount++
                        }
                    }

                    showLog("History", "" + completedCount)
                } else {
                    historyList.clear()
                    historyAdapter.notifyDataSetChanged()
                    shortToast("No Data Found")
                }
            } else {
                if (it.responseData.forSettlement != null) {
                    if (it.responseData.forSettlement.isNotEmpty()) {
                        historyList.clear()
                        historyList.addAll(it.responseData.forSettlement)
                        historyAdapter.notifyDataSetChanged()

                        for (historyData in historyList) {
                            if (historyData.status.equals("PENDING", false)) {
                                count++
                            }
                            if (historyData.status.equals("COMPLETED", false)) {
                                completedCount++
                            }
                        }

                        showLog("History", "" + completedCount)

                    } else {
                        historyList.clear()
                        historyAdapter.notifyDataSetChanged()
                        shortToast("No Data Found")
                    }
                }
            }

            btnSettlementVisibility(trxType, count, completedCount)
        }else{
            shortToast(it.responseDescription)
        }
    }

    //Show hide Settlement Button in History page
    private fun btnSettlementVisibility(trxType: String, pendingCount: Int, completedCount: Int) {
        showLog("Service", trxType)

        if (pendingCount > 0) {
            getActivity()?.let {
                showDialog(
                    "", "You have $pendingCount pending transaction(s), kindly complete.",
                    it
                )
            }
        }

        if (historyList.size <= 0) {
            btnSettlementHistory.visibility = View.GONE
            return
        }

        when (trxType) {
            Fields.ALL -> {
                btnSettlementHistory.visibility = View.GONE
            }
            Fields.CARD -> {
                if (completedCount > 0) {
                    btnSettlementHistory.visibility = View.VISIBLE
                }
            }
            Fields.Moto, Fields.EZYREC,  Fields.EZYSPLIT, Fields.EZYPASS -> {
                if (completedCount > 0 && getLoginResponse().hostType.equals("P", true)) {
                    btnSettlementHistory.visibility = View.VISIBLE
                } else {
                    btnSettlementHistory.visibility = View.GONE
                }
            }
            Fields.BOOST, Fields.GRABPAY, Fields.CASH, PREAUTH -> {
                btnSettlementHistory.visibility = View.GONE
            }
        }
    }

    override fun onStop() {
        super.onStop()
        historyViewModel.transactionHistoryList.removeObservers(this)
    }

    private fun getTrxList(): ArrayList<String> {
        val histList = ArrayList<String>()
        for (data in getProductList()) {
            if (data.isEnable) {
                if (data.historyName.equals(Constants.MobiCash)) {
                    histList.add(Fields.FPX)
                }
                histList.add(data.historyName)
            }
        }
        return histList
    }

    private fun enableVoidOption() {
        val swipeToDeleteCallback = object : SwipeToVoidCallback(context) {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val position = viewHolder.adapterPosition
                return if (historyAdapter.getData().size > position) {
                    val item = historyAdapter.getData()[position]
                    return if (item.status.equals("PENDING", true)
                        || item.txnType.equals(Fields.FPX, true)
                        || item.status.equals("REFUND", true)
                        || item.status.equals("VOID", false)
                        || item.status.equals("VOID", false)
                    )
                        makeMovementFlags(0, 0)
                    else
                        makeMovementFlags(0, ItemTouchHelper.LEFT)
                } else
                    makeMovementFlags(0, 0)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {
                // i = 4 = delete
                //i = 8 =  mark read
                showLog("Direction", " $i")
                if (i == 4) {
                    val dataPosition = viewHolder.adapterPosition
                    val item = historyAdapter.getData()[dataPosition]
                    position = dataPosition
                    historyAdapter.notifyDataSetChanged()
                    if (item.txnType.equals(Fields.CASH)) {
                        requestData.clear()
                        jsonVoidTransaction("Void", item, requestData)
                    } else
                        showPasswordPrompt(item)
                }
            }

        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(historyRecyclerView)
    }

    private fun showPasswordPrompt(item: ForSettlement) {
        lateinit var mAlertDialog: AlertDialog

        val inflater = getActivity()!!.layoutInflater
        val alertLayout: View = inflater.inflate(R.layout.alert_void_validate, null)
        val etUsername = alertLayout.findViewById<View>(R.id.username_edt_void) as EditText
        val etPassword = alertLayout.findViewById<View>(R.id.password_edt_void) as EditText
        val btnVoid = alertLayout.findViewById<View>(R.id.btn_alert_void) as Button
        val btnCancel = alertLayout.findViewById<View>(R.id.btn_alert_cancel) as Button
        val savedName = getSharedString(UserName)
        etUsername.setText(savedName)
        etUsername.isEnabled = false
        etPassword.isEnabled = true
        etPassword.isFocusable = true
        etPassword.requestFocus()
        val alert: AlertDialog.Builder = AlertDialog.Builder(getActivity(),R.style.AlertDialogTheme)
        alert.setView(alertLayout)
        alert.setCancelable(false)

        btnVoid.setOnClickListener {
            val textPassword = etPassword.text.toString()
            if (!textPassword.equals("", ignoreCase = true)) {
                mAlertDialog.dismiss()
//                val requestVal = HashMap<String, String>()
//                requestVal[Fields.Service] = Fields.VALIDATE_VOID
                requestData[Fields.username] = getSharedString(UserName)
                requestData[Fields.password] = textPassword
//                jsonUserValidation(requestVal, item)
                jsonVoidTransaction("Void", item, requestData)
            } else { //                    Toast.makeText(getActivity(), Constants.ENTER_PASSWORD, Toast.LENGTH_SHORT).show();
                shortToast(Constants.ENTER_PASSWORD)
            }
        }

        btnCancel.setOnClickListener {
            mAlertDialog.dismiss()
        }

        mAlertDialog = alert.create()
        mAlertDialog.show()
        //startActivity(new Intent(GoogleMaps.this, VoidAuthActivity.class).putExtra("trensID", transId));
    }

    private fun showFingerAuthenticationDialog() {
        finger.showDialog(
            this.getActivity()!!,
            Triple(
                // title
                getString(R.string.text_fingerprint),
                // subtitle
                null,
                // description
                null
            )
        )
    }

    private fun jsonUserValidation(
        userValidateParam: HashMap<String, String>,
        item: ForSettlement
    ) {
        showDialog("Validating...")
        historyViewModel.getUserVerification(userValidateParam)
        historyViewModel.userVerification.observe(this, Observer {
            cancelDialog()
            if (it.responseCode.equals("0000", true)) {
                showLog("Void Test", it.responseDescription)
//                jsonVoidTransaction("Void", item)
            }
        })
    }

    private fun jsonVoidTransaction(
        status: String,
        historyData: ForSettlement,
        requestData: HashMap<String, String>
    ) {
        var pathStr = "mobiapr19"
        val requestVal = requestData
        when {
            historyData.txnType.equals(Fields.CASH) -> {
                requestVal[Fields.Service] = Fields.CASH_CANCEL
                requestVal[Fields.sessionId] = getLoginResponse().sessionId
                requestVal[Fields.tid] = getLoginResponse().tid
                requestVal[Fields.trxId] = historyData.txnId
            }
            historyData.txnType.equals(Fields.BOOST) -> {
                when {
                    status.equals("Cancel", false) -> {
                        requestVal[Fields.Service] = Fields.BOOST_STATUS
                        requestData[Fields.username] = getSharedString(UserName)
                        requestVal[Fields.transactionStatus] = Fields.VOID
                    }
                    status.equals("Yes", false) -> {
                        requestVal[Fields.Service] = Fields.BOOST_STATUS
                        requestData[Fields.username] = getSharedString(UserName)
                        requestVal[Fields.transactionStatus] = Fields.COMPLETED
                    }
                    else -> {
                        requestVal[Fields.Service] = Fields.BOOST_VOID
                    }
                }

                requestVal[Fields.sessionId] = getLoginResponse().sessionId
                requestVal[Fields.tid] = getLoginResponse().tid
                requestVal[Fields.mid] = getLoginResponse().mid
                requestVal[Fields.AID] = historyData.aidResponse
                requestVal[Fields.trxId] = historyData.txnId
                requestVal[Fields.InvoiceId] = historyData.invoiceId ?: ""
            }
            historyData.txnType.equals(Fields.GRABPAY) -> {
                pathStr = "grabpay"
                when {
                    status.equals("Yes", false) -> {
                        requestVal[Fields.Service] = Fields.GPAY_CANCEL
                    }
                    else -> {
                        requestVal[Fields.Service] = Fields.GPAY_REFUND
                    }
                }

                requestVal[Fields.sessionId] = getLoginResponse().sessionId
                requestVal[Fields.tid] = getLoginResponse().tid
                requestVal[Fields.mid] = getLoginResponse().mid
                requestVal[Fields.AID] = historyData.aidResponse
                requestVal[Fields.trxId] = historyData.txnId
                requestVal[Fields.InvoiceId] = historyData.invoiceId ?: ""
            }
            historyData.txnType.equals(Fields.PRE_AUTH) -> {

                when {
                    status.equals("Complete", true) -> {
                        requestVal[Fields.Service] = Fields.PRE_AUTH_SALE
//                        requestData[Fields.username] = getSharedString(UserName)
                    }
                    status.equals("Cancel", true) -> {
                        requestVal[Fields.Service] = Fields.PRE_AUTH_VOID
//                        requestData[Fields.username] = getSharedString(UserName)
                    }
                }

                requestVal[Fields.sessionId] = getLoginResponse().sessionId
                requestVal[Fields.trxId] = historyData.txnId
                requestVal[Fields.HostType] = getLoginResponse().hostType
                requestVal[Fields.MerchantId] = getLoginResponse().merchantId
                if (getLoginResponse().tid.isNotEmpty()) {
                    requestVal[Fields.tid] = getLoginResponse().tid
                } else {
                    requestVal[Fields.tid] = getLoginResponse().motoTid
                }

            }
            else -> { //Ezywire

                when {
                    status.equals("Complete", false) -> {
                        requestVal[Fields.Service] = Fields.SALE_ACK
                    }
                    status.equals("Cancel", false) -> {
                        requestVal[Fields.Service] = Fields.VOID
                    }
                    else -> {
                        requestVal[Fields.Service] = Fields.VOID
                    }
                }

                requestVal[Fields.sessionId] = getLoginResponse().sessionId
                requestVal[Fields.trxId] = historyData.txnId
                requestVal[Fields.HostType] = getLoginResponse().hostType
                requestVal[Fields.MerchantId] = getLoginResponse().merchantId
                requestVal[Fields.tid] = getLoginResponse().tid
            }
        }

        showDialog("Processing...")
        val apiResponse = ApiService.serviceRequest()
        apiResponse.setVoidTransaction(pathStr, requestVal)
            .enqueue(object : Callback<VoidHistoryModel> {
                override fun onFailure(call: Call<VoidHistoryModel>, t: Throwable) {
                    cancelDialog()
                }

                override fun onResponse(
                    call: Call<VoidHistoryModel>,
                    response: Response<VoidHistoryModel>
                ) {
                    cancelDialog()
                    if (response.isSuccessful) {
                        shortToast(response.body()!!.responseDescription)
                        transactionHistory()
                    }
                }
            })
    }

    private fun jsonSettlement() {
        showDialog("Validating...")
        val reqParam = HashMap<String, String>()
        reqParam[Fields.Service] = Fields.SETTLEMENT
        reqParam[Fields.sessionId] = getLoginResponse().sessionId
        reqParam[Fields.HostType] = getLoginResponse().hostType
        reqParam[Fields.MerchantId] = getLoginResponse().merchantId
        when (trxTypeSpinner.selectedItem.toString()) {
            Constants.EzyMoto -> {
                reqParam[Fields.tid] = getLoginResponse().motoTid
            }
            Fields.EZYPASS -> {
                reqParam[Fields.tid] = getLoginResponse().ezypassTid
            }
            Fields.EZYREC -> {
                reqParam[Fields.tid] = getLoginResponse().ezyrecTid
            }
            Fields.EZYSPLIT -> {
                reqParam[Fields.tid] = getLoginResponse().ezysplitTid
            }
            else -> {
                reqParam[Fields.tid] = getLoginResponse().tid
            }
        }
        historyViewModel.getSettlement(reqParam)
        historyViewModel.settlementData.observe(this, Observer {
            cancelDialog()
            if (it.responseCode.equals("0000", true)) {
                transactionHistory()
            }
            shortToast(it.responseDescription)
        })
    }

    //Search Option
    private fun searchView() {
        historySearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                showLog("Query", "" + query)
                historyAdapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                historyAdapter.filter.filter(newText)
                return false
            }
        })
    }

    fun addFragment(data: ForSettlement, bundle: Bundle) {
        bundle.putSerializable("History", data)
        bundle.putString(Fields.TRX_TYPE, trxType)
        addFragment(historyDetailFragment, bundle, "History")
    }

    fun showAlert(forSettlement: ForSettlement) {

        showLog("Trx_type", "" + forSettlement.txnType)
        var descriptionStr = "Do you want to COMPLETE this transaction?"
        var positiveStr = "Complete"
        var negativeStr = "Dismiss"
        var neutralStr = "Cancel"
        var titleStr = ""

        when {
            forSettlement.txnType.equals(Fields.PRE_AUTH, false) -> {
                titleStr = PREAUTH
            }
            else -> {
                titleStr = forSettlement.txnType.toString()
            }
        }

        when {
            forSettlement.txnType.equals(Fields.BOOST, false) -> {
                descriptionStr = "Do you want to COMPLETE this transaction?"
                positiveStr = "Cancel"
                negativeStr = "Dismiss"
                neutralStr = "Yes"
            }
            forSettlement.txnType.equals(Fields.GRABPAY, false) -> {
                descriptionStr = "Do you want to CANCEL this transaction?"
                positiveStr = "Yes"
                negativeStr = "Dismiss"
            }
            else -> {
                descriptionStr = "Do you want to COMPLETE this transaction?"
                positiveStr = "Complete"
                negativeStr = "Dismiss"
                neutralStr = "Cancel"
            }
        }
        // Initialize a new instance of
        val builder = AlertDialog.Builder(context,R.style.AlertDialogTheme)
        builder.setTitle(titleStr)
        builder.setMessage(descriptionStr)
        builder.setPositiveButton(positiveStr) { dialog, which ->
            requestData.clear()
            jsonVoidTransaction(positiveStr, forSettlement, requestData)
            dialog.dismiss()
        }
        builder.setNegativeButton(negativeStr) { dialog, which ->
            dialog.dismiss()
        }
        builder.setNeutralButton(neutralStr) { dialog, which ->
            requestData.clear()
            jsonVoidTransaction(neutralStr, forSettlement, requestData)
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showAuthPrompt() {
        lateinit var mAlertDialog: AlertDialog

        val inflater = getActivity()!!.layoutInflater
        val alertLayout: View = inflater.inflate(R.layout.alert_ezyauth, null)
        val digitalImg = alertLayout.findViewById<View>(R.id.ezydigital_img) as ImageView
        val ezywireImg = alertLayout.findViewById<View>(R.id.ezywire_img) as ImageView
        val alert: AlertDialog.Builder = AlertDialog.Builder(getActivity(),R.style.AlertDialogTheme)
        alert.setView(alertLayout)
        alert.setCancelable(true)

        if (getProductList()[1].isEnable)
            ezywireImg.setImageResource(R.drawable.ezyauth)
        else
            ezywireImg.setImageResource(R.drawable.auth_wire_disable)

        if (getProductList()[0].isEnable)
            digitalImg.setImageResource(R.drawable.auth_digital_enable)
        else
            digitalImg.setImageResource(R.drawable.auth_digital_disable)

        ezywireImg.setOnClickListener {
            if (getProductList()[1].isEnable) {
                preAuthTransHistory(Fields.CARD)
            } else {
                shortToast("You are not Subscribed for ${getProductList()[1].productName}")
            }
            mAlertDialog.dismiss()

        }

        digitalImg.setOnClickListener {
            if (getProductList()[0].isEnable) {
                preAuthTransHistory(Fields.EZYMOTO)
            } else {
                shortToast("You are not Subscribed for ${getProductList()[0].productName}")
            }
            mAlertDialog.dismiss()
        }

        mAlertDialog = alert.create()
        mAlertDialog.show()
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

    private fun showServiceCharge() {
        lateinit var mAlertDialog: AlertDialog

        val inflater = layoutInflater
        val alertLayout: View = inflater.inflate(R.layout.settlement_dialog, null)
        val btnConfirm = alertLayout.findViewById<View>(R.id.button_confirm) as Button
        val btnCancel = alertLayout.findViewById<View>(R.id.button_cancel) as Button
        val alert: AlertDialog.Builder = AlertDialog.Builder(this.context)
        alert.setView(alertLayout)
        alert.setCancelable(false)
        Constants.isSwipe = false

        btnConfirm.setOnClickListener {
            jsonSettlement()
            mAlertDialog.dismiss()
        }

        btnCancel.setOnClickListener {
            mAlertDialog.dismiss()
        }

        mAlertDialog = alert.create()
        mAlertDialog.show()
    }

    override fun onClick(v: View) {

        when (v.id) {
            R.id.btn_settlement_history -> {
                showServiceCharge()
            }
        }

    }
}
