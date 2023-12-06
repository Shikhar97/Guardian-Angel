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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Calendar

class StepsMonitor : AppCompatActivity() {
    lateinit var stepsField: TextView
    lateinit var goalField: TextView
    lateinit var goalButton: Button
    lateinit var remainderButton: Button
    lateinit var suggestionsButton: Button

    private lateinit var alarmManager: AlarmManager

    private lateinit var pendingIntent: PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_steps_monitor)
        createNotificationChannel(this)
        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)

        topAppBar.setNavigationOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.help -> {
                    // Handle more item (inside overflow menu) press
                    true
                }

                else -> false
            }
        }

        // Set the alarm manager and pending intent
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        stepsField = this.findViewById(R.id.stepsCount)
        goalField = this.findViewById(R.id.goalField)
        remainderButton = this.findViewById(R.id.remainderButton)
        goalButton = this.findViewById(R.id.goalButton)
        suggestionsButton = this.findViewById(R.id.suggestionsButton)


        goalButton.setOnClickListener {
            Log.d(TAG, "Inside goal button")
            showNumberDialog()
        }

        remainderButton.setOnClickListener {
            if (isNumeric(goalField.text.toString())) {
                showTimeDialog()
            } else {
                showSetGoalWarningDialog()
            }

        }

        suggestionsButton.setOnClickListener {
            Log.d(TAG, "Inside goal button")
            showSuggestionsActivity()
        }
    }
    private fun isNumeric(value: String): Boolean {
        return value.toDoubleOrNull() != null
    }

    private fun showSuggestionsActivity() {
        val intent = Intent(this, WalkingSuggestionsActivity::class.java)
        startActivity(intent)
    }

    private fun showSetGoalWarningDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Warning!")
            .setMessage("Set your daily goal first to set remainders")
            .setPositiveButton("OK") { dialog, which ->

            }
            .show()
    }
    private fun showNumberDialog() {
        Log.d(TAG, "inside")
        val dialogView = layoutInflater.inflate(R.layout.number_dialog_layout, null)
        val numberField = dialogView.findViewById<EditText>(R.id.numberField)

        MaterialAlertDialogBuilder(this)
            .setTitle("Enter your goal")
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, which ->
                val enteredNumber = numberField.text.toString()
                Log.d(TAG, enteredNumber)
                goalField.text = enteredNumber
                goalButton.text = "Update Goal"
            }
            .setNegativeButton("Cancel") { dialog, which ->
            }
            .show()
    }

    private fun showTimeDialog() {
        val timeView = layoutInflater.inflate(R.layout.remainder_date_picker, null)
        val timeField = timeView.findViewById<TimePicker>(R.id.timePicker)

        MaterialAlertDialogBuilder(this)
            .setTitle("Set the time for goal remainders")
            .setView(timeView)
            .setPositiveButton("OK") { dialog, which ->
                var hour = timeField.hour
                var minute = timeField.minute

                var hourFlag = (timeField.hour < 12)

                val formattedTime = "%02d:%02d".format(hour, minute)
                Log.d(TAG, formattedTime.toString())

                val notificationIntent = Intent(this, AlarmReceiver::class.java)
                    .putExtra("stepsToday", stepsField.text)
                    .putExtra("goal", goalField.text)

                pendingIntent = PendingIntent.getBroadcast(this,
                    StepsMonitor.ALARM_REQUEST_CODE, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE)

                // Set the alarm for the selected time
                setAlarm(formattedTime, hourFlag)
            }
            .setNegativeButton("Cancel") { dialog, which ->
            }
            .show()
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

    @SuppressLint("ScheduleExactAlarm", "SuspiciousIndentation")
    private fun setAlarm(selectedTime: String, hourFlag: Boolean) {
        // Parse the selected time to a Calendar object
        val calendar = Calendar.getInstance()
        val (hour, minute) = selectedTime.split(":").map { it.toInt() }
        Log.d(TAG, calendar.timeInMillis.toString())
        calendar.set(Calendar.HOUR_OF_DAY, hour)
//        calendar.timeInMillis = System.currentTimeMillis() + 10_000L
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

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        Log.d(TAG, "Alarm set for $selectedTime")
    }

    companion object {
        private const val TAG = "StepsMonitor"
        private const val ALARM_REQUEST_CODE = 100
        const val EXTRA_SELECTED_TIME = "selected_time"
    }
}