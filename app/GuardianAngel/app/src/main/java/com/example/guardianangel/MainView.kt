package com.example.guardianangel

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import kotlin.math.abs
import kotlin.random.Random

class MainView : AppCompatActivity() {
    lateinit var calendarView: MaterialCalendarView
    private val dateCalculator = DateCalculator()

    var normalfacts = arrayOf("The average cycle for a woman is 28 days, but it can be anything from 22 to 36 days long.", "Ovulation normally happens about two weeks before your next period.", "Periods can be irregular due to stress or illness", "Fluctuations in weight can affect your period", "Abnormal bleeding can indicate more serious health issues.")
    var ovulationfacts = arrayOf("Sperm can live up to 5 days", "A woman is born with millions of eggs")
    var periodfacts = arrayOf("Your periods get worse when it is cold","You can still get pregnant if you’re on your period")

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
        var suptext = normalfacts.random()

        if (daysDiff != null) {
            if(daysDiff > 0) {
                ptext = getString(R.string.days_until_period, "$daysDiff days left", formattedfutureDate)
                if(daysDiff > 14) {
                    suptext = ovulationfacts.random()
                }
                else if(daysDiff < 5) {
                    suptext = periodfacts.random()
                }
            }
            else if (daysDiff.equals(0)){
                ptext = getString(R.string.days_until_period, "Today is the day!", formattedfutureDate)
                suptext = "No fact today! Just stay hydrated and take care of yourself."
            }
            else{
                ptext = getString(R.string.days_until_period, "Expected ${daysDiff?.let { abs(it) }} days ago", formattedfutureDate)
                suptext = normalfacts.random()
            }
        }

//        Log.d("daysDiff", ptext)
        periodview.text = ptext

        val infobutton = findViewById<FloatingActionButton>(R.id.floatingActionButton2)
        infobutton.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(resources.getString(R.string.title))
                .setMessage(resources.getString(R.string.supporting_text, suptext))
                .setNeutralButton(resources.getString(R.string.cancel)) { dialog, which ->
                }
                .show()
        }

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