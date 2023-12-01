package com.example.guardianangel

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import okhttp3.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException

class AlarmSchedulerImpl(
    private val context: Context
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @SuppressLint("ScheduleExactAlarm")
    override fun schedule() {
        Log.d("AlarmScheduler", "Alarm setting started!")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            121,
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        Log.d("Current time", dateFormat.format(calendar.time))

        // <For testing> Comment the below three lines for immediate testing
        calendar.set(Calendar.HOUR_OF_DAY, 21)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        // <For testing> Uncomment this to test the alarm immediately
        // calendar.timeInMillis = System.currentTimeMillis()
        // calendar.add(Calendar.SECOND, 10)

        if (Calendar.getInstance().after(calendar)) {
            calendar.add(Calendar.DATE, 1)
        }
        Log.d("Trigger time", dateFormat.format(calendar.time))

        // Repeat the alarm every day
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

        // <For testing> For demo purposes I'm adding instant notification too since setRepeating will not work as precise (depend on devices) as setExactAndAllowWhileIdle
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    override fun cancel() {
        Log.d("AlarmScheduler", "Alarm setting cancelled!")
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                125,
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }


}