package com.example.guardianangel.sleep_wellness

import com.example.guardianangel.R
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CompoundButton
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.guardianangel.MainActivity
import com.example.guardianangel.sleep_wellness.database.DBModel
import com.example.guardianangel.sleep_wellness.database.SQLiteHelper
import com.example.guardianangel.sleep_wellness.jobs.JobScheduler
import com.example.guardianangel.sleep_wellness.jobs.JobSchedulerInterface
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.materialswitch.MaterialSwitch
import java.util.Calendar

class SleepWellnessMain : AppCompatActivity() {
    private lateinit var toggleSleepWellness: MaterialSwitch
    private lateinit var spinnerWakeupPreference: Spinner
    private lateinit var buttonSave: Button
    private lateinit var wakeupPreferenceText: TextView
    private lateinit var sleepTimeText: TextView
    private lateinit var timePicker: TimePicker


    lateinit var dbHandler: SQLiteHelper
    lateinit var jobSchedulerInterface: JobSchedulerInterface
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sleep_wellness_settings)
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
        toggleSleepWellness = findViewById(R.id.toggleSleepWellness)
        spinnerWakeupPreference = findViewById(R.id.spinnerWakeupPreference)
        buttonSave = findViewById(R.id.buttonSave)
        wakeupPreferenceText = findViewById(R.id.textView)
        sleepTimeText = findViewById(R.id.textView2)

        timePicker = findViewById(R.id.datePicker1)
        timePicker.setIs24HourView(true);

        val tableName = SQLiteHelper.TABLE_NAME
        dbHandler = SQLiteHelper(this, null, tableName)

        if (!allPermissionsGranted()) {
            requestPermissions()
        }

        jobSchedulerInterface = JobScheduler(this)
        loadDataFromDatabase()

        toggleSleepWellness.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                wakeupPreferenceText.visibility = View.VISIBLE
                spinnerWakeupPreference.visibility = View.VISIBLE
                timePicker.visibility = View.VISIBLE
                sleepTimeText.visibility = View.VISIBLE
            } else {
                wakeupPreferenceText.visibility = View.GONE
                spinnerWakeupPreference.visibility = View.GONE
                timePicker.visibility = View.GONE
                sleepTimeText.visibility = View.GONE
            }
        }

        buttonSave.setOnClickListener {
            onSaveButtonClick()
        }
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value) permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(baseContext, "Permission request denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun allPermissionsGranted() =
        REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
        }

    companion object {
        private val REQUIRED_PERMISSIONS =
            mutableListOf(Manifest.permission.POST_NOTIFICATIONS)
                .apply {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                        add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }
                .toTypedArray()
    }

    private fun onSaveButtonClick() {
        // Retrieve values from UI components
        val enableSleepWellness = toggleSleepWellness.isChecked
        val wakeupPreference = spinnerWakeupPreference.selectedItem.toString()

        // Retrieve the selected time from the TimePicker
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
        calendar.set(Calendar.MINUTE, timePicker.minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val sleepTime = calendar.time

        if(enableSleepWellness) {
            if (dbHandler.isEntryExists()) {
                dbHandler.updateData(DBModel(
                    SLEEP_WELLNESS = enableSleepWellness,
                    WAKEUP_PREFERENCE = wakeupPreference,
                    SLEEP_TIME = sleepTime
                ))
            } else {
                dbHandler.addData(DBModel(
                    SLEEP_WELLNESS = enableSleepWellness,
                    WAKEUP_PREFERENCE = wakeupPreference,
                    SLEEP_TIME = sleepTime
                ))
            }
        }
        else {
            if (dbHandler.isEntryExists()) {
                dbHandler.deleteLastRow()
            }
        }

        // Update or add data based on entry existence


        if (enableSleepWellness) {
            // Schedule the alarm
            jobSchedulerInterface.scheduleDailyJob()
        } else {
            // Cancel the alarm
            jobSchedulerInterface.cancelDailyJob()
        }

        Toast.makeText(this, "Data saved/updated successfully", Toast.LENGTH_SHORT).show()
        loadDataFromDatabase()
    }

    private fun loadDataFromDatabase() {
        val latestData = dbHandler.getLatestData()

        println("latestData: $latestData")

        // Update UI with values from the database
        toggleSleepWellness.isChecked = latestData?.SLEEP_WELLNESS ?: false

        if (latestData?.SLEEP_WELLNESS == true) {
            wakeupPreferenceText.visibility = View.VISIBLE
            spinnerWakeupPreference.visibility = View.VISIBLE
            timePicker.visibility = View.VISIBLE
            sleepTimeText.visibility = View.VISIBLE
        } else {
            wakeupPreferenceText.visibility = View.GONE
            spinnerWakeupPreference.visibility = View.GONE
            timePicker.visibility = View.GONE
            sleepTimeText.visibility = View.GONE
        }

        if (latestData?.WAKEUP_PREFERENCE != null) {
            spinnerWakeupPreference.setSelection(getIndexForSpinner(latestData?.WAKEUP_PREFERENCE ?: ""))
        }

        if (latestData != null) {
            latestData.SLEEP_TIME?.let { sleepTime ->
                val calendar = Calendar.getInstance()
                calendar.time = sleepTime

                // Ensure that the TimePicker is in 24-hour format
                timePicker.setIs24HourView(true)

                // Ensure that the retrieved values are within the valid range
                val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)

                println("hourOfDay: $hourOfDay")
                println("minute: $minute")

                if (hourOfDay in 0..23 && minute in 0..59) {
                    timePicker.hour = hourOfDay
                    timePicker.minute = minute
                } else {
                    // Handle invalid values, perhaps log a warning
                }
            }
        }
    }

    private fun getIndexForSpinner(value: String): Int {
        val adapter = spinnerWakeupPreference.adapter
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i).toString() == value) {
                return i
            }
        }
        return 0 // Default to the first item if not found
    }
}