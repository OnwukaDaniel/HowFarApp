package com.azur.howfar.workManger

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.azur.howfar.R
import com.azur.howfar.activity.ActivityEnterOTP
import com.azur.howfar.activity.OnBoardingType
import com.azur.howfar.model.SendPhoneOTPResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.azur.howfar.models.UserProfile
import com.azur.howfar.models.VideoPost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException

class VideoPostWorkManager(private val context: Context, private val params: WorkerParameters) : Worker(context, params) {
    private var user = FirebaseAuth.getInstance().currentUser
    private var timeRef = FirebaseDatabase.getInstance().reference
    private var scope = CoroutineScope(Dispatchers.IO)
    private var videoPost = VideoPost()
    private lateinit var pref: SharedPreferences
    private val BASE_URL = "https://howfar.up.railway.app/api/"

    override fun doWork(): Result {
        pref = context.getSharedPreferences(context.getString(R.string.ALL_PREFERENCE), Context.MODE_PRIVATE)
        val json = params.inputData.getString("json")
        videoPost = Gson().fromJson(json, VideoPost::class.java)
        storeImage()
        return Result.success()
    }

    private fun storeImage() {
        timeRef = FirebaseDatabase.getInstance().reference.child("time").child(user!!.uid)
        timeRef.setValue(ServerValue.TIMESTAMP).addOnSuccessListener {
            timeRef.get().addOnSuccessListener { timeSnapshot ->
                if (timeSnapshot.exists()) {
                    val timeSent = timeSnapshot.value.toString()
                    videoPost.timePosted = timeSent
                    val imageRef = FirebaseStorage.getInstance().reference.child(VIDEO_POST).child(user!!.uid).child(timeSent)
                    val imageUploadTask = imageRef.putFile(Uri.fromFile(File(videoPost.videoUrl))!!)

                    imageUploadTask.continueWith { task ->
                        if (!task.isSuccessful) task.exception?.let { it ->
                            throw  it
                        }
                        imageRef.metadata.addOnSuccessListener {
                            imageRef.downloadUrl.addOnSuccessListener { uri ->
                                videoPost.videoUrl = uri.toString()
                                //uploadVideo()
                            }.addOnFailureListener {
                                Toast.makeText(context, "Video upload failed!!! Retry", Toast.LENGTH_LONG).show()
                                return@addOnFailureListener
                            }
                        }
                    }
                }
            }
        }
    }

    /*private fun uploadVideo() {
        scope.launch {
            val client = OkHttpClient()

            val mediaType = "application/json".toMediaTypeOrNull()
            val requestBody = Gson().toJson(input).toRequestBody(mediaType)

            val request = Request.Builder()
                .url(BASE_URL + "video-reel")
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println("onFailure *************************** ${e.message}")

                        val dialog = AlertDialog.Builder(this)
                        dialog.setTitle("Message")
                        dialog.setMessage("Error, \nPlease retry")
                        dialog.setPositiveButton("Ok") { d, _ ->
                            d.dismiss()
                        }
                        dialog.create().show()
                }

                override fun onResponse(call: Call, response: okhttp3.Response) {
                    val responseBody = response.body?.string()
                    println("onResponse 1 *************************** $responseBody")
                    println("onResponse 2 *************************** $dataResponse")
                    if (dataResponse.success) {
                        val intent = Intent(applicationContext, ActivityEnterOTP::class.java)
                        val json = Gson().toJson(registerEmail)
                        intent.putExtra("data", json)
                        intent.putExtra("OnBoardingType", OnBoardingType.sign_in)
                        intent.putExtra("type", "phone")
                        startActivity(intent)
                        overridePendingTransition(R.anim.enter_right_to_left, R.anim.exit_right_to_left)
                    } else {
                        badStatus2(dataResponse)
                    }
                }
            })
        }
        val ref = FirebaseDatabase.getInstance().reference.child(USER_DETAILS).child(user!!.uid)
        ref.get().addOnSuccessListener { userProfile ->
            if (userProfile.exists()) {
                val myProfile = userProfile.getValue(UserProfile::class.java)!!
                videoPost.profileImage = myProfile.image
                videoPost.profileName = myProfile.name
                if (videoPost.timePosted == "") {
                    timeRef = FirebaseDatabase.getInstance().reference.child("time").child(user!!.uid)
                    timeRef.setValue(ServerValue.TIMESTAMP).addOnSuccessListener {
                        timeRef.get().addOnSuccessListener { snapshot ->
                            videoPost.timePosted = snapshot.value.toString()
                            if (snapshot.exists()) upload()
                        }
                    }
                } else upload()
            }
        }
    }*/

    private fun upload() {
        val refM = FirebaseDatabase.getInstance().reference.child(VIDEO_POST).child("-${videoPost.timePosted}")
        val postRecord = FirebaseDatabase.getInstance().reference.child(VIDEO_POST_RECORD).child(user!!.uid).child(videoPost.timePosted)
        refM.setValue(videoPost).addOnSuccessListener {
            postRecord.setValue(videoPost)
            Toast.makeText(context, "Video post successfully uploaded", Toast.LENGTH_LONG).show()
        }.addOnFailureListener {
            Toast.makeText(context, "Video post Failed. Please retry", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val MOMENT_DATA = "MOMENT_DATA"
        const val PERSONAL_POST_RECORD = "PERSONAL_POST_RECORD"
        const val VIDEO_POST_RECORD = "VIDEO_POST_RECORD"
        const val VIDEO_POST = "VIDEO_POST"
        const val USER_DETAILS = "user_details"
    }
}