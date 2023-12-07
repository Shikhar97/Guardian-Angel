
package com.example.guardianangel

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone
import kotlin.Long
import kotlin.Pair
import androidx.core.util.Pair as APair


var cycleLength = 28
var periodLength = 5
class CycleTrackingProfile : AppCompatActivity() {
    private val gson = Gson()
    private val apiKey = BuildConfig.HEROKU_API_KEY
    private val tag = "Angel"

    private lateinit var extendedfab: ExtendedFloatingActionButton
    private lateinit var savefab: ExtendedFloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.cycle_tracking)
        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)

        topAppBar.setNavigationOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.help -> {
                    // Handle more item (inside overflow menu) press
                    true
                }

                else -> false
            }
        }

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
                .setEnd(today)
                .setOpenAt(today)
                .setValidator(DateValidatorPointBackward.now())
        Log.d("daysDiff ", "Today: $today, Start Year: $startYear")

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
        var startDate : Long?
        var endDate : Long?

        val dateFormatUtc = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy")
        dateFormatUtc.timeZone = TimeZone.getTimeZone("UTC")
        var formattedDateUtc = dateFormatUtc.format(MaterialDatePicker.thisMonthInUtcMilliseconds())
        extendedfab = findViewById(R.id.extended_fab)
        extendedfab.setOnClickListener {
            dateRangePicker.show(supportFragmentManager, "datePickerTag")

            dateRangePicker.addOnPositiveButtonClickListener { selection ->
                startDate = selection.first
                endDate = selection.second
                Log.d("daysDiff startDate", startDate.toString())
                Log.d("daysDiff endDate", endDate.toString())

                dateFormatUtc.timeZone = TimeZone.getTimeZone("UTC")
                formattedDateUtc = dateFormatUtc.format(startDate)

                Log.d("daysDiff formatteddate ",  formattedDateUtc.toString())

            }

        }
        val cycleLengthInputLayout = findViewById<TextInputLayout>(R.id.cyclelength)
        val periodLengthInputLayout = findViewById<TextInputLayout>(R.id.periodlength)

        val cycleLengthValue = cycleLengthInputLayout.editText?.text.toString()
        val periodLengthValue = periodLengthInputLayout.editText?.text.toString()


        savefab = findViewById(R.id.save_cycle_fab)
        savefab.setOnClickListener {
            val dbHelper = MyDatabaseHelper(this)

            val database = dbHelper.writableDatabase

            val values = ContentValues().apply {
                put("CYCLE_LENGTH", cycleLengthValue)
                put("PERIOD_LENGTH", periodLengthValue)
                put("LAST_PERIOD_DATE", formattedDateUtc.toString())

            val intent = Intent(this@CycleTrackingProfile, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
            }
            val newRowId = database.insert("cycletable", null, values)
            database.close()
        }

//            val intent = Intent(this, MyCycle::class.java)
//            cycleLength = lengthcycle.text.toString().toInt()
//            periodLength = lengthperiod.text.toString().toInt()
//            intent.putExtra("cycleLength", cycleLength)
//            intent.putExtra("periodLength", periodLength)
//            startActivity(intent)

    }
    private fun postUserCycle(userId: String = "655ad12b6ac4d71bf304c5eb")  {
        val result: ArrayList<Any> = ArrayList()
        val baseUrl = "https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/users/$userId"
        val client = OkHttpClient()
        var congestion = mutableListOf<Job>()

        runBlocking {
            var request = Request.Builder()
                .url(baseUrl)
                .header("X-Api-Auth", apiKey)
                .method("POST", null)
                .build()


            val job = launch(context = Dispatchers.Default) {
                coroutineScope {
                    Log.d(tag, request.toString())
                    var response = client.newCall(request).execute()

                    if (response.isSuccessful) {
                        val responseBody = response.body
                        val responseText = responseBody?.string()
                        val jsonObject = gson.fromJson(responseText, JsonObject::class.java)
                        Log.i(tag, jsonObject.toString())
                        result.add(jsonObject.get("name").asString)
                    } else {
                        Log.i(tag, "Request failed with code: ${response.code}")
                    }
                    response.close()
                }
            }
        }
    }
}

