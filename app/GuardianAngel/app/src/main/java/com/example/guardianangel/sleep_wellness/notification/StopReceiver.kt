package com.example.guardianangel.sleep_wellness.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import com.example.guardianangel.sleep_wellness.alarm.AlarmNotifier

class StopReceiver : BroadcastReceiver() {
    private var mediaPlayer: MediaPlayer? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            // Get the NotificationManager
            val notificationManager =
                it.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Stop the sound and cancel the notification
            AlarmNotifier.stopSound(notificationManager)
        }
    }
}
