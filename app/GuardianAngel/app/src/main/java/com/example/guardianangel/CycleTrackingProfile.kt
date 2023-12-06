
package com.example.guardianangel

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.NumberPicker.OnValueChangeListener
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import java.util.Calendar
import java.util.TimeZone
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import androidx.core.util.Pair as APair

var cycleLength = 28
var periodLength = 5
class CycleTrackingProfile : AppCompatActivity() {
    private lateinit var extendedfab: ExtendedFloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.cycle_tracking)

        findViewById<TextInputLayout>(R.id.cyclelength).editText?.text =
            Editable.Factory.getInstance().newEditable(28.toString())
        findViewById<TextInputLayout>(R.id.periodlength).editText?.text =
            Editable.Factory.getInstance().newEditable(5.toString())

        val today = MaterialDatePicker.todayInUtcMilliseconds()
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

        calendar.timeInMillis = today
        calendar[Calendar.MONTH] = Calendar.NOVEMBER
        val startYear = calendar.timeInMillis

        calendar.timeInMillis = today
        calendar[Calendar.DATE] = Calendar.DECEMBER
        val endYear = calendar.timeInMillis

        calendar[Calendar.MONTH] = Calendar.DECEMBER
        val december = calendar.timeInMillis


        val constraintsBuilder =
            CalendarConstraints.Builder()
                .setStart(startYear)
                .setEnd(MaterialDatePicker.todayInUtcMilliseconds())
                .setOpenAt(december)

        val dateRangePicker =
            MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select dates")
                .setSelection(
                    APair(
                        MaterialDatePicker.thisMonthInUtcMilliseconds(),
                        MaterialDatePicker.todayInUtcMilliseconds()
                    )
                )
                .setInputMode(MaterialDatePicker.INPUT_MODE_TEXT)
                .setCalendarConstraints(constraintsBuilder.build())
//                .setTheme(R.style.ThemeOverlay_MaterialComponents_MaterialCalendar)
                .build()

        extendedfab = findViewById(R.id.extended_fab)
        extendedfab.setOnClickListener {
            dateRangePicker.show(supportFragmentManager, "datePickerTag")
            Log.d("daysDiff", dateRangePicker.selection.toString())

        }

//        val cycleButtonClick = findViewById<ImageView>(R.id.myCalendar)
//        cycleButtonClick.setOnClickListener {
//                putString(
//                    "lengthcycle",
//                    findViewById<TextInputLayout>(R.id.cyclelength).editText?.text.toString()
//                )
//                putString(
//                    "lengthperiod",
//                    findViewById<TextInputLayout>(R.id.periodlength).editText?.text.toString()
//                )


//            val intent = Intent(this, MyCycle::class.java)
//            cycleLength = lengthcycle.text.toString().toInt()
//            periodLength = lengthperiod.text.toString().toInt()
//            intent.putExtra("cycleLength", cycleLength)
//            intent.putExtra("periodLength", periodLength)
//            startActivity(intent)
    }
}
