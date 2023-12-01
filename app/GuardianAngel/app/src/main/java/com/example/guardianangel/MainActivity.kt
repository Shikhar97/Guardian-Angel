package com.example.guardianangel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import com.example.guardianangel.SQLiteHelper
import com.example.guardianangel.DBModel

class MainActivity : AppCompatActivity() {
    private lateinit var toggleSleepWellness: Switch
    private lateinit var spinnerWakeupPreference: Spinner
    private lateinit var buttonSave: Button

    lateinit var dbHandler: SQLiteHelper
    lateinit var alarmScheduler: AlarmScheduler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        println("MainActivity.onCreate")
        toggleSleepWellness = findViewById(R.id.toggleSleepWellness)
        spinnerWakeupPreference = findViewById(R.id.spinnerWakeupPreference)
        buttonSave = findViewById(R.id.buttonSave)
        val tableName = SQLiteHelper.TABLE_NAME
        dbHandler = SQLiteHelper(this, null, tableName)

        alarmScheduler = AlarmSchedulerImpl(this)
        loadDataFromDatabase()

        buttonSave.setOnClickListener {
            onSaveButtonClick()
        }
    }

    fun onSaveButtonClick() {
        // Retrieve values from UI components
        val enableSleepWellness = toggleSleepWellness.isChecked
        val wakeupPreference = spinnerWakeupPreference.selectedItem.toString()

//        spinnerWakeupPreference.isEnabled = enableSleepWellness

        // Check if an entry already exists
        if (dbHandler.isEntryExists()) {
            // Entry exists, update the existing entry
            dbHandler.updateData(DBModel(SLEEP_WELLNESS = enableSleepWellness, WAKEUP_PREFERENCE = wakeupPreference))
        } else {
            // Entry does not exist, add a new entry
            dbHandler.addData(DBModel(SLEEP_WELLNESS = enableSleepWellness, WAKEUP_PREFERENCE = wakeupPreference))
        }

        if (enableSleepWellness) {
            // Schedule the alarm
            alarmScheduler.schedule()
        } else {
            // Cancel the alarm
            alarmScheduler.cancel()
        }

        Toast.makeText(this, "Data saved/updated successfully", Toast.LENGTH_SHORT).show()
        loadDataFromDatabase()
    }

    private fun loadDataFromDatabase() {
        val latestData = dbHandler.getLatestData()

        // Update UI with values from the database
        toggleSleepWellness.isChecked = latestData?.SLEEP_WELLNESS ?: false
        spinnerWakeupPreference.setSelection(getIndexForSpinner(latestData?.WAKEUP_PREFERENCE ?: ""))
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