package com.example.guardianangel

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.app.NotificationCompat
import com.example.guardianangel.customobjects.FuzzyResponseObj
import com.example.guardianangel.customobjects.HealthDataObj
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class UpdateActivity: ComponentActivity() {


    private lateinit var dataTextView: TextView
    private lateinit var responseTextView: TextView
    private lateinit var customImageView: ImageView

    private val CHANNEL_ID = "CHANNEL_ID"
    private val NOTIFICATION_ID = 1

    private var heart_rate = 95
    private var respiratory_rate = 20
    private var step_count = 8000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.update_layout)
        dataTextView = findViewById(R.id.dataTextView)
        responseTextView = findViewById(R.id.responseTextView)
        customImageView = findViewById(R.id.customImageView)
        runDiagnosis()

        val backButton: Button = findViewById(R.id.backButton)

        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getRecordsFromDB(userId: String, fromTime: String, toTime: String) {
        val client = OkHttpClient()
        val url = "https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/users/" + userId + "/user_attributes?keys=heart_rate%2Crespiratory_rate%2Csteps_count&from=" + fromTime + "&to=" + toTime;

        val request = Request.Builder()
            .url(url)
            .header("accept", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    dataTextView.text = "Error: ${e.message}"
                }
            }

            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val gson = Gson()

                    val healthDataObj = gson.fromJson(responseBody, HealthDataObj::class.java)


                    runOnUiThread {
                        if (healthDataObj != null) {
                            //heart_rate = healthDataObj.average_heart_rate
                            //respiratory_rate = healthDataObj.average_respiratory_rate
                            //step_count = healthDataObj.total_steps_count
                        }
                    }
                } else {
                    runOnUiThread {
                        dataTextView.text = "Request not successful: ${response.code}"
                    }
                }
            }
        })
    }

    private fun runDiagnosis() {
        val client = OkHttpClient()
        val url = "https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/healthFuzzy?hr=" + heart_rate + "&rr=" + respiratory_rate + "&sc=" + step_count

        val request = Request.Builder()
            .url(url)
            .header("accept", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    dataTextView.text = "Error: ${e.message}"
                }
            }

            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val gson = Gson()

                    val fuzzyAnalysisObj = gson.fromJson(responseBody, FuzzyResponseObj::class.java)


                    runOnUiThread {
                        if (fuzzyAnalysisObj != null) {

                            sendNotification(fuzzyAnalysisObj.health_update)

                            setLayoutBackground(fuzzyAnalysisObj.health_update)
                            dataTextView.text = "Heart Rate - " + heart_rate + "\n" +
                                    "Respiratory Rate - " + respiratory_rate + "\n" +
                                    "Step Count - " + step_count + "\n"
                            responseTextView.text = fuzzyAnalysisObj.health_update
                        }
                    }
                } else {
                    runOnUiThread {
                        dataTextView.text = "Request not successful: ${response.code}"
                    }
                }
            }
        })
    }

    private fun sendNotification(health_update: String) {
        createNotificationChannel()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE)
                as NotificationManager
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle("Your Health Update")
            .setContentText(health_update)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun createNotificationChannel() {
        val name = "MY_CHANNEL"
        val descriptionText = "Health Monitoring Notification Channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun setLayoutBackground(fuzzyResponse: String) {
        customImageView.setVisibility(View.VISIBLE)
        if (fuzzyResponse.contains("sleep", ignoreCase = true)) {
            customImageView.setImageResource(R.drawable.check)
        } else if (fuzzyResponse.contains("critical", ignoreCase = true)) {
            customImageView.setImageResource(R.drawable.ambulance)
        } else {
            customImageView.setImageResource(R.drawable.general)
        }
    }
}