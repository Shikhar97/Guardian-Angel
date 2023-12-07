package com.example.guardianangel

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Calendar
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

class StepsMonitor : AppCompatActivity() {
    lateinit var stepsField: TextView
    lateinit var goalField: TextView
    lateinit var goalButton: Button
    lateinit var remainderButton: Button
    lateinit var suggestionsButton: Button
    lateinit var progressIcon: CircularProgressIndicator

    private lateinit var alarmManager: AlarmManager

    private lateinit var pendingIntent: PendingIntent

    lateinit var barChart: BarChart

    lateinit var barData: BarData

    lateinit var barDataSet: BarDataSet

    lateinit var barEntriesList: ArrayList<BarEntry>

    val progress = 0
    val maxProgress = 4000

    private val SERVER_API_KEY = BuildConfig.HEROKU_API_KEY

    private val gson = Gson()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_steps_monitor)
        createNotificationChannel(this)
        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)

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

        // Set the alarm manager and pending intent
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        stepsField = this.findViewById(R.id.stepsCount)
        goalField = this.findViewById(R.id.goalField)
        remainderButton = this.findViewById(R.id.remainderButton)
        goalButton = this.findViewById(R.id.goalButton)
        suggestionsButton = this.findViewById(R.id.suggestionsButton)
        progressIcon = findViewById(R.id.progressIndicator)

        progressIcon.max =
            maxProgress
        progressIcon.progress =
            progress

        goalButton.setOnClickListener {
            Log.d(TAG, "Inside goal button")
            showNumberDialog()
        }

        remainderButton.setOnClickListener {
            if (isNumeric(goalField.text.toString())) {
                showTimeDialog()
            } else {
                showSetGoalWarningDialog()
            }

        }

        suggestionsButton.setOnClickListener {
            Log.d(TAG, "Inside goal button")
            showSuggestionsActivity()
        }

        barChart = findViewById(R.id.idBarChart)


        GlobalScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.IO) {
                    getRecentUserAttributes()
                    getGoal()
                    getUserAttributes()
                }

                setupBarChart()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupBarChart() {

        println(barEntriesList)

        barDataSet = BarDataSet(barEntriesList, "Steps Data")
        barData = BarData(barDataSet)
        barChart.data = barData

        barDataSet.valueTextColor = Color.BLACK
        barDataSet.color = resources.getColor(R.color.purple)
        barDataSet.valueTextSize = 7f

        barChart.description.isEnabled = false
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.axisLeft.isEnabled = false
        barChart.axisRight.isEnabled = false

        barChart.setFitBars(true)

        barChart.invalidate()
    }

    private fun convertDateStringToTimestamp(dateString: String): Float {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = format.parse(dateString)
        return SimpleDateFormat("dd", Locale.getDefault()).format(date).toFloat()
    }

    private fun getRecentUserAttributes(userId: String="655ad12b6ac4d71bf304c5eb") {
        val baseUrl = "https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/users/$userId/user_attributes/recent?count=30"
        val apiKey = SERVER_API_KEY
        val client = OkHttpClient()

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

                    var totalStepsCount = 0

                    for (i in 0 until userAttributesArray.length()) {
                        val userAttribute = userAttributesArray.getJSONObject(i)
                        val stepsCount = userAttribute.getInt("steps_count")
                        totalStepsCount += stepsCount
                    }

                    println("Total Steps Count: $totalStepsCount")

                    lifecycleScope.launch {
                        stepsField.text = totalStepsCount.toString()
                        progressIcon.progress = totalStepsCount
                    }
                }
            }
            response.close()
        }
    }

    private fun getUserAttributes(userId: String="655ad12b6ac4d71bf304c5eb"): String {
        var username : String = "n/a"
        val baseUrl = "https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/users/$userId/user_attributes?keys=steps_count&from=2023-11-30T00%3A00%3A00Z&to=2023-11-30T23%3A59%3A59Z&group_by=hour"
        val apiKey = SERVER_API_KEY
        val client = OkHttpClient()

        barEntriesList = ArrayList()

        val request = Request.Builder()
            .url(baseUrl)
            .header("X-Api-Auth", apiKey)
            .method("GET", null)
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    responseBody?.let {
                        val jsonArray = JSONArray(it)

                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)

                            // Assuming there's only one key in each inner object
                            val dateKey = jsonObject.keys().next()

                            val innerObject = jsonObject.getJSONObject(dateKey)

                            val timeKeys = innerObject.keys()
                            while (timeKeys.hasNext()) {
                                val timeKey = timeKeys.next()
                                val stepsCount = innerObject.getJSONObject(timeKey).getInt("total_steps_count").toFloat()
                                if(stepsCount != 0f) {
                                    barEntriesList.add(BarEntry(timeKey.toFloat(), stepsCount))
                                }
                            }

//                            val stepsCount = innerObject.getInt("total_steps_count").toString()
//                            stepsArray.add(stepsCount)
//                            val timestamp = convertDateStringToTimestamp(dateKey)
//                            barEntriesList.add(BarEntry(timestamp, stepsCount.toFloat()))
                        }
                    }
                }
            } else {
                Log.i(TAG, "Request failed with code: ${response.code}")
            }
            response.close()

        }
        return username
    }
    private fun isNumeric(value: String): Boolean {
        return value.toDoubleOrNull() != null
    }

    private fun showSuggestionsActivity() {
        val intent = Intent(this, WalkingSuggestionsActivity::class.java)
        startActivity(intent)
    }

    private fun showSetGoalWarningDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Warning!")
            .setMessage("Set your daily goal first to set remainders")
            .setPositiveButton("OK") { dialog, which ->

            }
            .show()
    }
    private fun showNumberDialog() {
        Log.d(TAG, "inside")
        val dialogView = layoutInflater.inflate(R.layout.number_dialog_layout, null)
        val numberField = dialogView.findViewById<EditText>(R.id.numberField)

        MaterialAlertDialogBuilder(this)
            .setTitle("Enter your goal")
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, which ->
                val enteredNumber = numberField.text.toString()
                Log.d(TAG, enteredNumber)
                goalField.text = enteredNumber
                goalButton.text = "Update Goal"

                updateGoal(enteredNumber)
            }
            .setNegativeButton("Cancel") { dialog, which ->
            }
            .show()
    }

    private fun getGoal(userId: String = "655ad12b6ac4d71bf304c5eb") {
        val baseUrl = "https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/users/$userId"
        val client = OkHttpClient()
        lifecycleScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url(baseUrl)
                .header("X-Api-Auth", SERVER_API_KEY)
                .method("GET", null)
                .build()

            coroutineScope {

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body
                    val responseText = responseBody?.string()
                    val jsonObject = gson.fromJson(responseText, JsonObject::class.java)
                    Log.i(TAG, jsonObject.toString())

                    val stepGoal = jsonObject.get("step_goal")
                    Log.d("stpes", stepGoal.toString())
                    if (stepGoal != null) {
                        lifecycleScope.launch {
                            goalField.text = stepGoal.asString
                            progressIcon.max = stepGoal.asInt
                        }
                    }
                } else {
                    Log.i(TAG, "Request failed with code: ${response.code}")
                }
                response.close()
            }
        }
    }

    private fun updateGoal(enteredNumber: String, userId: String = "655ad12b6ac4d71bf304c5eb") {
        val baseUrl = "https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/users/$userId"
        val client = OkHttpClient()

        val jsonBody = JSONObject()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        jsonBody.put("step_goal", enteredNumber)

        val requestBody = jsonBody.toString().toRequestBody(mediaType)
        lifecycleScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url(baseUrl)
                .header("X-Api-Auth", SERVER_API_KEY)
                .post(requestBody)
                .build()

            coroutineScope {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    Log.i(TAG, "Attributes updated")
                }
                else {
                    Log.i(TAG, "Request failed with code: ${response.code}")
                }
                response.close()

            }
        }
    }

    private fun showTimeDialog() {
        val timeView = layoutInflater.inflate(R.layout.remainder_date_picker, null)
        val timeField = timeView.findViewById<TimePicker>(R.id.timePicker)

        MaterialAlertDialogBuilder(this)
            .setTitle("Set the time for goal remainders")
            .setView(timeView)
            .setPositiveButton("OK") { dialog, which ->
                var hour = timeField.hour
                var minute = timeField.minute

                var hourFlag = (timeField.hour < 12)

                val formattedTime = "%02d:%02d".format(hour, minute)
                Log.d(TAG, formattedTime.toString())

                val notificationIntent = Intent(this, AlarmReceiver::class.java)
                    .putExtra("stepsToday", stepsField.text)
                    .putExtra("goal", goalField.text)

                pendingIntent = PendingIntent.getBroadcast(this,
                    StepsMonitor.ALARM_REQUEST_CODE, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE)

                // Set the alarm for the selected time
                setAlarm(formattedTime, hourFlag)
            }
            .setNegativeButton("Cancel") { dialog, which ->
            }
            .show()
    }


    @SuppressLint("WrongConstant", "ServiceCast")
    private fun createNotificationChannel(context: Context) {
        // Create the notification channel with the required properties
        val channelId = "alarm_channel"
        val channelName = "Alarm Channel"
        val importance = NotificationManagerCompat.IMPORTANCE_HIGH

        val channel = NotificationChannel(channelId, channelName, importance)
        channel.description = "Alarm notification channel"

        val notificationManager =  NotificationManagerCompat.from(context)
        notificationManager.createNotificationChannel(channel)
    }

    @SuppressLint("ScheduleExactAlarm", "SuspiciousIndentation")
    private fun setAlarm(selectedTime: String, hourFlag: Boolean) {
        // Parse the selected time to a Calendar object
        val calendar = Calendar.getInstance()
        val (hour, minute) = selectedTime.split(":").map { it.toInt() }
        Log.d(TAG, calendar.timeInMillis.toString())
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        if(!hourFlag)
            calendar.set(Calendar.AM_PM, Calendar.PM)

        val formattedTime = "%02d:%02d".format(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)
        )
        Log.d(TAG, "Alarm Set")
        Log.d(TAG, calendar.timeInMillis.toString())
        Log.d(TAG, System.currentTimeMillis().toString())
        Log.d(TAG, formattedTime.toString())

        if (calendar.timeInMillis < System.currentTimeMillis()) {
            Log.d(TAG, "Alarm time is in the past")
            return
        }

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        Log.d(TAG, "Alarm set for $selectedTime")
    }

    companion object {
        private const val TAG = "StepsMonitor"
        private const val ALARM_REQUEST_CODE = 100
        const val EXTRA_SELECTED_TIME = "selected_time"
    }
}