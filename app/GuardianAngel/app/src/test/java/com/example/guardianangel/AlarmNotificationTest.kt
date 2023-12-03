//package com.example.guardianangel
//
//import android.content.Context
//import android.content.Intent
//import android.media.RingtoneManager
//import android.net.Uri
//import android.os.Build
//import androidx.core.app.NotificationManagerCompat
//import androidx.test.core.app.ApplicationProvider
//import androidx.test.espresso.intent.Intents
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import androidx.test.platform.app.InstrumentationRegistry
//import com.example.guardianangel.notification.StopReceiver
//import org.junit.After
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.mockito.ArgumentMatchers.any
//import org.mockito.Mockito.mock
//import org.mockito.Mockito.verify
//
//@RunWith(AndroidJUnit4::class)
//class AlarmNotificationTest {
//
//    private lateinit var context: Context
//
//    @Before
//    fun setup() {
//        context = ApplicationProvider.getApplicationContext<Context>()
//    }
//
//    @After
//    fun cleanup() {
//        Intents.release()
//    }
//
//    @Test
//    fun testOnReceive() {
//        // Mock the notification manager
//        val mockNotificationManager = mock(NotificationManagerCompat::class.java)
//        AlarmNotificationHelper.startSound(context, Uri.EMPTY) // Mocking startSound method
//
//        // Set the default notification sound
//        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//
//        // Create an intent to simulate the broadcast
//        val intent = Intent(context, AlarmNotificationHelper::class.java)
//
//        // Create AlarmNotificationHelper instance and call onReceive
//        val alarmNotificationHelper = AlarmNotificationHelper()
//        alarmNotificationHelper.onReceive(context, intent)
//
//        // Verify that the notification manager is notified with the correct arguments
//        verify(mockNotificationManager).notify(any(), any())
//
//        // If you have more assertions, you can add them here
//    }
//
//    @Test
//    fun testStartSound() {
//        val soundUri = Uri.EMPTY
//
//        // Create an instance of AlarmNotificationHelper and call startSound
//        AlarmNotificationHelper.startSound(context, soundUri)
//
//        // If you have more assertions, you can add them here
//    }
//
//    @Test
//    fun testStopSound() {
//        // Mock the notification manager
//        val mockNotificationManager = mock(NotificationManagerCompat::class.java)
//
//        // Create an instance of AlarmNotificationHelper and call stopSound
//        AlarmNotificationHelper.stopSound(mockNotificationManager)
//
//        // If you have more assertions, you can add them here
//    }
//}
