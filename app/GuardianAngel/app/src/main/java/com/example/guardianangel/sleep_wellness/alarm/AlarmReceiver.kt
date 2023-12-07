package com.example.guardianangel.sleep_wellness.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import android.util.Log
import android.widget.Toast
import com.example.guardianangel.BuildConfig
import com.example.guardianangel.sleep_wellness.database.SQLiteHelper
import kotlinx.coroutines.Dispatchers
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.*
import com.example.guardianangel.sleep_wellness.jobs.JobScheduler
import com.example.guardianangel.sleep_wellness.jobs.JobSchedulerInterface
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException

class AlarmReceiver : BroadcastReceiver() {
    private lateinit var dbHandler: SQLiteHelper
    private lateinit var jobSchedulerInterface: JobSchedulerInterface
    override fun onReceive(context: Context, intent: Intent) {
        println("AlarmReceiver.onReceive")
        dbHandler = SQLiteHelper(context, null)
        var latestData = dbHandler.getLatestData()
        if(latestData?.SLEEP_WELLNESS == true) {
            triggerNotification(context)
            println("Scheduling a new Job")
            jobSchedulerInterface = JobScheduler(context)
            jobSchedulerInterface.scheduleDailyJob(86400L)
        }
    }

    private fun triggerNotification(context: Context) {
        try {
            println("Inside triggerNotification")
            var wakeUpTime = 420L // Default 7 hours
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    wakeUpTime = getWakeUpTimeFromServer(context)
                    println("Wake up time Inside triggerNotification: $wakeUpTime")
                    scheduleAlarm(wakeUpTime, context)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        } catch (ex: Exception) {
            Log.d("Receive Ex", "triggerNotification onReceive: ${ex.printStackTrace()}")
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun getWakeUpTimeFromServer(context: Context): Long {
        println("SENDING REQUEST TO SERVER FOR SLEEP TIME COMPUTATION")
        // Adding user id to the url
        val baseUrl = "https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/users/655ff2802c6a0e4de1d9a9d4/wake_up_time"
        val apiKey = BuildConfig.HEROKU_API_KEY
        var wakeUpTime = 420L // Default 7 hours
        // <For testing> Without API key the below code doesn't work. Reach out to aelango3@asu.edu for api key
        val client = OkHttpClient()

        var latestData = dbHandler.getLatestData()
        var wakeupPreference = latestData?.WAKEUP_PREFERENCE?.lowercase(Locale.getDefault()) ?: "normal"

        val url = baseUrl.toHttpUrlOrNull()!!.newBuilder()
            .addQueryParameter("user_preference", wakeupPreference)
            .build()

        val request = Request.Builder()
            .url(url)
            .header("X-Api-Auth", apiKey)
            .method("GET", null)
            .build()

        println(request)

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful)
                throw IOException("Unexpected code $response")
                wakeUpTime = 420L

            for (header in response.headers) {
                println("${header.first}: ${header.second}")
            }

            val responseBody = response.body?.string()
            println(responseBody)
            val jsonResponse = parseJsonResponse(responseBody)
            wakeUpTime = jsonResponse?.optLong("wake_up_time")!!
            println("Wake up time: $wakeUpTime")
        }
        // <For testing> Comment the above code and uncomment the below code for immediate testing
        return wakeUpTime
    }

    fun scheduleAlarm(wakeUpTime: Long, context: Context): Long {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, AlarmNotifier::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            2, // Request code should be 2
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance()
        val currentTime = calendar.timeInMillis
        // <For testing> Comment line 105 and uncomment line 106 for immediate testing
        val triggerTime = currentTime + (wakeUpTime * 60 * 1000)
        // val triggerTime = currentTime + (10 * 1000)
        val triggerTimeFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(
            Date(triggerTime)
        )
        println("scheduleAlarm Alarm time $triggerTimeFormatted")
//        Toast.makeText(context, "Alarm time: $triggerTimeFormatted", Toast.LENGTH_LONG).show()

        dbHandler.updateAlarmTime(triggerTime)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )

        return triggerTime
    }

    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            2, // Request code should be the same as used when scheduling the alarm (in my case, 2)
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Cancel the alarm
        alarmManager.cancel(pendingIntent)
    }

    private fun parseJsonResponse(response: String?): JSONObject? {
        return try {
            JSONObject(response)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}