package com.example.guardianangel

import android.database.CursorIndexOutOfBoundsException
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class PeriodDateCalculator: AppCompatActivity() {
    data class MyResult(val cycleLength: Int, val periodLength: Int, val startdate: String)

    fun calldatabase(): MyResult {
        val dbHelper = MyDatabaseHelper(this)
        val database = dbHelper.readableDatabase

        val projection = arrayOf("CYCLE_LENGTH", "PERIOD_LENGTH", "LAST_PERIOD_DATE")

        val sortOrder = "id DESC"

        val cursor = database.query(
            "cycletable",
            projection,
            null,
            null,
            null,
            null,
            sortOrder,
            "1" // Limit to 1 result to get the latest row
        )

        // Check if the cursor has results
        var cycleLength = 28
        var periodLength = 5
        var startdate = ""


        try {
            // Check if the cursor has results
            if (cursor.moveToFirst()) {
                cycleLength = cursor.getInt(cursor.getColumnIndexOrThrow("CYCLE_LENGTH"))
                periodLength = cursor.getInt(cursor.getColumnIndexOrThrow("PERIOD_LENGTH"))
                startdate = cursor.getString(cursor.getColumnIndexOrThrow("LAST_PERIOD_DATE"))
            }
        } catch (e: CursorIndexOutOfBoundsException) {
            // Handle the case where the cursor is empty
            e.printStackTrace()
        } finally {
            cursor.close()
            database.close()
        }


        Log.d("daysDiff cyclelength", cycleLength.toString())
        Log.d("daysDiff period", periodLength.toString())
        Log.d("daysDiff startdate", startdate.toString())

        return MyResult(cycleLength, periodLength, startdate)


    }

    fun calculateNextDate(startdate: String, cycleLength: Int): Date {

        val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy")
        val datestart = dateFormat.parse(startdate)

//        Log.d("daysDiff", startdate.toString())
//        Log.d("daysDiff", cycleLength.toString())
        val calendar = Calendar.getInstance()
        calendar.time = datestart
        calendar.add(Calendar.DATE, cycleLength)
        val futureDate = calendar.time

//        Log.d("daysDiff", futureDate.toString())
        return futureDate
    }

    fun calculateDifference(futureDate: Date): Long {

        val now = Calendar.getInstance()
        val thisday = now.time
//        Log.d("daysDiff", thisday.toString())

        val diff = futureDate.time - thisday.time
        val daysDiff = diff / (24 * 60 * 60 * 1000)

//        Log.d("daysDiff", daysDiff.toString())

        return daysDiff
    }


}
