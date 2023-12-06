package com.example.guardianangel

import RecentUserAttributes
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.app.NotificationCompat
import com.example.guardianangel.customobjects.FuzzyResponseObj
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class HealthStats : ComponentActivity() {

    private lateinit var barChart: BarChart
    private lateinit var hrTextView: TextView
    private lateinit var rrTextView: TextView
    private val CHANNEL_ID = "CHANNEL_ID"
    private val NOTIFICATION_ID = 1
    private lateinit var intent: Intent

    private lateinit var hrList: ArrayList<Int>
    private lateinit var rrList: ArrayList<Int>
    private lateinit var scList: ArrayList<Int>
    private lateinit var barEntriesList: ArrayList<BarEntry>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_home)

        val healthCardView: MaterialCardView = findViewById(R.id.card3)
        barChart = findViewById(R.id.barChartId)

        intent = Intent(this, UpdateActivity::class.java)
        healthCardView.setOnClickListener {
            startActivity(intent)
        }

        initializeBarChart()

        hrTextView = findViewById(R.id.heartRateTextView)
        rrTextView = findViewById(R.id.respiratoryRateTextView)

        hrList = ArrayList()
        rrList = ArrayList()
        scList = ArrayList()

        getSensorData()
    }

    private fun getSensorData(): ArrayList<Int> {
        val client = OkHttpClient()
        val userId = "655ad12b6ac4d71bf304c5eb"

        var hr_list = ArrayList<Int>()
        val url =
            "https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/users/" + userId + "/user_attributes/recent?count=7"

        val request = Request.Builder()
            .url(url)
            .header("accept", "application/json")
            .header("X-Api-Auth", "<api_key>")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    hrTextView.text = "Error: ${e.message}"
                }
            }

            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val userAttributesList =
                        Gson().fromJson(responseBody, RecentUserAttributes::class.java)
                    if (userAttributesList.userAttributes.isNotEmpty()) {
                        val userAttributes = userAttributesList.userAttributes
                        userAttributes.forEach { healthData ->
                            hrList.add(healthData.heartRate)
                            rrList.add(healthData.respiratoryRate)
                            scList.add(healthData.stepsCount)
                        }
                    }

                    hr_list = hrList
                    runOnUiThread {
                        setHealthData(hrList[0], rrList[0])
                        runDiagnosis(hrList[0], rrList[0], estimateStepCount(scList))
                        getHeartRateTrends(hrList)
                    }
                } else {
                    runOnUiThread {
                        hrTextView.text = "Request not successful: ${response.code}"
                    }
                }
            }
        })
        return hr_list
    }

    private fun runDiagnosis(heartRatVal: Int, respiratoryRateVal: Int, stepCountVal: Int) {
        val client = OkHttpClient()
        val url =
            "https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/healthFuzzy?hr=" + heartRatVal + "&rr=" + respiratoryRateVal + "&sc=" + stepCountVal

        val request = Request.Builder()
            .url(url)
            .header("accept", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    hrTextView.text = "Error: ${e.message}"
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
                            intent.putExtra("HEALTH_UPDATE_KEY", fuzzyAnalysisObj.health_update)

                            if (!fuzzyAnalysisObj.health_update.contains(
                                    "normal",
                                    ignoreCase = true
                                )
                            ) {
                                sendNotification(fuzzyAnalysisObj.health_update)
                            }
                        }
                    }
                } else {
                    runOnUiThread {
                        hrTextView.text = "Request not successful: ${response.code}"
                    }
                }
            }
        })
    }

    private fun estimateStepCount(srList: ArrayList<Int>) : Int {
        val stepCount = ((srList.sum() / srList.size) * 6 * 24)
        return stepCount
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


    private fun getHeartRateTrends(hrList: ArrayList<Int>) {

        lateinit var barData: BarData
        lateinit var barDataSet: BarDataSet

        barEntriesList = ArrayList()

        val colors = ArrayList<Int>()

        hrList.asReversed().forEachIndexed { index, hrVal ->
            barEntriesList.add(BarEntry((index + 1).toFloat(), hrVal.toFloat()))
            colors.add(Color.GRAY)
        }

        colors[colors.size - 1] = Color.rgb(255, 165, 0)

        barDataSet = BarDataSet(barEntriesList, "Heart Rate Trend Over the last hour")
        barData = BarData(barDataSet)
        barChart.data = barData
        barDataSet.valueTextColor = Color.TRANSPARENT

        barDataSet.colors = colors
        barDataSet.valueTextSize = 16f
        barChart.description.isEnabled = false
        barChart.notifyDataSetChanged()
        barChart.invalidate()
    }

    private fun setHealthData(heartRate: Int, respiratoryRate: Int) {
        hrTextView.text = heartRate.toString() + " beats/min"
        rrTextView.text = respiratoryRate.toString() + " breaths/min"
    }

    private fun initializeBarChart() {
        val xAxis: XAxis = barChart.xAxis
        xAxis.setDrawLabels(false)
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(false)

        barChart.axisLeft.setDrawLabels(false)
        barChart.axisLeft.setDrawAxisLine(false)
        barChart.axisLeft.setDrawGridLines(false)

        barChart.axisRight.setDrawLabels(false)
        barChart.axisRight.setDrawAxisLine(false)
        barChart.axisRight.setDrawGridLines(false)
        barChart.setDrawGridBackground(false)
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
    }
}