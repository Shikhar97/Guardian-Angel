package com.example.guardianangel

import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.widget.CalendarView
import android.widget.CalendarView.OnDateChangeListener
import android.widget.TextView

const val DEBUG_TAG: String = "MyCycle"

class MyCycle : AppCompatActivity() {

    lateinit var dateTV: TextView
    lateinit var calendarView: CalendarView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_cycle)

        dateTV = findViewById(R.id.idTVDate)
        calendarView = findViewById(R.id.calendarView)

        calendarView
            .setOnDateChangeListener(
                OnDateChangeListener { view, year, month, dayOfMonth ->

                    val date = (dayOfMonth.toString() + "-"
                            + (month + 1) + "-" + year)

                    dateTV.text = date
                })




    }
}