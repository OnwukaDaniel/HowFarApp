package com.azur.howfar.newtwork_viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.azur.howfar.model.StatusData
import com.flutterwave.raveandroid.RavePayActivity.BASE_URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.util.*


class StatusVM : ViewModel() {
    private var scope = CoroutineScope(Dispatchers.IO)
    val responseData = MutableLiveData<String>()

    fun createPost(statusData: StatusData) {
        scope.launch {
            println("Status init ***************************************")
            val client = OkHttpClient()
            val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("serverTime", "")
                .addFormDataPart("storageLink", "")
                .addFormDataPart("statusType", "1")
                .addFormDataPart("caption", statusData.caption)
                .addFormDataPart("timeSent", Calendar.getInstance().timeInMillis.toString())
                .addFormDataPart("captionBackgroundColor", statusData.captionBackgroundColor)
                .addFormDataPart("statusDeliveryType", "")
                .addFormDataPart(
                    "image",
                    "image.jpg",
                    File(statusData.image).asRequestBody("application/octet-stream".toMediaTypeOrNull())
                )
                .build()
            val request: Request = Request.Builder()
                .url("$BASE_URL/status")
                .method("POST", body)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println("onFailure *************************** ${e.message}")
                    //runOnUiThread { progressDialog.dismiss() }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    println("onFailure *************************** $responseBody")
                    /*val dataResponse = Gson().fromJson(responseBody, SuccessAccountCreation::class.java)
                    if (responseBody.) {
                    } else {
                        println("onFailure *************************** ${e.message}")
                    }*/
                }
            })
        }
    }

}