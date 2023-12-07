package com.example.guardianangel

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ExpandableListView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.time.Instant
import java.util.Collections
import java.util.Date
import kotlin.math.abs


class MainView : AppCompatActivity() {
    lateinit var calendarView: MaterialCalendarView
    private val dateCalculator = DateCalculator()
    private val gson = Gson()
    private val apiKey = BuildConfig.HEROKU_API_KEY
    private val tag = "Angel"

    var normalfacts = arrayOf("The average cycle for a woman is 28 days, but it can be anything from 22 to 36 days long.", "Ovulation normally happens about two weeks before your next period.", "Periods can be irregular due to stress or illness", "Fluctuations in weight can affect your period", "Abnormal bleeding can indicate more serious health issues.")
    var ovulationfacts = arrayOf("Sperm can live up to 5 days", "A woman is born with millions of eggs")
    var periodfacts = arrayOf("Your periods get worse when it is cold","You can still get pregnant if you’re on your period")

    @SuppressLint("ClickableViewAccessibility")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_main)

//        val userAttributes = getUserCycle()

//        cycleLength = intent.getIntExtra("cycleLength", 28)
//        periodLength = intent.getIntExtra("periodLength", 5)
//        var startdate = intent.getStringExtra("lastperioddate")
        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        topAppBar.setNavigationOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
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
        var startdate = "Dec 07"
        if (cursor.moveToFirst()) {
            cursor.getInt(cursor.getColumnIndexOrThrow("CYCLE_LENGTH")).also { cycleLength = it }
            periodLength = cursor.getInt(cursor.getColumnIndexOrThrow("PERIOD_LENGTH"))
            startdate = cursor.getString(cursor.getColumnIndexOrThrow("LAST_PERIOD_DATE"))
        }

        cursor.close()
        database.close()


//        cycleLength = userAttributes[0].toString().toInt()
//        periodLength = userAttributes[1].toString().toInt()
//        var startdate = userAttributes[2].toString()
        Log.d("daysDiff cyclelength", cycleLength.toString())
        Log.d("daysDiff period", periodLength.toString())
        Log.d("daysDiff startdate", startdate.toString())

        var futureDate = startdate?.let { dateCalculator.calculateNextDate(it, cycleLength) }

        val localDate: LocalDate? = futureDate?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()

        Log.d("daysDiff", localDate.toString())



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
                .setTitle(resources.getString(R.string.dialog_title))
                .setMessage(resources.getString(R.string.dialog_supporting_text, suptext))
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

        val tomorrow = LocalDate.now().plusDays(1)
        val tomyear = tomorrow.year
        val tommonth = tomorrow.monthValue
        val tomdate = tomorrow.dayOfMonth
