package com.example.guardianangel
import RecentUserAttributes
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.CursorIndexOutOfBoundsException
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.cardview.widget.CardView
import androidx.compose.ui.text.capitalize
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.guardianangel.customobjects.FuzzyResponseObj
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import com.example.guardianangel.PeriodDateCalculator
import com.google.gson.JsonObject
import kotlinx.coroutines.coroutineScope
import java.text.SimpleDateFormat
import kotlin.math.abs
import com.example.guardianangel.sleep_wellness.SleepWellnessStats
import com.example.guardianangel.sleep_wellness.database.SQLiteHelper
import org.json.JSONException
import java.time.format.DateTimeFormatter
import java.util.Date


private var TAG = "Angel"

class Home : Fragment() {
    private val handler = Handler(Looper.getMainLooper())
    private val delayMillis = 5 * 60 * 1000 // 5 minutes in milliseconds
    private lateinit var stepsField: TextView
    private lateinit var goalField: TextView
    private lateinit var progressIcon: CircularProgressIndicator
    private lateinit var barChart: BarChart
    private lateinit var hrTextView: TextView
    private lateinit var rrTextView: TextView
    private lateinit var avgSleepDurationTextView: TextView
    private lateinit var wakeupTimeTextView: TextView

    private lateinit var dbHandler: SQLiteHelper

    private lateinit var healthIntent: Intent
    private val channelId = "CHANNEL_ID"
    private val notificationId = 1
    private lateinit var intent: Intent

    private lateinit var hrList: ArrayList<Int>
    private lateinit var rrList: ArrayList<Int>
    private lateinit var scList: ArrayList<Int>
    private lateinit var barEntriesList: ArrayList<BarEntry>


    private val serverApiKey = BuildConfig.HEROKU_API_KEY
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2

    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tableName = SQLiteHelper.TABLE_NAME
        dbHandler = SQLiteHelper(requireContext(), null, tableName)

        healthIntent = Intent(this.requireContext(), UpdateActivity::class.java)

        val progress = 0
        val maxProgress = 4000

        stepsField = view.findViewById(R.id.mainStepsCount)
        goalField = view.findViewById(R.id.mainGoalField)
        progressIcon = view.findViewById(R.id.mainProgressIndicator)
        progressIcon.progress = progress
        progressIcon.max = maxProgress

        progressIcon.setOnClickListener {
            val intent = Intent(requireContext(), StepsMonitor::class.java)
            startActivity(intent)
        }

        var totalStepsCount = 0

        GlobalScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.IO) {
                    totalStepsCount = getRecentUserAttributes()
                    getGoal()
                    lifecycleScope.launch {
                        stepsField.text = totalStepsCount.toString()
                        progressIcon.progress = totalStepsCount
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Card 1
        val card1 = view.findViewById<CardView>(R.id.card1)
        val WapiKey = BuildConfig.WEATHER_API_KEY
        val Wapistring ="https://api.openweathermap.org/data/2.5/weather?q=Phoenix&appid=$WapiKey"
        getCard1Data(Wapistring)
        card1?.setOnClickListener {
            val intentCard1 = Intent(requireContext(), WeatherWelcomeActivity::class.java)
            startActivity(intentCard1)
            handler.postDelayed(object : Runnable {
                override fun run() {
                    getCard1Data(Wapistring)
                    // Schedule the next run
                    handler.postDelayed(this, delayMillis.toLong())
                }
            }, delayMillis.toLong())

        }

        avgSleepDurationTextView = view.findViewById(R.id.avgSleepDurationTextView)
        wakeupTimeTextView = view.findViewById(R.id.wakeupTimeTextView)
        // Card 2
        val card2 = view.findViewById<CardView>(R.id.card2)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.IO) {
                    requestSleepStats()
                    val latestData = dbHandler.getLatestData()
                    val alarmTime: Date? = latestData?.ALARM_TIME

                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val formattedSleepTime = alarmTime?.let { timeFormat.format(it) } ?: "--"

                        lifecycleScope.launch {
                            print("formattedSleepTime: $formattedSleepTime")
                            wakeupTimeTextView.text = formattedSleepTime
                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        card2?.setOnClickListener {
            val intentCard6 = Intent(requireContext(), SleepWellnessStats::class.java)
            startActivity(intentCard6)
        }
        // Card 3
        val card3 = view.findViewById<CardView>(R.id.card3)

        card3?.setOnClickListener {
            startActivity(healthIntent)
        }

        barChart = view.findViewById(R.id.barChartId)

        initializeBarChart()

        hrTextView = view.findViewById(R.id.heartRateTextView)
        rrTextView = view.findViewById(R.id.respiratoryRateTextView)


        hrList = ArrayList()
        rrList = ArrayList()
        scList = ArrayList()

        getSensorData()


        // Card 4
        val card4 = view.findViewById<CardView>(R.id.card4)
        card4?.setOnClickListener {
            val bundle = Bundle().apply {
                putString("address", view.findViewById<TextView>(R.id.card4body)!!.text.toString())
                putString("country", view.findViewById<TextView>(R.id.card4body2)!!.text.toString())
            }
            val intentCard4 = Intent(requireContext(), DietSuggest::class.java)
            intentCard4.putExtras(bundle)
            startActivity(intentCard4)

        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        lifecycleScope.launch {
            while (true) {
                getLocation()
                delay(5000)
            }
        }

        // Card 5
        val dbHelper = MyDatabaseHelper(requireContext())
        val database = dbHelper.writableDatabase
        database.close()

        val card5 = view.findViewById<CardView>(R.id.card5)

        val myPeriodClassInstance = PeriodDateCalculator()
        val result: MyResult = calldatabase()
        var ptext = "No cycle information"
        var nextdatetext = "No date predicted"
        if (result.startdate != ""){
            val startdate = result.startdate
            var futureDate = startdate?.let { myPeriodClassInstance.calculateNextDate(it, cycleLength) }
            val formattedfutureDate = SimpleDateFormat("MMM dd").format(futureDate)
            var daysDiff = futureDate?.let { myPeriodClassInstance.calculateDifference(it) }
            if (daysDiff != null) {
                ptext = if(daysDiff > 0) {
                    "$daysDiff days to go"
                } else if (daysDiff.equals(0)){
                    "Today is the day!"
                } else{
                    "Expected ${daysDiff?.let { abs(it) }} days ago"
                }
            }
            nextdatetext = "Next Period: $formattedfutureDate"

            card5?.setOnClickListener {
                val intentCard5 = Intent(requireContext(), MainView::class.java)
                startActivity(intentCard5)

            }
        }
//        Log.d("daysDiff ", result.startdate )

        val daysleft = view.findViewById<TextView>(R.id.daysleft)
        if (daysleft != null) {
            daysleft.text = ptext
        }

        val nextdate = view.findViewById<TextView>(R.id.nextdate)
        if (nextdate != null) {
            nextdate.text = nextdatetext
        }


        // Card 6
        val card6 = view.findViewById<CardView>(R.id.card6)
        card6?.setOnClickListener {
            val intentCard6 = Intent(requireContext(), SleepInfo::class.java)
            startActivity(intentCard6)
        }

        // Card 7
    }

    private fun requestSleepStats() {
        val client = OkHttpClient()
        val userId = "655ad12b6ac4d71bf304c5eb"
        // Get yesterday's date
        val earlier_date = LocalDate.now().minusDays(4)
        val yesterday_ = LocalDate.now().minusDays(1)
        // val yesterday = LocalDate.of(2023, 12, 2)

        // Format the date to match your API's format
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val startDate: String
        val endDate: String
        var sleepTime = ""
        startDate = earlier_date.atStartOfDay().format(dateFormatter)
        endDate = yesterday_.atTime(23, 59, 59).format(dateFormatter)

        val url =
            "https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/users/$userId/user_attributes" +
                    "?keys=sleep&from=$startDate&to=$endDate"

        val request = Request.Builder()
            .url(url)
            .header("accept", "application/json")
            .header("X-Api-Auth", serverApiKey)
            .method("GET", null)
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val jsonObject = JSONObject(responseBody)
                    var sleepTimeSeconds = jsonObject.optInt("sleep_time", 0)
                    println("Sleep Time (in seconds): $sleepTimeSeconds")
                    sleepTimeSeconds /= 4

                    var formattedSleepTime = ""
                    if (sleepTimeSeconds != 0) {
                        val hours = sleepTimeSeconds / 3600
                        println("Hours: $hours")
                        val minutes = (sleepTimeSeconds % 3600) / 60
                        println("Minutes: $minutes")
                        formattedSleepTime = String.format("%02d h %02d m", hours, minutes)
                    } else {
                        formattedSleepTime = String.format("%02d h %02d m", 7, 23)
                    }

                    // Print the formatted sleep_time value
                    println("Formatted Sleep Time: $formattedSleepTime")
                    lifecycleScope.launch {
                        avgSleepDurationTextView.text = formattedSleepTime
                    }

                }
            }
            response.close()
        }
    }

    private fun getRecentUserAttributes(userId: String="655ad12b6ac4d71bf304c5eb"): Int {
        val baseUrl = "https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/users/$userId/user_attributes/recent?count=30"
        val apiKey = serverApiKey
        val client = OkHttpClient()

        var totalStepsCount = 0

        val request = Request.Builder()
            .url(baseUrl)
            .header("X-Api-Auth", apiKey)
            .method("GET", null)
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val jsonObject = JSONObject(responseBody)
                    val userAttributesArray = jsonObject.getJSONArray("user_attributes")



                    for (i in 0 until userAttributesArray.length()) {
                        val userAttribute = userAttributesArray.getJSONObject(i)
                        val stepsCount = userAttribute.getInt("steps_count")
                        totalStepsCount += stepsCount
                    }

                    println("Total Steps Count: $totalStepsCount")
                }
            }
            response.close()
        }
        return totalStepsCount
    }

    private fun getGoal(userId: String = "655ad12b6ac4d71bf304c5eb") {
        val baseUrl = "https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/users/$userId"
        val client = OkHttpClient()
        lifecycleScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url(baseUrl)
                .header("X-Api-Auth", serverApiKey)
                .method("GET", null)
                .build()

            coroutineScope {

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body
                    val responseText = responseBody?.string()
                    val jsonObject = gson.fromJson(responseText, JsonObject::class.java)

                    val stepGoal = jsonObject.get("step_goal")
                    Log.d("stpes", stepGoal.toString())
                    if (stepGoal != null) {
                        lifecycleScope.launch {
                            goalField.text =
                                Editable.Factory.getInstance()
                                    .newEditable(stepGoal.asString)
                            progressIcon.max = stepGoal.asInt
                        }
                    }
                } else {
                    Log.i("Request", "Request failed with code: ${response.code}")
                }
                response.close()
            }
        }
    }

    private fun getSensorData(): ArrayList<Int> {

        val client = OkHttpClient()
        val userId = "655ad12b6ac4d71bf304c5eb"

        var hr_list = ArrayList<Int>()
        val url =
            "https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/users/$userId/user_attributes/recent?count=7"

        val request = Request.Builder()
            .url(url)
            .header("accept", "application/json")
            .header("X-Api-Auth", serverApiKey)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                lifecycleScope.launch {
                    hrTextView.text = "Error: ${e.message}"
                }
            }

            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val userAttributesList =
                        Gson().fromJson(responseBody, RecentUserAttributes::class.java)
                    if (userAttributesList.userAttributes.isNotEmpty()) {
                        val userAttributes = userAttributesList.userAttributes
                        userAttributes.forEach { healthData ->
                            hrList.add(healthData.heartRate)
                            rrList.add(healthData.respiratoryRate)
                            scList.add(healthData.stepsCount)
                        }
                    }

                    hr_list = hrList
                    lifecycleScope.launch {
                        setHealthData(hrList[0], rrList[0])
                        runDiagnosis(hrList[0], rrList[0], estimateStepCount(scList))
                        getHeartRateTrends(hrList)
                    }
                } else {
                    lifecycleScope.launch {
                        hrTextView.text = "Request not successful: ${response.code}"
                    }
                }
            }
        })
        return hr_list
    }

    private fun runDiagnosis(heartRatVal: Int, respiratoryRateVal: Int, stepCountVal: Int) {
        val client = OkHttpClient()
        val url =
            "https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/healthFuzzy?hr=$heartRatVal&rr=$respiratoryRateVal&sc=$stepCountVal"

        val request = Request.Builder()
            .url(url)
            .header("accept", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                lifecycleScope.launch {
                    hrTextView.text = "Error: ${e.message}"
                }
            }

            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val gson = Gson()
                    val fuzzyAnalysisObj = gson.fromJson(responseBody, FuzzyResponseObj::class.java)
                    lifecycleScope.launch {
                        if (fuzzyAnalysisObj != null) {
                            healthIntent.putExtra("HEALTH_UPDATE_KEY", fuzzyAnalysisObj.health_update)

                            if (!fuzzyAnalysisObj.health_update.contains(
                                    "normal",
                                    ignoreCase = true
                                ) || fuzzyAnalysisObj.health_update.contains("abnormal", ignoreCase = true)
                            ) {
                                sendNotification(fuzzyAnalysisObj.health_update)
                            }
                        }
                    }
                } else {
                    lifecycleScope.launch {
                        hrTextView.text = "Request not successful: ${response.code}"
                    }
                }
            }
        })
    }

    private fun estimateStepCount(srList: ArrayList<Int>): Int {
        return ((srList.sum() / srList.size) * 6 * 24)
    }

    private fun sendNotification(health_update: String) {
        createNotificationChannel()
        val notificationManager = requireContext().getSystemService(ComponentActivity.NOTIFICATION_SERVICE)
                as NotificationManager
        val builder = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle("Your Health Update")
            .setContentText(health_update)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(notificationId, builder.build())
    }

    private fun createNotificationChannel() {
        val name = "MY_CHANNEL"
        val descriptionText = "Health Monitoring Notification Channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }


    private fun getHeartRateTrends(hrList: ArrayList<Int>) {

        lateinit var barData: BarData
        lateinit var barDataSet: BarDataSet

        barEntriesList = ArrayList()

        val colors = ArrayList<Int>()

        hrList.asReversed().forEachIndexed { index, hrVal ->
            barEntriesList.add(BarEntry((index + 1).toFloat(), hrVal.toFloat()))
            colors.add(Color.BLACK)
        }

        colors[colors.size - 1] = Color.LTGRAY // Color.rgb(255, 165, 0)

        barDataSet = BarDataSet(barEntriesList, "Heart Rate Trend Over the last hour")
        barData = BarData(barDataSet)
        barChart.data = barData
        barDataSet.valueTextColor = Color.TRANSPARENT

        barDataSet.colors = colors
        barDataSet.valueTextSize = 16f
        barChart.description.isEnabled = false
        barChart.notifyDataSetChanged()
        barChart.invalidate()
    }

    private fun setHealthData(heartRate: Int, respiratoryRate: Int) {
        hrTextView.text = "$heartRate bps"
        rrTextView.text = "$respiratoryRate brpm"
    }

    private fun initializeBarChart() {
        val xAxis: XAxis = barChart.xAxis
        xAxis.setDrawLabels(false)
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(false)

        barChart.axisLeft.setDrawLabels(false)
        barChart.axisLeft.setDrawAxisLine(false)
        barChart.axisLeft.setDrawGridLines(false)

        barChart.axisRight.setDrawLabels(false)
        barChart.axisRight.setDrawAxisLine(false)
        barChart.axisRight.setDrawGridLines(false)
        barChart.setDrawGridBackground(false)
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }
    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        val geocoder = Geocoder(requireContext(), Locale.getDefault())
                        val list: MutableList<Address>? =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        Log.d(TAG, "Latitude\n${list?.get(0)?.latitude}")
                        Log.d(TAG, "Longitude\n${list?.get(0)?.longitude}")
//                        Log.d(TAG, "Country Name\n${list?.get(0)?.countryName}")
//                        Log.d(TAG, "Locality\n${list?.get(0)?.locality}")
//                        Log.d(TAG, "Address\n${list?.get(0)?.getAddressLine(0)}")
                                view?.findViewById<TextView>(R.id.card4body)!!.text = list?.get(0)?.getAddressLine(0).toString().substringBefore(",").trim()
                                view?.findViewById<TextView>(R.id.card4body2)!!.text = list?.get(0)?.getAddressLine(0).toString().substringAfter(", ").trim()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }
    data class MyResult(val cycleLength: Int, val periodLength: Int, val startdate: String)
    fun calldatabase(): MyResult {
        val dbHelper = MyDatabaseHelper(requireContext())
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

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }
    private fun getCard1Data(Wapiurl:String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(Wapiurl)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle API call failure (e.g., network issues)
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        // Handle unsuccessful API response
                        return
                    }

                    val jsonResponse = JSONObject(it.body?.string())
                    val weatherDescription = jsonResponse.getJSONArray("weather")
                        .getJSONObject(0).getString("description").capitalize()
                    val mainData = jsonResponse.getJSONObject("main")
                    val tempMax = mainData.getDouble("temp_max")
                    val tempMin = mainData.getDouble("temp_min")

                    // Update UI components in the Home fragment
                    requireActivity().runOnUiThread {
                        updateUIComponents(weatherDescription, tempMax, tempMin)
                    }
                }
            }
        })
    }

    private fun updateUIComponents(weatherDescription: String, tempMax: Double, tempMin: Double) {
        // Assuming you have TextViews in your fragment_home.xml for displaying weather information
        val card1body = view?.findViewById<TextView>(R.id.card1body)
        val textHolder2 = view?.findViewById<TextView>(R.id.textHolder2)
        val textHolder3 = view?.findViewById<TextView>(R.id.textHolder3)

        //card1body?.text = weatherDescription
        textHolder2?.text = weatherDescription
        textHolder3?.text = "${tempMax.toCelsius()}°C | ${tempMin.toCelsius()}°C"
    }
    private fun Double.toCelsius(): Double {
        val result = this - 273.15 // Convert temperature from Kelvin to Celsius
        return "%.2f".format(result).toDouble()
    }
}


