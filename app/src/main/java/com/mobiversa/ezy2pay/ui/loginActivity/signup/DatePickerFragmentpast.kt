package com.mobiversa.ezy2pay.ui.loginActivity.signup


import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.utils.Constants


/**
 * Created by Sangavi K
 * For Mobiversa Sdn Bhd
 */
class DatePickerFragmentpast : DialogFragment() {
    var ondateSet: OnDateSetListener? = null
    fun setCallBack(ondate: OnDateSetListener?) {
        ondateSet = ondate
    }

    private var year = 0
    private var month = 0
    private var day = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val mDate =
            DatePickerDialog(context!!, R.style.DialogTheme, ondateSet, year, month, day)
        mDate.datePicker.maxDate = System.currentTimeMillis()
        return mDate
    }

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        if (args != null) {
            year = args.getInt(Constants.YEAR)
            month = args.getInt(Constants.MONTH)
            day = args.getInt(Constants.DAY)
        }

    }
}
