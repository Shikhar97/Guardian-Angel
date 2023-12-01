package com.example.guardianangel

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@LargeTest
class AlarmActivityInstrumentedTest {

    @get:Rule
    val activityRule = ActivityTestRule(AlarmActivity::class.java)

    @Test
    fun validTimeInput_setsAlarm() {
        // Input a valid time and check if the alarm is set correctly
        val validTime = "12:30"
        // Perform actions to set the time in your app

        // Assert that the alarm is set for the correct time
        // You may need to use IdlingResource or other synchronization mechanisms
    }

    @Test
    fun invalidTimeInput_handlesError() {
        // Input an invalid time and ensure the app handles it appropriately
        val invalidTime = "abc"
        // Perform actions to set the time in your app

        // Assert that the app handles the error, for example, displays an error message
    }

    @Test
    fun alarmTriggering_displaysNotification() {
        // Set an alarm for a short time from now to trigger quickly
        val shortTime = "00:01"
        // Perform actions to set the time in your app

        // Wait for the alarm to trigger (you may need to use IdlingResource or other mechanisms)
        TimeUnit.SECONDS.sleep(5)

        // Assert that the notification is displayed
        // You may need to interact with the notification system for assertions
    }
}