//        val futureDates = arrayOf(localDate?.let { CalendarDay.from(it.year, it.monthValue, it.dayOfMonth) }, CalendarDay.from(tomyear, tommonth, tomdate))
        val futureDates = Array<CalendarDay?>(periodLength) { index ->
            localDate?.plusDays(index.toLong())?.let {
                CalendarDay.from(it.year, it.monthValue, it.dayOfMonth)
            }
        }

        calendarView.state().edit()
            .setMinimumDate(CalendarDay.from(year, month, 1))
            .setMaximumDate(CalendarDay.from(year, month, 31))
            .setCalendarDisplayMode(CalendarMode.MONTHS)
            .commit();

        calendarView.selectionMode = MaterialCalendarView.SELECTION_MODE_NONE;
        for (date in futureDates) {
            calendarView.setDateSelected(date, true)
        }


        val symptoms = arrayListOf(
            "Bloating",
            "Insomnia",
            "Diarrhoea",
            "Cramps",
            "Irritability",
            "Flow",
            "Spotting",
            "Acne",
            "Constipation"
        )


        var mListView = findViewById<ListView>(R.id.symptoms_list)
        val customAdapter = RatingListCustomAdapter(this, symptoms)
        mListView.adapter = customAdapter

        var recbutton = findViewById<Button>(R.id.recbutton)
        recbutton.setOnClickListener {
            val hashMapValues = customAdapter.getHashMap()
            Log.i("daysDiff HashMapValues", hashMapValues.toString())
            val suggestions = provideReproductiveHealthSuggestions(hashMapValues)
            Log.i("daysDiff suggestions", suggestions)

            MaterialAlertDialogBuilder(this)
                .setTitle("Some suggestions for you!")
                .setMessage(resources.getString(R.string.dialog_supporting_text, suggestions))
                .setNeutralButton(resources.getString(R.string.cancel)) { dialog, which ->
                }
                .show()

            }

        }

    fun provideReproductiveHealthSuggestions(symptomRatings: Map<String, Float>): String {
        val insomniaRating = symptomRatings["Insomnia"] ?: 0.0f
        val crampsRating = symptomRatings["Cramps"] ?: 0.0f
        val acneRating = symptomRatings["Acne"] ?: 0.0f
        val irritabilityRating = symptomRatings["Irritability"] ?: 0.0f
        val diarrheaRating = symptomRatings["Diarrhoea"] ?: 0.0f
        val constipationRating = symptomRatings["Constipation"] ?: 0.0f
        val spottingRating = symptomRatings["Spotting"] ?: 0.0f
        val bloatingRating = symptomRatings["Bloating"] ?: 0.0f
        val flowRating = symptomRatings["Flow"] ?: 0.0f

        val suggestions = StringBuilder()

        if (insomniaRating >= 3.0) {
            suggestions.append("You're having trouble sleeping. Consider establishing a consistent sleep routine and minimizing caffeine intake.")
        }

        if (crampsRating >= 3.0) {
            suggestions.append("\nYou're experiencing severe cramps. Applying heat and taking over-the-counter pain relievers may help.")
        }

        if (acneRating >= 3.0) {
            suggestions.append("\nYou're dealing with acne. Ensure a proper skincare routine and consult with a dermatologist for personalized advice.")
        }

        if (irritabilityRating >= 2.0) {
            suggestions.append("\nFeeling irritable? Practice stress-relief techniques and ensure adequate self-care.")
        }
        if (flowRating >= 2.0) {
            suggestions.append("\n On your periods? Hydrate yourself .")
        }
        if (diarrheaRating >= 3.0 && flowRating < 3.0) {
            suggestions.append("\nHigh diarrhea rating with low flow might indicate approaching or light periods. Monitor closely for changes.")
        }

        if (constipationRating >= 3.0) {
            suggestions.append("\nExperiencing constipation? Increase fiber intake and stay hydrated.")
        }

        if (spottingRating >= 4.0) {
            suggestions.append("\nSevere spotting detected. Consult with a healthcare professional for further evaluation.")
        }
        else if (spottingRating >= 2.0) {
            suggestions.append("\nMedium spotting detected. If you are not near periods or expecting pregnancy, consult with a healthcare professional immediately.")
        }

        if (bloatingRating >= 2.0) {
            suggestions.append("\nFeeling bloated? Avoid gas-inducing foods and consider light exercises.")
        }

        return if (suggestions.isNotEmpty()) {
            suggestions.toString()
        } else {
            "No specific reproductive health concerns detected."
        }

    }


//    private fun getUserCycle(userId: String = "655ad12b6ac4d71bf304c5eb"): ArrayList<Any> {
//        val result: ArrayList<Any> = ArrayList()
//        val baseUrl = "https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/users/$userId"
//        val client = OkHttpClient()
//        var congestion = mutableListOf<Job>()
//
//        runBlocking {
//            var request = Request.Builder()
//                .url(baseUrl)
//                .header("X-Api-Auth", apiKey)
//                .method("GET", null)
//                .build()
//
//
//            val job = launch(context = Dispatchers.Default) {
//                coroutineScope {
//                    var response = client.newCall(request).execute()
//
//                    if (response.isSuccessful) {
//                        val responseBody = response.body
//                        val responseText = responseBody?.string()
//                        val jsonObject = gson.fromJson(responseText, JsonObject::class.java)
//                        Log.i(tag, jsonObject.toString())
//                        result.add(jsonObject.get("cycleLength").asInt)
//                        result.add(jsonObject.get("periodLength").asInt)
//                        result.add(jsonObject.get("lastperioddate").asInt)
//                    } else {
//                        Log.i(tag, "Request failed with code: ${response.code}")
//                    }
//                    response.close()
//
//                }
//            }
//            congestion.add(job)
//            congestion = Collections.unmodifiableList(congestion)
//            congestion.joinAll()
//        }
//        return result
//    }


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

