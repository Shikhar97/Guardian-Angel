package com.example.guardianangel

import android.content.Intent
import android.widget.Button
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule

@RunWith(AndroidJUnit4::class)
class MainActivityTest {


    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() {
        scenario = activityScenarioRule.scenario
    }
    @Test
    fun testDiagnoseButton() {
        scenario.onActivity { activity ->
            val diagnoseButton = activity.findViewById<Button>(R.id.button)
            assertNotNull(diagnoseButton)

            diagnoseButton.performClick()

            val expectedIntent = Intent(activity, UpdateActivity::class.java)
            val actualIntent = activity.intent
            assertNotNull(actualIntent)
        }
    }
    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.guardianangel", appContext.packageName)
    }
}