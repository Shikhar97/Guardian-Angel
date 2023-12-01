package com.example.guardianangel

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    lateinit var stepsField: TextView
    lateinit var goalField: TextView
    lateinit var goalButton: Button
    lateinit var remainderButton: Button
    private lateinit var timeSelector: Button

    private lateinit var alarmManager: AlarmManager

    private lateinit var pendingIntent: PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = intent
//        val selectedTime = intent.getStringExtra(AlarmActivity.EXTRA_SELECTED_TIME) ?: return

        // Create the notification channel if necessary
        createNotificationChannel(this)

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        stepsField = this.findViewById(R.id.stepsCount)
        goalField = this.findViewById(R.id.goalField)
        remainderButton = this.findViewById(R.id.remainderButton)

        goalButton = this.findViewById(R.id.goalButton)

        goalButton.setOnClickListener {
            showNumberDialog()
        }

        remainderButton.setOnClickListener {
            showTimeDialog()
        }
    }

    private fun showNumberDialog() {
        val dialogView = layoutInflater.inflate(R.layout.number_dialog_layout, null)
        val numberField = dialogView.findViewById<EditText>(R.id.numberField)
        val submitButton = dialogView.findViewById<Button>(R.id.submitButton)

        val alertDialog = AlertDialog.Builder(this, R.style.DialogTheme)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        submitButton.setOnClickListener {
            val enteredNumber = numberField.text.toString()
            goalField.text = enteredNumber
            goalButton.text = "Update Goal"
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun showTimeDialog() {
        val timeView = layoutInflater.inflate(R.layout.date_picker, null)
        val timeField = timeView.findViewById<TimePicker>(R.id.timePicker)
        val submitButton = timeView.findViewById<Button>(R.id.timeButton)

        val alertDialog = AlertDialog.Builder(this, R.style.DialogTheme)
            .setView(timeView)
            .setCancelable(false)
            .create()

        submitButton.setOnClickListener {
            var hour = timeField.hour
            var minute = timeField.minute

            var hourFlag = (timeField.hour < 12)

            val formattedTime = "%02d:%02d".format(hour, minute)
            Log.d(TAG, formattedTime.toString())

            val notificationIntent = Intent(this, AlarmReceiver::class.java)
                .putExtra("stepsToday", stepsField.text)
                .putExtra("goal", goalField.text)

            pendingIntent = PendingIntent.getBroadcast(this,
                MainActivity.ALARM_REQUEST_CODE, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE)

            setAlarm(formattedTime, hourFlag)

            alertDialog.dismiss()
        }

        alertDialog.show()
    }


    @SuppressLint("WrongConstant", "ServiceCast")
    fun createNotificationChannel(context: Context) {
        // Create the notification channel with the required properties
        val channelId = "alarm_channel"
        val channelName = "Alarm Channel"
        val importance = NotificationManagerCompat.IMPORTANCE_HIGH

        val channel = NotificationChannel(channelId, channelName, importance)
        channel.description = "Alarm notification channel"

        val notificationManager =  NotificationManagerCompat.from(context)
        notificationManager.createNotificationChannel(channel)
    }

    @SuppressLint("ScheduleExactAlarm", "SuspiciousIndentation")
    private fun setAlarm(selectedTime: String, hourFlag: Boolean) {
        // Parse the selected time to a Calendar object
        val calendar = Calendar.getInstance()
        val (hour, minute) = selectedTime.split(":").map { it.toInt() }
        Log.d(TAG, calendar.timeInMillis.toString())
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        if(!hourFlag)
        calendar.set(Calendar.AM_PM, Calendar.PM)

        val formattedTime = "%02d:%02d".format(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)
        )
        Log.d(TAG, "Alarm Set")
        Log.d(TAG, calendar.timeInMillis.toString())
        Log.d(TAG, System.currentTimeMillis().toString())
        Log.d(TAG, formattedTime.toString())

        if (calendar.timeInMillis < System.currentTimeMillis()) {
            Log.d(TAG, "Alarm time is in the past")
            return
        }

        // Set the alarm for the selected time
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
//        alarmManager.setRepeating(
//            AlarmManager.RTC_WAKEUP,
//            calendar.timeInMillis,
//            AlarmManager.INTERVAL_DAY,
//            pendingIntent
//        )
        Log.d(TAG, "Alarm set for $selectedTime")
    }

    companion object {
        private const val TAG = "AlarmActivity"
        private const val ALARM_REQUEST_CODE = 100
    }
}