package com.mobiversa.ezy2pay.utils

import android.util.Base64
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and


/**
 * Created by Karthik
 */
class Encryptor {
 companion object Encrypt {

    fun encrypt(
        key: String,
        initVector: String,
        value: String
    ): String? {
        try {
            val iv =
                IvParameterSpec(initVector.toByteArray(charset("UTF-8")))
            val skeySpec =
                SecretKeySpec(key.toByteArray(charset("UTF-8")), "AES")
            val cipher =
                Cipher.getInstance("AES/CBC/PKCS5PADDING")
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv)
            val encrypted = cipher.doFinal(value.toByteArray())
            println(
                "encrypted string: "
                        + Base64.encodeToString(
                    encrypted,
                    Base64.DEFAULT
                )
            )
            return Base64.encodeToString(encrypted, Base64.DEFAULT)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

    fun decrypt(
        key: String,
        initVector: String,
        encrypted: String?
    ): String? {
        try {
            val iv =
                IvParameterSpec(initVector.toByteArray(charset("UTF-8")))
            val skeySpec =
                SecretKeySpec(key.toByteArray(charset("UTF-8")), "AES")
            val cipher =
                Cipher.getInstance("AES/CBC/PKCS5PADDING")
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv)
            val original =
                cipher.doFinal(Base64.decode(encrypted, Base64.DEFAULT))
            return String(original)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

    fun hexaToAscii(s: String?, toString: Boolean): String {
        var retString = ""
        var tempString = ""
        var offset = 0
        if (toString) {
            for (i in 0 until s!!.length / 2) {
                tempString = s.substring(offset, offset + 2)
                retString += if (tempString.equals(
                        "1c",
                        ignoreCase = true
                    )
                ) "[1C]" else decodeHexString(tempString)
                offset += 2
            } // end for
        } else {
            for (i in 0 until s!!.length) {
                tempString = s.substring(offset, offset + 1)
                retString += encodeHexString(tempString)
                offset += 1
            } // end for
        }
        return retString
    } // end hexaToAscii

    private fun decodeHexString(hexText: String?): String? {
        var decodedText: String? = null
        var chunk: String? = null
        if (hexText != null && hexText.isNotEmpty()) {
            val numBytes = hexText.length / 2
            val rawToByte = ByteArray(numBytes)
            var offset = 0
            for (i in 0 until numBytes) {
                chunk = hexText.substring(offset, offset + 2)
                offset += 2
                rawToByte[i] = (chunk.toInt(16) and 0x000000FF).toByte()
            }
            // System.out.println(rawToByte.toString());
            decodedText = String(rawToByte)
        }
        return decodedText
    }

    fun encodeHexString(sourceText: String): String {
        val rawData = sourceText.toByteArray()
        val hexText = StringBuffer()
        var initialHex: String? = null
        var initHexLength = 0
        for (i in rawData.indices) { // System.out.println("raw "+rawData[i]);
            val positiveValue: Int = (rawData[i] and 0x000000FF.toByte()).toInt()
            initialHex = Integer.toHexString(positiveValue)
            initHexLength = initialHex.length
            while (initHexLength++ < 2) {
                hexText.append("0")
            }
            hexText.append(initialHex)
        }
        return hexText.toString().toUpperCase()
    }

    @JvmStatic
   public fun main(args: Array<String>) { /* String key = "Bar12345Bar12345"; // 128 bit key
        String initVector = "RandomInitVector"; // 16 bytes IV
*/
/*String key = "b07ad9f31df158ed";//b188a41f725899bc"; // 128 bit key
         String initVector = "b188a41f725899bc"; // 16 bytes IV

         String encrypted = null;// encrypt(key, initVector, "4918914107195005#123#1907");
        System.out.println("Data : "+encrypted);
        //345944483730624939484D6D37526D4171366E4344656E467471775361377A31505070776C5142456B43593D : 4511dccbea0a3350 d8ff72c7648cd678
        encrypted = "345944483730624939484D6D37526D4171366E4344656E467471775361377A31505070776C5142456B43593D";
        key = "4511dccbea0a3350";
        initVector = "d8ff72c7648cd678";
        String heData = hexaToAscii(encrypted,false);
		System.out.println(" Hexa Encrypted Data : " + heData);

		String heData1 = hexaToAscii(heData,true);
		System.out.println(" Hexa Encrypted Data : " + heData1);

		String deData = decrypt(key, initVector, heData1);
		System.out.println(deData);
		String card[] = deData.split("#");
		System.out.println(" Card : "+card[0]);
		System.out.println(" Ccv : "+card[1]);
		System.out.println(" expDate : "+card[2]);*/
        val date = Date()
        println("Date :$date")
        val df: DateFormat = SimpleDateFormat("yyyyMMdd")
        val df1: DateFormat = SimpleDateFormat("HHmmss")
        val reportDate = df.format(date)
        println("Date format : $reportDate")
        var reportDate1 = df1.format(date)
        println("Date format : $reportDate1")
        reportDate1 = "dt$reportDate1"
        println("Date format : $reportDate1")
        val reportdata = reportDate + reportDate1
        val encrypted1 = encrypt(reportdata, reportdata, "4545")
        println("Data : $encrypted1")
        val heData2 = hexaToAscii(encrypted1, false)
        println(" Hexa Encrypted Data : $heData2")
        val heData3 = hexaToAscii(heData2, true)
        println(" Hexa Encrypted Data : $heData3")
        val deData1 = decrypt(reportdata, reportdata, heData3)
        println(deData1)
    }
}
}