package com.example.guardianangel

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale


class MainActivity : AppCompatActivity() {
    private var bottomNavigationView: NavigationBarView? = null
    val utilfuncs = UtilFunction()
    private var TAG = "Angel"


//    private val applicationScope = CoroutineScope(SupervisorJob())
//    private val appDatabase = UsersDb.AppDatabase.getDatabase(this, applicationScope)
//    private val userDao = appDatabase.userDao()

    private var locations = listOf(
        Pair(33.415791, -111.925850), //McD
        Pair(33.409540, -111.916470), //Home
        Pair(33.421670, -111.920200), //Home
        Pair(33.421740, -111.919650), //ASU
        Pair(34.048927, -111.093735), //Starbucks
        Pair(33.429343, -111.908912), //Starbucks
        Pair(33.4218288, -111.9466686), //Oregano
    )
//    private val activityResultLauncher =
//        registerForActivityResult(
//            ActivityResultContracts.RequestMultiplePermissions()
//        )
//        { permissions ->
//            // Handle Permission granted/rejected
//            var permissionGranted = true
//            permissions.entries.forEach {
//                if (it.key in REQUIRED_PERMISSIONS && !it.value)
//                    permissionGranted = false
//            }
//            if (!permissionGranted) {
//                Toast.makeText(
//                    baseContext,
//                    "Permission request denied",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }

    companion object {
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ).toTypedArray()
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

        lifecycleScope.launch {
            while (true) {
                getLocation()
                delay(5000)
            }

//                if (randomLat == 33.415791 && randomLong == -111.925850) {
//                    Log.d(TAG, "You are at McDonalds")
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
//                    Log.d(TAG, "You are at Oregano's Pizza")
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
//                    Log.d(TAG, "You are at Starbucks")
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
        }
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }

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