package com.example.guardianangel.sleep_wellness.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import com.example.guardianangel.sleep_wellness.alarm.AlarmNotifier
import com.example.guardianangel.sleep_wellness.database.SQLiteHelper

class StopReceiver : BroadcastReceiver() {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var dbHandler: SQLiteHelper
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            dbHandler = SQLiteHelper(context, null)
            // Get the NotificationManager
            val notificationManager =
                it.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Stop the sound and cancel the notification
            AlarmNotifier.stopSound(notificationManager, dbHandler)

        }
    }
}
