package com.example.guardianangel

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date


const val DEBUG_TAG: String = "MyCycle"

class MyCycle : AppCompatActivity() {

    lateinit var dateTV: TextView
    lateinit var calendarView: MaterialCalendarView

    private fun convertCalendarDayToDate(calendarDay: CalendarDay): Date {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val dateString = "${calendarDay.year}-${calendarDay.month}-${calendarDay.day}" // Note: month is zero-based
        return dateFormat.parse(dateString)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_cycle)


        calendarView = findViewById<MaterialCalendarView>(R.id.calendarView)

        val today = LocalDate.now()
        val year = today.year
        val month = today.monthValue
        val date = today.dayOfMonth

        calendarView.state().edit()
            .setMinimumDate(CalendarDay.from(2023, 11, 1))
            .setMaximumDate(CalendarDay.from(year, month, date))
            .setCalendarDisplayMode(CalendarMode.MONTHS)
            .commit();


        calendarView.selectionMode = MaterialCalendarView.SELECTION_MODE_RANGE


        val fab: View = findViewById(R.id.floatingActionButton)
        fab.setOnClickListener {
            val selectedDates = calendarView.selectedDates

            Log.d("Dates", selectedDates.toString())

            if (selectedDates.isNullOrEmpty()) {

                Toast.makeText(this, "Select last period dates", Toast.LENGTH_SHORT).show()

            }

            else {
                val firstday = selectedDates.first()

                val lastdate = convertCalendarDayToDate(firstday)



                val dbHelper = MyDatabaseHelper(this)

                val database = dbHelper.writableDatabase

                val values = ContentValues().apply {
                    put("CYCLE_LENGTH", intent.getIntExtra("cycleLength", 28))
                    put("PERIOD_LENGTH", intent.getIntExtra("periodLength", 5))
                    put("LAST_PERIOD_DATE", lastdate.toString())
                }
                val newRowId = database.insert("cycletable", null, values)
                database.close()

                val intent = Intent(this, MainView::class.java)
                intent.putExtra("cycleLength", cycleLength)
                intent.putExtra("periodLength", periodLength)
                intent.putExtra("lastperioddate", lastdate.toString())
                startActivity(intent)

            }
        }


    }
}
