package com.example.guardianangel.sleep_wellness.alarm


import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import com.example.guardianangel.R
import com.example.guardianangel.sleep_wellness.database.SQLiteHelper
import com.example.guardianangel.sleep_wellness.notification.DemoStopReceiver
import android.os.Handler
import android.os.Looper

class DemoAlarmNotifier : BroadcastReceiver() {
    private var mediaPlayer: MediaPlayer? = null
    private var notificationId: Int? = null
    private lateinit var notificationManager: NotificationManager
    private lateinit var dbHandler: SQLiteHelper
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("DemoAlarmNotifier", "onReceive")

        val message = "Wake up"
        val channelId = "alarm_id"

        context?.let { ctx ->
            this.notificationId = 111
            notificationManager =
                ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Uri for the default notification sound
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val builder = NotificationCompat.Builder(ctx, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Guardian Angel - Demo Alarm")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(defaultSoundUri)  // Set the notification sound
                .setAutoCancel(true)  // Automatically dismiss the notification when clicked

            // Add a stop button
            val stopIntent = Intent(ctx, DemoStopReceiver::class.java)
            val stopPendingIntent = PendingIntent.getBroadcast(
                ctx,
                111,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_media_pause, "Stop", stopPendingIntent)

            // Show the notification
            notificationManager.notify(notificationId!!, builder.build())

            val customSoundUri = Uri.parse("android.resource://${ctx.packageName}/${R.raw.alarm_sound}")

            // Play the sound
            playSound(context, customSoundUri)

            Handler(Looper.getMainLooper()).postDelayed({
                stopSound(notificationManager)
            }, 30000) // Stop after 30 seconds
        }
    }
    private fun playSound(context: Context, soundUri: Uri) {
        startSound(context, soundUri)
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
            notificationManager?.cancel(111)
        }
    }
}
