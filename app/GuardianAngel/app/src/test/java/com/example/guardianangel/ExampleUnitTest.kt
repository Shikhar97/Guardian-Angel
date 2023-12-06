package com.example.guardianangel

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Before
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

import org.junit.Assert.*
import java.text.SimpleDateFormat
import java.util.*
/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
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
    fun check_suggestion_1() {
        // Arrange
        val location = "starbucks"
        val allergy = "milk"
        val medi = "none"
        val medicalCond = "diabetic"
        val mainActivity = UtilFunction()

        // Act
        val result = mainActivity.getSuggestion(location, allergy, medi, medicalCond)

        // Assert
        assertEquals("Iced Coffee", result)
    }
    @Test
    fun check_suggestion_2() {
        // Arrange
        val location = "starbucks"
        val allergy = "none"
        val medi = "none"
        val medicalCond = "diabetic"
        val mainActivity = UtilFunction()

        // Act
        val result = mainActivity.getSuggestion(location, allergy, medi, medicalCond)

        // Assert
        assertEquals("Hot Coffee", result)
    }
    @Test
    fun check_suggestion_3() {
        // Arrange
        val location = "starbucks"
        val allergy = "none"
        val medi = "none"
        val medicalCond = "none"
        val mainActivity = UtilFunction()

        // Act
        val result = mainActivity.getSuggestion(location, allergy, medi, medicalCond)

        // Assert
        assertEquals("Vanilla Latte", result)
    }
    @Test
    fun check_suggestion_4() {
        // Arrange
        val location = "mcd"
        val allergy = "gluten"
        val medi = "none"
        val medicalCond = "none"
        val mainActivity = UtilFunction()

        // Act
        val result = mainActivity.getSuggestion(location, allergy, medi, medicalCond)

        // Assert
        assertEquals("Mcpuff", result)
    }
    @Test
    fun check_suggestion_5() {
        // Arrange
        val location = "mcd"
        val allergy = "none"
        val medi = "none"
        val medicalCond = "none"
        val mainActivity = UtilFunction()

        // Act
        val result = mainActivity.getSuggestion(location, allergy, medi, medicalCond)

        // Assert
        assertEquals("McChicken", result)
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


    @Test
    fun `calculateDifference should return the correct difference in days`() {
        // Arrange
        val futureDateString = "Mon Dec 05 00:00:00 GMT 2023" // Example future date
        val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US)
        val futureDate = dateFormat.parse(futureDateString)

        // Stubbing the behavior of Calendar.getInstance() to return a fixed date using doReturn
        if (futureDate != null) {
            doReturn(futureDate.time).`when`(calendar).timeInMillis
        }

        // Act
        val result = dateCalculator.calculateDifference(futureDate)

        // Assert
        assertEquals(3L, result) // Adjust the expected difference based on your test case
    }
}