package com.example.guardianangel

import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.*

class AlarmActivityTest {

    @Test
    fun validTimeInput_setsAlarm() {
        val validTime = "12:30"

        val mockContext = mock(AlarmActivity::class.java)

        val alarmActivity = AlarmActivity()

        alarmActivity.createNotificationChannel(mockContext)
        alarmActivity.setAlarm(validTime)

        assertTrue(alarmActivity.notificationChannelCreated)

    }

    @Test
    fun alarmTriggering_displaysNotification() {
        val shortTime = "00:01"

        val mockContext = mock(AlarmActivity::class.java)

        val alarmActivity = AlarmActivity()

        alarmActivity.createNotificationChannel(mockContext)
        alarmActivity.setAlarm(shortTime)

        assertTrue(alarmActivity.notificationDisplayed)
    }
}
