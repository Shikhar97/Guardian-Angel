package com.example.guardianangel

import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import androidx.test.core.app.ActivityScenario
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric.buildActivity
import org.robolectric.RobolectricTestRunner
import java.util.Calendar

private lateinit var activity: MainActivity
@RunWith(RobolectricTestRunner::class)
class MainActivityTest {
    @Before
    fun setUp() {
        activity = buildActivity(MainActivity::class.java).create().get()
    }

    @Test
    fun testInitialValuesDisplayed() {
        val scenario: ActivityScenario<MainActivity> = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            // Assert initial values for UI elements
            assertEquals("Initial steps value", "0", activity.stepsField.text.toString())
            assertEquals("Initial goal value", "Goal Not Set", activity.goalField.text.toString())
            assertEquals("Initial remainder button text", "Set Remainder", activity.remainderButton.text.toString())
            assertEquals("Initial goal button text", "Send Goal", activity.goalButton.text.toString())
            @Test
            fun testInitialValuesDisplayed() {
                val scenario: ActivityScenario<MainActivity> = ActivityScenario.launch(MainActivity::class.java)

                scenario.onActivity { activity ->
                    // Assert initial values for UI elements
                    assertEquals("Initial steps value", "YourExpectedInitialStepsValue", activity.stepsField.text.toString())
                    assertEquals("Initial goal value", "YourExpectedInitialGoalValue", activity.goalField.text.toString())
                    assertEquals("Initial remainder button text", "YourExpectedInitialButtonText", activity.remainderButton.text.toString())
                    assertEquals("Initial goal button text", "YourExpectedInitialButtonText", activity.goalButton.text.toString())

                    scenario.close()
                }
            }

            scenario.close()
        }
    }

    @Test
    fun testGoalSettingWithoutActivityScenario() {
        val goalButton = activity.findViewById<Button>(R.id.goalButton)
        goalButton.performClick()

        val numberField = activity.findViewById<EditText>(R.id.numberField)
        val submitButton = activity.findViewById<Button>(R.id.submitButton)

        numberField.setText("5000")
        submitButton.performClick()

        val goalField = activity.findViewById<TextView>(R.id.goalField)
        assertEquals("5000", goalField.text.toString())

        activity.finish()
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
