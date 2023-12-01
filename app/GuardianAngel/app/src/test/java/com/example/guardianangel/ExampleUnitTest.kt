package com.example.guardianangel

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.text.SimpleDateFormat
import java.util.*

class ExampleUnitTest {
    private lateinit var dateCalculator: DateCalculator
    private lateinit var calendar: Calendar

    @Before
    fun setup() {
        dateCalculator = DateCalculator()
        calendar = mock(Calendar::class.java)
    }

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }


    @Test
    fun `calculateNextDate should return the correct date`() {
        // Arrange
        val startDate = "Mon Nov 20 00:00:00 GMT 2023" // Example start date
        val cycleLength = 28
        val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US)
        val startDateObject = dateFormat.parse(startDate)
        val expectedDate = addDays(startDateObject, cycleLength)

        doReturn(startDateObject.time).`when`(calendar).timeInMillis

        // Act
        val result = dateCalculator.calculateNextDate(startDate, cycleLength)

        // Assert
        assertEquals(expectedDate, result)
    }

    private fun addDays(date: Date, days: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DATE, days)
        return calendar.time
    }
}

