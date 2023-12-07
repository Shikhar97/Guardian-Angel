package com.example.guardianangel.sleep_wellness.jobs

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.guardianangel.sleep_wellness.alarm.AlarmReceiver
import okhttp3.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.*
import com.example.guardianangel.sleep_wellness.database.SQLiteHelper

class JobScheduler(
    private val context: Context
) : JobSchedulerInterface {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private lateinit var dbHandler: SQLiteHelper
    @SuppressLint("ScheduleExactAlarm")
    override fun scheduleDailyJob(timeInterval: Long) {
        dbHandler = SQLiteHelper(context, null)
        var latestData = dbHandler.getLatestData()
        if (latestData?.SLEEP_WELLNESS != true) {
            // Sleep wellness is not true, return without scheduling the job
            return
        }
        println("Inside scheduleDailyJob function")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            121,
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        println("DailyJob Current time: ${dateFormat.format(calendar.time)}")
        println("Time interval received: $timeInterval")

        if (timeInterval == 0L) {
            // First time this is triggered
            calendar.timeInMillis = System.currentTimeMillis()
           // calendar.add(Calendar.SECOND, 10)
            // <For testing> Comment the below three lines for immediate testing
            latestData.SLEEP_TIME?.let { sleepTime ->
//                val dbCalendar = Calendar.getInstance()
//                dbCalendar.time = sleepTime
//                calendar.set(Calendar.HOUR_OF_DAY, dbCalendar.get(Calendar.HOUR_OF_DAY))
//                calendar.set(Calendar.MINUTE, dbCalendar.get(Calendar.MINUTE))
//                calendar.set(Calendar.SECOND, 0)
                calendar.add(Calendar.SECOND, 1)
            }

        } else {
            // This is triggered after the first time
//            calendar.timeInMillis = System.currentTimeMillis()
//            calendar.add(Calendar.SECOND, 10)
            calendar.add(Calendar.SECOND, timeInterval.toInt())
        }

        if (Calendar.getInstance().after(calendar)) {
            calendar.add(Calendar.DATE, 1)
        }
        println("DailyJob Trigger time: ${dateFormat.format(calendar.time)}")
        Toast.makeText(context, "Trigger time: ${dateFormat.format(calendar.time)}", Toast.LENGTH_SHORT).show()
        dbHandler.updateDailyJobTime(calendar.timeInMillis)
//        val interval = AlarmManager.INTERVAL_DAY
//
//        // Repeat the alarm every day
//        alarmManager.setRepeating(
//            AlarmManager.RTC_WAKEUP,
//            calendar.timeInMillis,
//            interval,
//            pendingIntent
//        )

        // <For testing> For demo purposes I'm adding instant notification too since setRepeating will not work as precise (depend on devices) as setExactAndAllowWhileIdle
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    override fun cancelDailyJob() {
        Log.d("cancelDailyJob", "DailyJob is cancelled!")
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                121,
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }
}