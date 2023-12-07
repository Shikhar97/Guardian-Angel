package com.example.guardianangel.sleep_wellness

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.guardianangel.BuildConfig
import com.example.guardianangel.MainActivity
import com.example.guardianangel.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.gson.annotations.SerializedName
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale


class SleepWellnessStats: AppCompatActivity() {
    private val tag = "Angel"
    private lateinit var barChart: BarChart
    private lateinit var tabLayout: TabLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var graphTitle: TextView
    private val serverApiKey = BuildConfig.HEROKU_API_KEY
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sleep_wellness_stats)
        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)

        barChart = findViewById(R.id.SleepWellnessBarChart)
        val defaultTabPosition = 0
        initializeBarChart(defaultTabPosition)

        tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.GONE

        graphTitle = findViewById(R.id.graphTitle)
        graphTitle.text = "Hourly Breakdown of Sleep Duration (Yesterday)"

        tabLayout.getTabAt(0)?.select()
        handleTabClick(tabLayout.getTabAt(0)!!, progressBar, graphTitle)

        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // Handle tab selected event
                handleTabClick(tab, progressBar, graphTitle)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // Handle tab unselected event
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // Handle tab reselected event
            }
        })

        topAppBar.setNavigationOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.help -> {
                    // Handle more item (inside overflow menu) press
                    true
                }
                else -> false
            }
        }
    }

    private fun handleTabClick(tab: TabLayout.Tab, progressBar: ProgressBar, graphTitle: TextView) {
        // Implement the behavior when a tab is clicked
        barChart.clear()
//        barChart.clearValues()
        barChart.data = null
        barChart.notifyDataSetChanged()
        when (tab.position) {
            0, 1 -> {
                // Initialize the bar chart based on the selected tab
                initializeBarChart(tab.position)
                graphTitle.text = if (tab.position == 0) "Hourly Breakdown of Sleep Duration (Yesterday)" else "Sleep Duration Over the Past 7 Days"
                requestSleepStats(if (tab.position == 0) "hour" else "day", progressBar)
            }
            // Add more cases if you have additional tabs
        }
    }

    private fun initializeHourlyBarChart() {
        val xAxis: XAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawLabels(true)
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(false)

        xAxis.axisMinimum = -1f

        xAxis.valueFormatter = HourAxisValueFormatter()

        barChart.axisLeft.setDrawLabels(true)
        barChart.axisLeft.setDrawAxisLine(false)
        barChart.axisLeft.setDrawGridLines(false)

        barChart.axisRight.setDrawLabels(false)
        barChart.axisRight.setDrawAxisLine(false)
        barChart.axisRight.setDrawGridLines(false)

        barChart.axisLeft.axisMinimum = 0f
        barChart.axisRight.axisMinimum = 0f
        barChart.setDrawGridBackground(false)
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
    }

    private fun initializeDailyBarChart() {
        println("Inside initializeDailyBarChart")
        val xAxis: XAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawLabels(true)
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(false)

        xAxis.axisMinimum = -0.5f

        xAxis.valueFormatter = DateAxisValueFormatter()

        barChart.axisLeft.setDrawLabels(true)
        barChart.axisLeft.setDrawAxisLine(false)
        barChart.axisLeft.setDrawGridLines(false)

        barChart.axisRight.setDrawLabels(false)
        barChart.axisRight.setDrawAxisLine(false)
        barChart.axisRight.setDrawGridLines(false)

        barChart.axisLeft.axisMinimum = 0f
        barChart.axisRight.axisMinimum = 0f
        barChart.setDrawGridBackground(false)
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
    }

    class DateAxisValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val yesterday = LocalDate.now().minusDays(1)
           // val yesterday = LocalDate.of(2023, 12, 2)
            val currentDate = yesterday.minusDays(6L-value.toLong())
            val dateFormat = DateTimeFormatter.ofPattern("MM/dd", Locale.getDefault())
            return dateFormat.format(currentDate)
        }
    }

    private fun initializeBarChart(tabPosition: Int) {
        println("tabPosition $tabPosition")
        barChart.clear()
//        barChart.clearValues()
        barChart.data = null
        barChart.notifyDataSetChanged()
        when (tabPosition) {
            0 -> initializeHourlyBarChart()
            1 -> initializeDailyBarChart()
            // Add more cases if you have additional tabs
        }
    }

    class HourAxisValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val hour = value.toInt() % 24
            return String.format("%02d:00", hour)
        }
    }

    private fun requestSleepStats(groupBy: String, progressBar: ProgressBar) {
        progressBar.visibility = View.VISIBLE
        val client = OkHttpClient()
        val userId = "655ad12b6ac4d71bf304c5eb"
        // Get yesterday's date
        val yesterday = LocalDate.now().minusDays(1)
        // val yesterday = LocalDate.of(2023, 12, 2)

        // Format the date to match your API's format
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val startDate: String
        val endDate: String

        if (groupBy == "hour") {
            startDate = yesterday.atStartOfDay().format(dateFormatter)
            print("startDate $startDate")
            endDate = yesterday.atTime(23, 59, 59).format(dateFormatter)
        } else {
            val sevenDaysAgo = yesterday.minusDays(6)
            startDate = sevenDaysAgo.atStartOfDay().format(dateFormatter)
            endDate = yesterday.atTime(23, 59, 59).format(dateFormatter)
        }


        val url =
            "https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/users/$userId/user_attributes" +
                    "?keys=sleep&from=$startDate&to=$endDate&group_by=$groupBy&static_keys=yes"

        val request = Request.Builder()
            .url(url)
            .header("accept", "application/json")
            .header("X-Api-Auth", serverApiKey)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    // Handle failure, update UI or show an error message
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    println("Response Body $responseBody")

                    try {
                        when (groupBy) {
                            "hour" -> processSleepStatsbyHour(responseBody)
                            "day" -> processSleepStatsbyDay(responseBody)
                        }

                    } catch (e: JSONException) {
                        e.printStackTrace()
                        runOnUiThread {
                            progressBar.visibility = View.GONE
                            // Handle JSON parsing error
                        }
                    }
                } else {
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        println("Request not successful: ${response.code}")
                        // Handle unsuccessful response, update UI or show an error message
                    }
                }
            }

            private fun processSleepStatsbyHour(responseBody: String?) {
                val jsonArray = JSONArray(responseBody)

                if (jsonArray.length() > 0) {
                    val sleepStatsObject =
                        jsonArray.getJSONObject(0) // Assuming there's only one element in the array
                    val sleepDataArray = sleepStatsObject.getJSONArray("data")
//                            val currentDate = sleepStatsObject.getString("date")

                    val barEntriesList = ArrayList<BarEntry>()

                    for (i in 0 until sleepDataArray.length()) {
                        val sleepDataObject = sleepDataArray.getJSONObject(i)
                        val hour = sleepDataObject.getInt("hour").toFloat()
                        val sleepTime =
                            sleepDataObject.getJSONObject("metrics").getInt("sleep_time")
                                .toFloat() / 60

                        barEntriesList.add(BarEntry(hour, sleepTime))
                    }

                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        val darkPurpleColor = ContextCompat.getColor(barChart.context, R.color.dark_purple)

                        // Update your UI or chart here using barEntriesList
                        val barDataSet = BarDataSet(barEntriesList, "Sleep Time").apply {
                            color = darkPurpleColor
                            setDrawValues(false)
                        }
                        // Create BarData with the BarDataSet
                        val barData = BarData(barDataSet)

                        // Set data to the bar chart
                        barChart.data = barData

                        // Refresh the chart
                        barChart.invalidate()
                    }
                }
            }

            private fun processSleepStatsbyDay(responseBody: String?) {
                try {
                    val jsonArray = JSONArray(responseBody)

                    if (jsonArray.length() > 0) {
                        val barEntriesList = ArrayList<BarEntry>()

                        for (i in 0 until jsonArray.length()) {
                            val sleepDataObject = jsonArray.getJSONObject(i)
                            val xValue = i.toFloat() // Assuming x-axis values are integers for days
                            // Create a mapping bw idx and the dates and replace the date while formatting
                            val sleepTime =
                                sleepDataObject.getJSONObject("metrics").getInt("sleep_time").toFloat() / 60 / 60
                            println("xValue $xValue sleepTime $sleepTime")
                            barEntriesList.add(BarEntry(xValue, sleepTime))
                        }

                        runOnUiThread {
                            progressBar.visibility = View.GONE
                            val darkPurpleColor = ContextCompat.getColor(barChart.context, R.color.dark_purple)

                            // Update your UI or chart here using barEntriesList
                            val barDataSet = BarDataSet(barEntriesList, "Sleep Time").apply {
                                color = darkPurpleColor
                                setDrawValues(false)
                            }
                            // Create BarData with the BarDataSet
                            val barData = BarData(barDataSet)

                            // Set data to the bar chart
                            barChart.data = barData

                            // Refresh the chart
                            barChart.invalidate()
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    runOnUiThread {
                        // Handle JSON parsing error
                    }
                }
            }
        })

    }

}
