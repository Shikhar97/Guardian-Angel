package com.example.guardianangel
import RecentUserAttributes
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.cardview.widget.CardView
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
import java.util.Locale

private var TAG = "Angel"

class Home : Fragment() {

    private lateinit var stepsField: TextView
    private lateinit var progressIcon: CircularProgressIndicator
    private lateinit var barChart: BarChart
    private lateinit var hrTextView: TextView
    private lateinit var rrTextView: TextView
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


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stepsField = view.findViewById(R.id.mainStepsCount)
        progressIcon = view.findViewById(R.id.mainProgressIndicator)

        progressIcon.setOnClickListener {
            val intent = Intent(requireContext(), StepsMonitor::class.java)
            startActivity(intent)
        }

        var totalStepsCount = 0

        GlobalScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.IO) {
                    totalStepsCount = getRecentUserAttributes()
                    stepsField.text = totalStepsCount.toString()
                    progressIcon.progress = totalStepsCount
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Card 1
        val card1 = view.findViewById<CardView>(R.id.card1)
        card1?.setOnClickListener {
            val intentCard1 = Intent(requireContext(), WeatherWelcomeActivity::class.java)
            startActivity(intentCard1)

        }
        // Card 2
        // Card 3
        val card3 = view.findViewById<CardView>(R.id.card3)
        card3?.setOnClickListener {
            val intentCard3 = Intent(requireContext(), UpdateActivity::class.java)
            startActivity(intentCard3)
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
            val intentCard4 = Intent(requireContext(), DietSuggest::class.java)
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
        // Card 6
        val card6 = view.findViewById<CardView>(R.id.card6)
        card6?.setOnClickListener {
            val intentCard6 = Intent(requireContext(), SleepInfo::class.java)
            startActivity(intentCard6)
        }

        // Card 7
    }

    private fun getRecentUserAttributes(userId: String="655ad12b6ac4d71bf304c5eb"): Int {
        val baseUrl = "https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/users/$userId/user_attributes/recent?count=50"
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

    private fun getSensorData(): ArrayList<Int> {
        val client = OkHttpClient()
        val userId = "655ad12b6ac4d71bf304c5eb"

        var hr_list = ArrayList<Int>()
        val url =
            "https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/users/$userId/user_attributes/recent?count=7"

        val request = Request.Builder()
            .url(url)
            .header("accept", "application/json")
            .header("X-Api-Auth", "<api_key>")
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
                            intent.putExtra("HEALTH_UPDATE_KEY", fuzzyAnalysisObj.health_update)

                            if (!fuzzyAnalysisObj.health_update.contains(
                                    "normal",
                                    ignoreCase = true
                                )
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
            colors.add(Color.GRAY)
        }

        colors[colors.size - 1] = Color.rgb(255, 165, 0)

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
        hrTextView.text = "$heartRate beats/min"
        rrTextView.text = "$respiratoryRate breaths/min"
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
                        Log.d(TAG, "Country Name\n${list?.get(0)?.countryName}")
                        Log.d(TAG, "Locality\n${list?.get(0)?.locality}")
                        Log.d(TAG, "Address\n${list?.get(0)?.getAddressLine(0)}")
//                        val homeFragment = supportFragmentManager.findFragmentById(R.id.frame_layout)
//                        if (homeFragment != null) {
//                            val homeFragmentView = homeFragment.view
//                            if (homeFragmentView != null) {
                                view?.findViewById<TextView>(R.id.card4body)!!.text = list?.get(0)?.getAddressLine(0).toString().substringBefore(",").trim()
                                view?.findViewById<TextView>(R.id.card4body2)!!.text = list?.get(0)?.getAddressLine(0).toString().substringAfter(", ").trim()

//                            }
//                        }
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

}