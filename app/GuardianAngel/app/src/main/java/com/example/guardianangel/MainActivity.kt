package com.example.guardianangel

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private var bottomNavigationView: NavigationBarView? = null
    val utilfuncs = UtilFunction()


//    private val applicationScope = CoroutineScope(SupervisorJob())
//    private val appDatabase = UsersDb.AppDatabase.getDatabase(this, applicationScope)
//    private val userDao = appDatabase.userDao()


    private var tag = "Angel"
    private var locations = listOf(
        Pair(33.415791, -111.925850), //McD
        Pair(33.409540, -111.916470), //Home
        Pair(33.421670, -111.920200), //Home
        Pair(33.421740, -111.919650), //ASU
        Pair(34.048927, -111.093735), //Starbucks
        Pair(33.429343, -111.908912), //Starbucks
        Pair(33.4218288, -111.9466686), //Oregano
    )
    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(
                    baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    companion object {
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ).toTypedArray()
    }

    private fun getLocation(): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView?.setOnItemSelectedListener { item ->
            val v = item.itemId
            if (v == R.id.home) {
                val fragment: Fragment = Home()
                val fm: FragmentManager = supportFragmentManager
                fm.beginTransaction().replace(R.id.frame_layout, fragment).commit()
            } else if (v == R.id.details) {
                val fragment: Fragment = Details()
                val fm: FragmentManager = supportFragmentManager
                fm.beginTransaction().replace(R.id.frame_layout, fragment).commit()
            } else if (v == R.id.notifications) {
                val fragment: Fragment = Notification()
                val fm: FragmentManager = supportFragmentManager
                fm.beginTransaction().replace(R.id.frame_layout, fragment).commit()
            } else if (v == R.id.settings) {
                val fragment: Fragment = Settings()
                val fm: FragmentManager = supportFragmentManager
                fm.beginTransaction().replace(R.id.frame_layout, fragment).commit()
            }
            true
        }
        val fragment: Fragment = Home()
        val fm: FragmentManager = supportFragmentManager
        fm.beginTransaction().replace(R.id.frame_layout, fragment).commit()

        fm.executePendingTransactions()

        val homeFragment = supportFragmentManager.findFragmentById(R.id.frame_layout) as Home?
        val homeFragmentView = homeFragment?.view

        // Set the progress (4 out of 10)
        val progress = 0
        val maxProgress = 1000
        val progressPercentage = (progress.toFloat() / maxProgress.toFloat()) * 100
        Log.d("TAG", progressPercentage.toString())

        Handler(Looper.getMainLooper()).post {
            homeFragmentView?.findViewById<CircularProgressIndicator>(R.id.progressIndicator)?.max =
                maxProgress
            homeFragmentView?.findViewById<CircularProgressIndicator>(R.id.progressIndicator)?.progress =
                progress
        }

        lifecycleScope.launch {
            var latitude = 0.0
            var longitude = 0.0
            if (ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions()
            }
            val message = " You are at: \n"
            while (true) {
                getLocation().lastLocation
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            latitude = location.latitude
                            longitude = location.longitude
                            Log.d(
                                tag,
                                "Current location is \n" + "lat : ${latitude}\n" +
                                        "long : ${longitude}\n" + "fetched at ${System.currentTimeMillis()}"
                            )
                        } else {
                            Log.d(
                                tag, "Location not found"
                            )
                        }
                    }
//                val (randomLat, randomLong) = locations[random.nextInt(locations.size)]
//                Log.d(tag, "Randomly picked pair: $randomLat, $randomLong")
//
//                val frag = fm.findFragmentById(R.id.frame_layout)
//                val textView = frag?.view?.findViewById<TextView>(R.id.currentplace)
//                val cardView = frag?.view?.findViewById<CardView>(R.id.suggestion_view)
//                val imageView = cardView?.findViewById<ImageView>(R.id.imageView3)
//                val mapView = frag?.view?.findViewById<MapView>(R.id.mapView)
//
//                mapView?.onCreate(Bundle())  // Make sure to call the necessary lifecycle methods
//                mapView?.onResume()
//                mapView?.getMapAsync { googleMap ->
//                    // Now you have the GoogleMap object, and you can add a marker
//                    val marker = MarkerOptions()
//                        .position(LatLng(randomLat, randomLong)) // Replace with actual coordinates
//                        .title("Marker Title") // Replace with your marker title
//                        .snippet("Marker Snippet") // Replace with your marker snippet
//
//                    googleMap.addMarker(marker)
//                    googleMap.moveCamera(
//                        CameraUpdateFactory.newLatLngZoom(
//                            LatLng(
//                                randomLat,
//                                randomLong
//                            ), 15f
//                        )
//                    )
//                }

