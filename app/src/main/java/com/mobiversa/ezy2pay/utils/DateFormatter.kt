package com.mobiversa.ezy2pay.utils

import android.text.format.DateFormat
import android.text.format.DateUtils
import android.util.Log
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class DateFormatter {
    companion object {
        var date = 0
        var month = 0
        var year = 0
        var dayName: String? = null
        var monthName: String? = null
        var hour = 0
        var min = 0
        var time: String? = null
        var session: String? = null
        var today: Boolean? = null
        var pastTime: String? = null
        var sdf =
            SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH)
        var sdfRange =
            SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        var sdfComp =
            SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

        fun DateParsing(dateStr: String?) {
            try {
                val date1 = sdf.parse(dateStr)
                date = DateFormat.format("dd", date1).toString().toInt() // 20
                month = DateFormat.format("MM", date1).toString().toInt() // 06
                year = DateFormat.format("yyyy", date1).toString().toInt()// 2013
                monthName = DateFormat.format("MMM", date1) as String // 2013
                dayName = DateFormat.format("E", date1) as String // 2013
                time = DateFormat.format("hh:mm a", date1) as String // 2013
                hour = DateFormat.format("h", date1).toString().toInt()// 2013
                min = DateFormat.format("h", date1).toString().toInt()// 2013 // 2013
                session =
                    DateFormat.format("a", date1) as String // 2013
                val date = sdfComp.parse(dateStr)
                val millis = date.time
                today = DateUtils.isToday(millis)
                pastTime = DateUtils.getRelativeTimeSpanString(millis) as String
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }

        fun DateComparison(dateStr1: String?, dateStr2: String?): String {
            var comparison = ""
            try {
                val date1 = sdfComp.parse(dateStr1)
                val date2 = sdfComp.parse(dateStr2)
                if (date1 == date2) {
                    comparison = "equal"
                } else if (date2.after(date1)) {
                    comparison = "after"
                } else if (date2.before(date1)) {
                    comparison = "before"
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return comparison
        }

        fun getDates(
            date1: Date?,
            date2: Date?
        ): List<Date> {
            val dates = ArrayList<Date>()
            val cal1 = Calendar.getInstance()
            cal1.time = date1
            val cal2 = Calendar.getInstance()
            cal2.time = date2
            while (!cal1.after(cal2)) {
                dates.add(cal1.time)
                cal1.add(Calendar.DATE, 1)
            }
            return dates
        }

        fun DateFormateChange(date: Date?): String {
            val formatterOut = SimpleDateFormat("dd/MM/yyyy")
            return formatterOut.format(date)
        }

        fun getDaysCount(date1: Date,
                         date2: Date) : String{
            val myFormat = SimpleDateFormat("dd MM yyyy")
            var days : Long = 0
            try {
//                val date1 = myFormat.parse(dateStr1)
//                val date2 = myFormat.parse(dateStr2)
                val diff = date2.time - date1.time
                Log.e("Days: " ,""+ TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS))
                days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
            } catch (e: ParseException) {
                e.printStackTrace()
            }


            if (days<30){

                return  "" + (30 - days)

            }else{
                return "1"
            }


        }
    }



}
