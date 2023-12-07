package com.example.guardianangel
import RecentUserAttributes
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.example.guardianangel.customobjects.FuzzyResponseObj
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


class Home : Fragment() {

    lateinit var stepsField: TextView
    lateinit var progressIcon: CircularProgressIndicator

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


    private val SERVER_API_KEY = BuildConfig.HEROKU_API_KEY

    val mHandler: Handler = Handler()
    private val mUiThread: Thread? = null

    fun runOnUiThread(action: Runnable) {
        if (Thread.currentThread() !== mUiThread) {
            mHandler.post(action)
        } else {
            action.run()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val progress = 0
        val maxProgress = 1000

        stepsField = view.findViewById(R.id.mainStepsCount)
        progressIcon = view.findViewById(R.id.mainProgressIndicator)

        progressIcon.setOnClickListener {
            val intent = Intent(requireContext(), StepsMonitor::class.java)
            startActivity(intent)
        }

        var totalStepsCount = 0

        GlobalScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.IO) {
                    totalStepsCount = getRecentUserAttributes()
                    stepsField.text = totalStepsCount.toString()
                    progressIcon.progress = totalStepsCount
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Card 1
        val card1 = view.findViewById<CardView>(R.id.card1)
        card1?.setOnClickListener {
            val intentCard1 = Intent(requireContext(), WeatherWelcomeActivity::class.java)
            startActivity(intentCard1)

        }
        // Card 2
        // Card 3
        val card3 = view.findViewById<CardView>(R.id.card3)
        card3?.setOnClickListener {
            val intentCard3 = Intent(requireContext(), UpdateActivity::class.java)
            startActivity(intentCard3)
        }

        barChart = view.findViewById(R.id.barChartId)

        initializeBarChart()

        hrTextView = view.findViewById(R.id.heartRateTextView)
        rrTextView = view.findViewById(R.id.respiratoryRateTextView)

        hrList = ArrayList()
        rrList = ArrayList()
        scList = ArrayList()

        getSensorData()


        // Card 4
        val card4 = view.findViewById<CardView>(R.id.card4)
        card4?.setOnClickListener {
            val intentCard4 = Intent(requireContext(), DietSuggest::class.java)
            startActivity(intentCard4)

        }

        // Card 5
        // Card 6
        val card6 = view.findViewById<CardView>(R.id.card6)
        card6?.setOnClickListener {
            val intentCard6 = Intent(requireContext(), SleepInfo::class.java)
            startActivity(intentCard6)
        }

        // Card 7
    }

    private fun getRecentUserAttributes(userId: String="655ad12b6ac4d71bf304c5eb"): Int {
        val baseUrl = "https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/users/$userId/user_attributes/recent?count=50"
        val apiKey = SERVER_API_KEY
        val client = OkHttpClient()

        var totalStepsCount = 0

        val request = Request.Builder()
            .url(baseUrl)
            .header("X-Api-Auth", apiKey)
            .method("GET", null)
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val jsonObject = JSONObject(responseBody)
                    val userAttributesArray = jsonObject.getJSONArray("user_attributes")



                    for (i in 0 until userAttributesArray.length()) {
                        val userAttribute = userAttributesArray.getJSONObject(i)
                        val stepsCount = userAttribute.getInt("steps_count")
                        totalStepsCount += stepsCount
                    }

                    println("Total Steps Count: $totalStepsCount")
                }
            }
            response.close()
        }
        return totalStepsCount
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
        val notificationManager = requireContext().getSystemService(ComponentActivity.NOTIFICATION_SERVICE)
                as NotificationManager
        val builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
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
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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