//                if (randomLat == 33.415791 && randomLong == -111.925850) {
//                    Log.d(tag, "You are at McDonalds")
//                    textView?.text = "$message Starbucks\n As you have cold"
//                    val coffeeType =
//                        utilfuncs.getSuggestion("starbucks", "none", "none", "diabetic")
//                    if (coffeeType == "Hot Coffee") {
//                        imageView?.setImageResource(R.mipmap.hot_tea)
//
//                    } else {
//                        imageView?.setImageResource(R.mipmap.iced_tea)
//                    }
//
//                } else if (randomLat == 33.4218288 && randomLong == -111.9466686) {
//                    Log.d(tag, "You are at Oregano's Pizza")
//                    textView?.text = "$message Starbucks\n As you have cold"
//                    val coffeeType =
//                        utilfuncs.getSuggestion("starbucks", "none", "none", "diabetic")
//                    if (coffeeType == "Hot Coffee") {
//                        imageView?.setImageResource(R.mipmap.hot_tea)
//
//                    } else {
//                        imageView?.setImageResource(R.mipmap.iced_tea)
//                    }
//
//                } else if (randomLat == 33.429343 && randomLong == -111.908912) {
//                    Log.d(tag, "You are at Starbucks")
//                    textView?.text = "$message Starbucks\n As you have cold"
//                    val coffeeType =
//                        utilfuncs.getSuggestion("starbucks", "none", "none", "diabetic")
//                    if (coffeeType == "Hot Coffee") {
//                        imageView?.setImageResource(R.mipmap.hot_tea)
//
//                    } else {
//                        imageView?.setImageResource(R.mipmap.iced_tea)
//                    }
//                }
                delay(5000)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(tag, "Mapready")
        googleMap.addMarker(
            MarkerOptions()
                .position(LatLng(0.0, 0.0))
                .title("Marker")
        )
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }
}

class UtilFunction {
    fun getSuggestion(
        location: String,
        allergy: String,
        medi: String,
        medicalCond: String
    ): String {
//        var noSugar = false
//        var isMilkAllergic = false
//        val allergiesFlow: Flow<List<String>> = userDao.getAllergies()
//        val medicalConditionsFlow: Flow<List<String>> = userDao.getMedicalConditions()
//        var congestion = mutableListOf<Job>()

//        runBlocking {
//            val job = launch(context = Dispatchers.Default) {
//                coroutineScope {
//                    allergiesFlow.collect { allergies ->
//
//                         Check if any allergy matches "milk"
//                        for (allergy in allergies) {
//                            if (allergy.equals("milk", ignoreCase = true)) {
//                                isMilkAllergic = true
//                                break
//                            }
//                        }
//                    }
//
//                    medicalConditionsFlow.collect { medicalConditions ->
//
//                         Check if any allergy matches "milk"
//                        for (allergy in medicalConditions) {
//                            if (allergy.equals("diabetic", ignoreCase = true)) {
//                                noSugar = true
//                                break
//                            }
//                        }
//                    }
//                }
//            }
//            congestion.add(job)
//        }
//        congestion = Collections.unmodifiableList(congestion)
//        congestion.joinAll()
        if (location == "starbucks") {
            if (allergy == "milk" && medicalCond == "diabetic") {
                return "Iced Coffee"

            } else if (medicalCond == "diabetic") {
                return "Hot Coffee"
            } else {
                return "Vanilla Latte"
            }

        } else {
            if (allergy == "gluten") {
                return "Mcpuff"

            } else {
                return "McChicken"
            }
        }
    }
}