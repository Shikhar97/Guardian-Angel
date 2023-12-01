package com.example.guardianangel

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

import android.util.Log
import kotlinx.coroutines.Dispatchers
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import okhttp3.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class AlarmReceiver : BroadcastReceiver() {
    private lateinit var dbHandler: SQLiteHelper
    override fun onReceive(context: Context, intent: Intent) {
        println("AlarmReceiver.onReceive")
        dbHandler = SQLiteHelper(context, null)
        try {
            println("AlarmReceiver.onReceive try")
            Log.d("AlarmReceiver", "Alarm received!")
            fetchDataFromServer(context)
        } catch (ex: Exception) {
            Log.d("Receive Ex", "onReceive: ${ex.printStackTrace()}")
        }
    }

    private fun fetchDataFromServer(context: Context) {
        // Start a coroutine in the background thread
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = getWakeUpTimeFromServer(context)
                // Handle the result as needed
            } catch (e: Exception) {
                // Handle exceptions (e.g., IOException, JSONException)
                e.printStackTrace()
            }
        }
    }
    private fun getWakeUpTimeFromServer(context: Context) {
        println("Inside getWakeUpTimeFromServer")
        // Adding user id to the url
        val baseUrl = "https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/users/655ff2802c6a0e4de1d9a9d4/wake_up_time"
        val apiKey = "<api_key>"
       //  <For testing> Without API key the below code doesn't work. Reach out to aelango3@asu.edu for api key
//        val client = OkHttpClient()
//
//        var latestData = dbHandler.getLatestData()
//        var wakeupPreference = latestData?.WAKEUP_PREFERENCE?.lowercase(Locale.getDefault()) ?: "normal"
//
//        val url = baseUrl.toHttpUrlOrNull()!!.newBuilder()
//            .addQueryParameter("user_preference", wakeupPreference)
//            .build()
//
//        val request = Request.Builder()
//            .url(url)
//            .header("X-Api-Auth", apiKey)
//            .method("GET", null)
//            .build()
//
//        println(request)
//
//        client.newCall(request).execute().use { response ->
//            if (!response.isSuccessful)
//                throw IOException("Unexpected code $response")
//
//            for (header in response.headers) {
//                println("${header.first}: ${header.second}")
//            }
//
//            val responseBody = response.body?.string()
//            println(responseBody)
//            val jsonResponse = parseJsonResponse(responseBody)
//            val wakeUpTime = jsonResponse?.optLong("wake_up_time")
//            if (wakeUpTime != null) {
//                // Schedule alarm with the received wake-up time
//                scheduleAlarm(wakeUpTime, context)
//            }
//        }
        // <For testing> Comment the above code and uncomment the below code for immediate testing
        scheduleAlarm(400, context)
    }

    fun scheduleAlarm(wakeUpTime: Long, context: Context): Long {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, AlarmNotificationHelper::class.java)
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
        val triggerTimeFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
            Date(triggerTime)
        )
        Log.d("Alarm time", triggerTimeFormatted)
        // Toast.makeText(context, "Alarm time: $triggerTimeFormatted", Toast.LENGTH_LONG).show()

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
            2, // Request code should be the same as used when scheduling the alarm (in your case, 2)
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