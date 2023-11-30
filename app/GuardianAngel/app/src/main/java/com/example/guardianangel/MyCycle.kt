package com.example.guardianangel

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.util.Calendar


const val DEBUG_TAG: String = "MyCycle"

class MyCycle : AppCompatActivity() {

    lateinit var dateTV: TextView
    lateinit var calendarView: MaterialCalendarView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_cycle)


        dateTV = findViewById(R.id.idTVDate)
        calendarView = findViewById<MaterialCalendarView>(R.id.calendarView)

        calendarView.state().edit()
            .setMinimumDate(CalendarDay.from(2022, 8, 1))
            .setMaximumDate(CalendarDay.from(2024, 12, 31))
            .setCalendarDisplayMode(CalendarMode.MONTHS)
            .commit();


        calendarView.selectionMode = MaterialCalendarView.SELECTION_MODE_RANGE


        val fab: View = findViewById(R.id.floatingActionButton)
        fab.setOnClickListener { view ->
            val selectedDates = calendarView.selectedDates
            Log.d("Dates", selectedDates.toString())
        }
//        cycleButtonClick.setOnClickListener {
//            val intent = Intent(this, MyCycle::class.java)
//            startActivity(intent)
//        }






    }
}
