package com.mobiversa.ezy2pay.ui.qrCode

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.mobiversa.ezy2pay.MainActivity
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.base.BaseFragment
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.Fields
import com.mobiversa.ezy2pay.utils.Fields.Companion.Amount
import com.mobiversa.ezy2pay.utils.Fields.Companion.Currency
import com.mobiversa.ezy2pay.utils.Fields.Companion.InvoiceId
import com.mobiversa.ezy2pay.utils.Fields.Companion.Latitude
import com.mobiversa.ezy2pay.utils.Fields.Companion.Longitude
import com.mobiversa.ezy2pay.utils.Fields.Companion.MYR
import com.mobiversa.ezy2pay.utils.Fields.Companion.Service
import com.mobiversa.ezy2pay.utils.Fields.Companion.mid
import com.mobiversa.ezy2pay.utils.Fields.Companion.sessionId
import com.mobiversa.ezy2pay.utils.Fields.Companion.tid
import kotlinx.android.synthetic.main.qr_fragment.view.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern


class QRFragment : BaseFragment() {

    lateinit var boostTimerTxt: TextView
    lateinit var boostQRImage: ImageView
    lateinit var qrLogoImg: ImageView
    private var urlStr = ""
    var service = ""

    companion object {
        fun newInstance() = QRFragment()
    }

