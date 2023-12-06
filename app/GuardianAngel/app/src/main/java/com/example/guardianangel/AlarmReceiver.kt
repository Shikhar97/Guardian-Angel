package com.example.guardianangel

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val value1 = intent.getStringExtra("stepsToday")?.toInt()
        val value2 = intent.getStringExtra("goal")?.toInt()
        Log.d(TAG, value1.toString())
        Log.d(TAG, value2.toString())

        val openIntent = Intent(context, WalkingSuggestionsActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(context, 0, openIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        var notificationBuilder: NotificationCompat.Builder

        if (value1 != null) {
            if(value1 < value2!!) {
                notificationBuilder = NotificationCompat.Builder(context, "alarm_channel")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true)
                    .setVibrate(longArrayOf(1000,1000,1000,1000))
                    .setContentTitle("Seems that you haven't met your Goal")
                    .setContentText("Click to see suggestions!")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
            }else {
                notificationBuilder = NotificationCompat.Builder(context, "alarm_channel")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true)
                    .setVibrate(longArrayOf(1000,1000,1000,1000))
                    .setContentTitle("Awesome you have met your Goal")
                    .setContentText("Click to see suggestions!")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
            }
            // Display the notification
            val notificationManager = NotificationManagerCompat.from(context)
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return
            }
            notificationManager.notify(1, notificationBuilder.build())
        }
    }
}