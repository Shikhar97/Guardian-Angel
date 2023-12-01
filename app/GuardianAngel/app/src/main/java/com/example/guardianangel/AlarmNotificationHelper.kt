package com.example.guardianangel

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Handler
import android.util.Log
import kotlinx.coroutines.*

class AlarmNotificationHelper : BroadcastReceiver() {
    private var mediaPlayer: MediaPlayer? = null
    private var notificationId: Int? = null
    private lateinit var notificationManager: NotificationManager
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("AlarmNotificationHelper", "onReceive")

            val message = "Wake up"
            val channelId = "alarm_id"

            context?.let { ctx ->
                this.notificationId = 1
            notificationManager =
                ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Uri for the default notification sound
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val builder = NotificationCompat.Builder(ctx, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Alarm Demo")
                .setContentText("Notification sent with message $message")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(defaultSoundUri)  // Set the notification sound
                .setAutoCancel(true)  // Automatically dismiss the notification when clicked

                // Add a stop button
                val stopIntent = Intent(ctx, StopReceiver::class.java)
                val stopPendingIntent = PendingIntent.getBroadcast(
                    ctx,
                    3,
                    stopIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(android.R.drawable.ic_media_pause, "Stop", stopPendingIntent)

                // Show the notification
                notificationManager.notify(notificationId!!, builder.build())

                val customSoundUri = Uri.parse("android.resource://${ctx.packageName}/${R.raw.alarm_sound}")

                // Play the sound
                playSound(context, customSoundUri)
        }
    }
    private fun playSound(context: Context, soundUri: Uri) {
        AlarmNotificationHelper.startSound(context, soundUri)
    }

    companion object {
        private var mediaPlayer: MediaPlayer? = null

        fun startSound(context: Context, soundUri: Uri) {

            mediaPlayer = MediaPlayer.create(context, soundUri)
            mediaPlayer?.start()
        }

        fun stopSound(notificationManager: NotificationManager?) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            notificationManager?.cancel(1)
        }
    }
}