    private lateinit var viewModel: QRViewModel

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.qr_fragment, container, false)
        viewModel = ViewModelProviders.of(this).get(QRViewModel::class.java)

        boostTimerTxt = rootView.qr_timer_txt
        boostQRImage = rootView.qr_scanner_img
        qrLogoImg = rootView.qr_logo_img

        service = arguments!!.getString(Service) as String
        val totalAmount = arguments!!.getString(Amount) as String
        val invoiceId = arguments!!.getString(InvoiceId) as String
        val qrParams = HashMap<String, String>()

        when (service) {
            Fields.BoostQR -> {
                (activity as MainActivity).supportActionBar?.title = "Boost"
                setTitle("Boost", false)
                qrLogoImg.setImageResource(R.drawable.ic_boost)
                if (!getLoginResponse().type.equals(Constants.LITE))
                    urlStr = "mobiapr19/mobi_jsonservice/"
                else {
                    urlStr = "mobilite/jsonservice"
                    qrParams[Fields.mobiLiteTid] = getLoginResponse().liteMid
                }

                when {
                    getLoginResponse().tid.isNotEmpty() -> {
                        qrParams[tid] = getLoginResponse().tid
                        qrParams[mid] = getLoginResponse().mid
                    }
                    getLoginResponse().motoTid.isNotEmpty() -> {
                        qrParams[tid] = getLoginResponse().motoTid
                        qrParams[mid] = getLoginResponse().motoMid
                    }
                    getLoginResponse().ezypassTid.isNotEmpty() -> {
                        qrParams[tid] = getLoginResponse().ezypassTid
                        qrParams[mid] = getLoginResponse().ezypassMid
                    }
                    getLoginResponse().ezyrecTid.isNotEmpty() -> {
                        qrParams[tid] = getLoginResponse().ezyrecTid
                        qrParams[mid] = getLoginResponse().ezyrecMid
                    }
                    getLoginResponse().gpayTid.isNotEmpty() -> {
                        qrParams[tid] = getLoginResponse().gpayTid
                        qrParams[mid] = getLoginResponse().gpayMid
                    }
                }

            }
            Fields.GPayQR -> {
                (activity as MainActivity).supportActionBar?.title = "GrabPay"
                setTitle("GrabPay", false)
                qrLogoImg.setImageResource(R.drawable.ic_grabpay)
                urlStr = "grabpay/mobi_jsonservice/"

                qrParams[tid] = getLoginResponse().gpayTid
                qrParams[mid] = getLoginResponse().gpayMid
            }
            Fields.MobiPassQR -> {
                (activity as MainActivity).supportActionBar?.title = "MobiPass"
                setTitle("MobiPass", false)
                qrLogoImg.setImageResource(R.drawable.mobipass_icon)
                urlStr = "mobiapr19/mobi_jsonservice/"

                qrParams[tid] = getLoginResponse().ezypassTid
                qrParams[mid] = getLoginResponse().ezypassMid
            }
        }

        setHasOptionsMenu(true)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        rootView.amount_txt.text = "RM $totalAmount"

        qrParams[Service] = service
        qrParams[sessionId] = getLoginResponse().sessionId
        qrParams[Currency] = MYR
        qrParams[Amount] = getAmount(totalAmount)
        qrParams[Latitude] = Constants.latitudeStr
        qrParams[Longitude] = Constants.longitudeStr
        qrParams[Fields.Location] = if (Pattern.matches(
                ".*[a-zA-Z]+.*[a-zA-Z]",
                Constants.countryStr
            )
        ) Constants.countryStr else ""
        qrParams[InvoiceId] = invoiceId

        jsonBoostQR(qrParams)

        return rootView
    }

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(QRViewModel::class.java)
    }

    private fun jsonBoostQR(qrParams: HashMap<String, String>) {
        showLog("Boost", ""+qrParams)
        showDialog("Generating Please wait...")
        viewModel.boostQR(urlStr, qrParams)
        viewModel.qrModel.observe(viewLifecycleOwner, Observer {
            cancelDialog()
            if (it.responseCode.equals("0000", true)) {

                when (qrParams[Service]) {
                    Fields.BoostQR -> {
                        boostQRImage.setImageBitmap(it.responseData.base64ImageQRCode.decodeBase64IntoBitmap())
                    }
                    Fields.MobiPassQR -> {
                        try {
                            val mBitmap = TextToImageEncode(it.responseData.base64ImageQRCode)
                            boostQRImage.setImageBitmap(mBitmap)
                        } catch (e: WriterException) {
                            e.printStackTrace()
                        }
                    }
                    Fields.GPayQR -> {
                        try {
                           val mBitmap = TextToImageEncode(it.responseData.qrcode)
                            boostQRImage.setImageBitmap(mBitmap)
                        } catch (e: WriterException) {
                            e.printStackTrace()
                        }
                    }
                }
                timerTask()
            } else {
                setTitle("Home", true)
                (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
                fragmentManager?.popBackStack()
            }

        })
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val activity = activity as? MainActivity
        return when (item.itemId) {
            android.R.id.home -> {
                context!!.startActivity(Intent(getActivity(), MainActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun timerTask() {
        val countDownTimer = object : CountDownTimer(120000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                Log.d("tekloon", "millisUntilFinished $millisUntilFinished")
                val hms = String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                    ),
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                    )
                )
                boostTimerTxt.text = "QR Expires : $hms"
            }

            override fun onFinish() {
                Log.d("tekloon", "onFinish")
                boostTimerTxt.text = "TIME'S UP!!"
            }
        }
        countDownTimer.start()
    }

    //Convert Text to QRCode image
    @Throws(WriterException::class)
    fun textToImageEncode(
        Value: String?,
        imageView: ImageView,
        service: String
    ): Bitmap? {
        val bitMatrix: BitMatrix = try {
            MultiFormatWriter().encode(
                Value,
                BarcodeFormat.DATA_MATRIX,
                200,
                200
            )
        } catch (Illegalargumentexception: IllegalArgumentException) {
            return null
        }
        val bitMatrixWidth = bitMatrix.width
        val bitMatrixHeight = bitMatrix.height
        val pixels = IntArray(bitMatrixWidth * bitMatrixHeight)
        for (y in 0 until bitMatrixHeight) {
            val offset = y * bitMatrixWidth
            for (x in 0 until bitMatrixWidth) {

                if (service.equals(Fields.GPayQR, true)) {
                    pixels[offset + x] = if (bitMatrix[x, y]) resources.getColor(R.color.green) else resources.getColor(R.color.white)
                } else {
                    pixels[offset + x] = if (bitMatrix[x, y]) resources.getColor(R.color.txt_black) else resources.getColor(R.color.white)
                }
            }
        }
        val bitmap =
            Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444)
        bitmap.setPixels(
            pixels,
            0,
            200,
            0,
            0,
            bitMatrixWidth,
            bitMatrixHeight
        )

        imageView.setImageBitmap(bitmap)
        return bitmap
    }

    @Throws(WriterException::class)
    fun TextToImageEncode(Value: String?): Bitmap? {
        val bitMatrix: BitMatrix = try {
            MultiFormatWriter().encode(
                Value,
                BarcodeFormat.QR_CODE,
                200,
                200
            )
        } catch (Illegalargumentexception: java.lang.IllegalArgumentException) {
            return null
        }
        val bitMatrixWidth = bitMatrix.width
        val bitMatrixHeight = bitMatrix.height
        val pixels = IntArray(bitMatrixWidth * bitMatrixHeight)
        for (y in 0 until bitMatrixHeight) {
            val offset = y * bitMatrixWidth
            for (x in 0 until bitMatrixWidth) {
                if (service.equals(Fields.GPayQR)) {
                    pixels[offset + x] = if (bitMatrix[x, y]
                    ) resources.getColor(R.color.green) else resources.getColor(R.color.white)
                }else{
                    pixels[offset + x] = if (bitMatrix[x, y]
                    ) resources.getColor(R.color.black) else resources.getColor(R.color.white)
                }
            }
        }
        val bitmap =
            Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444)
        bitmap.setPixels(pixels,
            0,
            200,
            0,
            0,
            bitMatrixWidth,
            bitMatrixHeight
        )
        return bitmap
    }


}
