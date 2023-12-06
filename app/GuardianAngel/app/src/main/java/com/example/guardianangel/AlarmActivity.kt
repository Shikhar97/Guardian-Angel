package com.example.guardianangel

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import java.util.*

class AlarmActivity : AppCompatActivity() {

    private lateinit var alarmManager: AlarmManager

    private lateinit var pendingIntent: PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the intent that started this activity and extract the selected time
        val intent = intent
        val selectedTime = intent.getStringExtra(EXTRA_SELECTED_TIME) ?: return

        // Create the notification channel if necessary
        createNotificationChannel(this)

        // Set the alarm manager and pending intent
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val notificationIntent = Intent(this, AlarmReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(this, ALARM_REQUEST_CODE, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE)

        // Set the alarm for the selected time
        setAlarm(selectedTime)
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

    @SuppressLint("ScheduleExactAlarm")
    private fun setAlarm(selectedTime: String) {
        // Parse the selected time to a Calendar object
        val calendar = Calendar.getInstance()
        val (hour, minute) = selectedTime.split(":").map { it.toInt() }
        Log.d(TAG, calendar.timeInMillis.toString())
        calendar.set(Calendar.HOUR_OF_DAY, hour)
//        calendar.timeInMillis = System.currentTimeMillis() + 10_000L
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.AM_PM, Calendar.PM)
        val formattedTime = "%02d:%02d".format(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)
        )
        Log.d(TAG, "Alarm Set")
        Log.d(TAG, calendar.timeInMillis.toString())
        Log.d(TAG, System.currentTimeMillis().toString())
        Log.d(TAG, formattedTime.toString())

//        calendar.timeInMillis = calendar.timeInMillis + 2_000L

        // Check if the alarm time is in the past
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            Log.d(TAG, calendar.timeInMillis.toString())
            Log.d(TAG, System.currentTimeMillis().toString())
            Log.d(TAG, "Alarm time is in the past")
        }

        // Set the alarm for the selected time
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        Log.d(TAG, "Alarm set for $selectedTime")
    }

    companion object {
        private const val TAG = "AlarmActivity"
        private const val ALARM_REQUEST_CODE = 100
        const val EXTRA_SELECTED_TIME = "selected_time"
    }
}