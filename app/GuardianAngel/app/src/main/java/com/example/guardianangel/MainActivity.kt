package com.example.guardianangel

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationBarView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay

import kotlinx.coroutines.launch
import java.lang.Thread.sleep
import kotlin.random.Random


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var bottomNavigationView: NavigationBarView? = null
    private var frameLayout: FrameLayout? = null
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
        frameLayout = findViewById(R.id.frame_layout)
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
            }
            true
        }
        val fragment: Fragment = Home()
        val fm: FragmentManager = supportFragmentManager
        fm.beginTransaction().replace(R.id.frame_layout, fragment).commit()

        fm.addOnBackStackChangedListener(object : FragmentManager.OnBackStackChangedListener {
            override fun onBackStackChanged() {
                // Fragment replacement is complete, now you can find the new fragment
                val mapFragment = fm.findFragmentById(R.id.map) as SupportMapFragment
                if (mapFragment.isAdded) {
                    mapFragment.getMapAsync(this@MainActivity)
                }
                // Remove the listener to prevent unnecessary callbacks
                fm.removeOnBackStackChangedListener(this)
            }
        })

        val random = Random
        while (true) {
            lifecycleScope.launch {
                var latitude: Double
                var longitude: Double

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
                            tag, "Location not found")
                    }

                }
            }
                val (randomLat, randomLong) = locations[random.nextInt(locations.size)]
                Log.d(tag, "Randomly picked pair: $randomLat, $randomLong")
                if(randomLat == 33.415791 && randomLong == -111.925850){
                    Log.d(tag, "You are at McDonalds")
                    break
                }
                else {
                    sleep(5000)
                }
//                Pair(34.048927, -111.093735), //Starbucks
//                Pair(33.429343, -111.908912), //Starbucks
//                Pair(33.4218288, -111.9466686)

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