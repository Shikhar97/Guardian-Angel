package com.example.guardianangel

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Before
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
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
}