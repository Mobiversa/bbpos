package com.mobiversa.ezy2pay.network

import com.google.gson.GsonBuilder
import com.mobiversa.ezy2pay.BuildConfig
import com.mobiversa.ezy2pay.network.response.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface ApiService {

    @POST("payment/mobiapr19/mobi_jsonservice/")
    fun login(@Body postParam: HashMap<String, String>): Call<LoginResponse>

    @POST("payment/mobiapr19/mobi_jsonservice/")
    fun validateUser(@Body postParam: HashMap<String, String>): Call<UserValidation>

    @POST("payment/mobiapr19/mobi_jsonservice/")
    fun getOTP(@Body postParam: HashMap<String, String>): Call<OTPResponse>

    @POST("payment/mobiapr19/mobi_jsonservice/")
    fun sendUserDetail(@Body postParam: HashMap<String, String>): Call<RegisterDetailResponse>

    @POST("payment/mobiapr19/mobi_jsonservice/")
    fun verifyUserDetail(@Body postParam: HashMap<String, String>): Call<VerifyUserModel>

    //Notification List
    @POST("payment/{linkValue}")
    fun getQRJson(@Path("linkValue", encoded = true) linkValue: String, @Body params: @JvmSuppressWildcards Map<String, Any>): Call<QRModel>

    //Notification List
    @POST("notifications")
    fun getNotificationList(@Body params: @JvmSuppressWildcards Map<String, Any>): Call<NotificationList>

    @POST("notifications")
    fun getNotificationTask(@Body params: @JvmSuppressWildcards Map<String, Any>): Call<SuccessModel>

    @POST("payment/mobiapr19/mobi_jsonservice/")
    fun getProductList(@Body postParam: HashMap<String, String>): Call<ProfileProductList>

    @POST("payment/mobiapr19/mobi_jsonservice/")
    fun getCountryList(@Body postParam: HashMap<String, String>): Call<CountryList>

    @POST("payment/{linkValue}/")
    fun getEzyMotoRecPayment(@Path("linkValue", encoded = true) linkValue: String,@Body postParam: HashMap<String, String>): Call<EzyMotoRecPayment>

    @POST("payment/mobihotelapi/jsonservice/")
    fun getEzyMotoExternalPayment(@HeaderMap headerParam: HashMap<String, String>, @Body postParam: HashMap<String, String>): Call<EzyMotoRecPayment>

    @POST("payment/mobiapr19/mobi_jsonservice/")
    fun getKeyInjection(@Body postParam: HashMap<String, String>): Call<KeyInjectModel>

    @POST("payment/mobiapr19/mobi_jsonservice/")
    fun getPaymentInfo(@Body postParam: HashMap<String, String>): Call<PaymentInfoModel>

    @POST("payment/mobiapr19/mobi_jsonservice/")
    fun getCallAckModel(@Body postParam: HashMap<String, String>): Call<CallAckModel>

    @POST("payment/mobiapr19/mobi_jsonservice/")
    fun getDeclineNotifyData(@Body postParam: HashMap<String, String>): Call<SendDeclineNotifyModel>

    @POST("payment/mobiapr19/mobi_jsonservice/")
    fun getTransactionHistory(@Body postParam: HashMap<String, String>): Call<TransactionHistoryModel>

    @POST("payment/mobiapr19/mobi_jsonservice/")
    fun getUserValidation(@Body postParam: HashMap<String, String>): Call<SuccessModel>

    @POST("payment/mobilite/jsonservice/")
    fun geMobiLiteBalance(@Body postParam: HashMap<String, String>): Call<SuccessModel>

    @POST("payment/{linkValue}/mobi_jsonservice/")
    fun setVoidTransaction(@Path("linkValue", encoded = true) linkValue: String, @Body postParam: HashMap<String, String>): Call<VoidHistoryModel>

    @POST("payment/mobiapr19/mobi_jsonservice/")
    fun setMobiCash(@Body postParam: HashMap<String, String>): Call<SuccessModel>

    @POST("payment/mobiapr19/mobi_jsonservice/")
    fun getSettlement(@Body postParam: HashMap<String, String>): Call<SuccessModel>

    @POST("payment/mobiapr19/mobi_jsonservice/")
    fun setSale(@Body postParam: HashMap<String, String>): Call<SuccessModel>

    @POST("payment/mobiapr19/mobi_jsonservice/")
    fun getReceiptData(@Body postParam: HashMap<String, String>): Call<ReceiptModel>

    @POST("payment/mobiapr19/mobi_jsonservice/")
    fun registerUser(@Body postParam: HashMap<String, String>): Call<SuccessModel>

    @POST("externalapi/merchantservice/")
    fun registerLiteUser(@Body postParam: HashMap<String, String>): Call<SuccessModel>

    @POST("externalapi/merchantservice/")
    fun upgradeUser(@Body postParam: HashMap<String, String>): Call<SuccessModel>

    @POST("payment/mobiapr19/mobi_jsonservice")
    fun logoutUser(@Body postParam: HashMap<String, String>): Call<SuccessModel>

    @POST("payment/mobiapr19/mobi_jsonservice/")
    fun updateMerchant(@Body postParam: HashMap<String, String>): Call<SuccessModel>

    @POST("payment/mobiapr19/mobi_jsonservice/")
    fun getMerchantDetails(@Body postParam: HashMap<String, String>): Call<MerchantDetailModel>

    @POST("api/fpx")
    fun getBankList( @Body postParam: HashMap<String, String>): Call<BankListModel>

    @POST("api/CountryAndState")
    fun getStateList( @Body postParam: HashMap<String, String>): Call<StateModel>

    companion object RetrofitClient {
        private const val BANK_URL = "https://fpx.mobiversa.com/"
        private const val CITY_URL = "https://ocsservices.mobiversa.com/"
//        private const val BASE_URL = "https://paydee.gomobi.io/"
//        private const val REGISTER_URL = "https://paydee.gomobi.io/"
//        private const val NOTIFICATION_URL = "https://paydee.gomobi.io/notificationservices/"
//        private const val BASE_URL = "https://ecom.gomobi.io/"
//        private const val REGISTER_URL = "https://ecom.gomobi.io/"
//        private const val NOTIFICATION_URL = "https://ecom.gomobi.io/notificationservices/"
        private const val BASE_URL = "https://pay.gomobi.io/"
        private const val REGISTER_URL = "https://pay.gomobi.io/"
        private const val NOTIFICATION_URL = "https://pay.gomobi.io/notificationservices/"

        private val client = OkHttpClient().newBuilder()
            .followRedirects(false)
            .followSslRedirects(false)
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {level =
                    if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE}).build()
        private val gson = GsonBuilder()
            .setLenient()
            .create()

        fun serviceRequest(): ApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build().create(ApiService::class.java)
        }
        fun externalRequest(): ApiService {
            return Retrofit.Builder()
                .baseUrl(REGISTER_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build().create(ApiService::class.java)
        }
        fun bankRequest(): ApiService {
            return Retrofit.Builder()
                .baseUrl(BANK_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build().create(ApiService::class.java)
        }
        fun stateRequest(): ApiService {
            return Retrofit.Builder()
                .baseUrl(CITY_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build().create(ApiService::class.java)
        }

        fun notificationRequest(): ApiService {
            return Retrofit.Builder()
                .baseUrl(NOTIFICATION_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build().create(ApiService::class.java)
        }
    }
}