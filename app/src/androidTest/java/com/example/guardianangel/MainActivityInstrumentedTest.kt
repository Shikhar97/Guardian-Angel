package com.example.guardianangel

import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentedTest {
    @Test
    fun testInitialValuesDisplayed() {
        val scenario: ActivityScenario<MainActivity> = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            assertEquals("Initial steps value", "0", activity.stepsField.text.toString())
            assertEquals("Initial goal value", "Goal Not Set", activity.goalField.text.toString())
            assertEquals("Initial remainder button text", "Set Remainder", activity.remainderButton.text.toString())
            assertEquals("Initial goal button text", "Send Goal", activity.goalButton.text.toString())

            scenario.close()
        }
    }
    @Test
    fun testGoalSetting() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            val goalButton = activity.findViewById<Button>(R.id.goalButton)
            goalButton.performClick()

            val numberField = activity.findViewById<EditText>(R.id.numberField)
            val submitButton = activity.findViewById<Button>(R.id.submitButton)

            numberField.setText("5000")
            submitButton.performClick()

            val goalField = activity.findViewById<TextView>(R.id.goalField)
            assertEquals("5000", goalField.text.toString())

            scenario.close()
        }
    }

    @Test
    fun testTimeSetting() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            val remainderButton = activity.findViewById<Button>(R.id.remainderButton)
            remainderButton.performClick()

            val timePicker = activity.findViewById<TimePicker>(R.id.timePicker)
            val submitButton = activity.findViewById<Button>(R.id.timeButton)

            timePicker.hour = 12
            timePicker.minute = 30

            submitButton.performClick()

            val expectedHour = timePicker.hour
            val expectedMinute = timePicker.minute

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, expectedHour)
            calendar.set(Calendar.MINUTE, expectedMinute)

            val expectedTimeInMillis = timePicker.hour * 60 * 60 * 1000 + timePicker.minute * 60 * 1000

            assertEquals(expectedTimeInMillis, calendar.timeInMillis)

            scenario.close()
        }
    }
}
