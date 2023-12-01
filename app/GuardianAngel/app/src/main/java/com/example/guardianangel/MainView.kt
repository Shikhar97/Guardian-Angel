package com.example.guardianangel

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import kotlin.math.abs


class MainView : AppCompatActivity() {
    lateinit var calendarView: MaterialCalendarView
    private val dateCalculator = DateCalculator()


    @SuppressLint("ClickableViewAccessibility")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_main)

        cycleLength = intent.getIntExtra("cycleLength", 28)
        periodLength = intent.getIntExtra("periodLength", 5)
        var startdate = intent.getStringExtra("lastperioddate")
//        Log.d("startdate", startdate.toString())

        var futureDate = startdate?.let { dateCalculator.calculateNextDate(it, cycleLength) }


        var daysDiff = futureDate?.let { dateCalculator.calculateDifference(it) }

        var periodview = findViewById<TextView>(R.id.periodText)
        val formattedfutureDate = SimpleDateFormat("MMM dd").format(futureDate)
        var ptext = ""

        if (daysDiff != null) {
            if(daysDiff > 0) {
                ptext = getString(R.string.days_until_period, "$daysDiff days left", formattedfutureDate)
            }
            else if (daysDiff.equals(0)){
                ptext = getString(R.string.days_until_period, "Today is the day!", formattedfutureDate)
            }
            else{
                ptext = getString(R.string.days_until_period, "Expected ${daysDiff?.let { abs(it) }} days ago", formattedfutureDate)
            }
        }

//        Log.d("daysDiff", ptext)
        periodview.text = ptext

        calendarView = findViewById<MaterialCalendarView>(R.id.calendarView)
        calendarView.setOnTouchListener { _, _ ->  false }


        val today = LocalDate.now()
        val year = today.year
        val month = today.monthValue
        val date = today.dayOfMonth

        calendarView.state().edit()
            .setMinimumDate(CalendarDay.from(year, month, 1))
            .setMaximumDate(CalendarDay.from(year, month, date))
            .setCalendarDisplayMode(CalendarMode.WEEKS)
            .commit();

        calendarView.selectionMode = MaterialCalendarView.SELECTION_MODE_NONE;
        calendarView.setDateSelected(CalendarDay.from(year, month, date), true)

    }


}
class DateCalculator {

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