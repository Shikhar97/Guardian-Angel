package com.example.guardianangel

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

private lateinit var placesClient: PlacesClient
private const val API_KEY = BuildConfig.MAPS_API_KEY

class WalkingSuggestionsActivity : FragmentActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_walking_suggestions)

        Places.initialize(applicationContext, API_KEY)
        placesClient = Places.createClient(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Check for location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true

            mMap.uiSettings.isZoomControlsEnabled = true

            // Request location updates
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Get the last known location
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: android.location.Location? ->
                        location?.let {
                            // Move the camera to the current location
                            val currentLatLng = LatLng(it.latitude, it.longitude)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14.5f))

                            // Fetch nearby gyms
                            fetchNearbyPlaces(it, "park")
                            fetchNearbyPlaces(it, "gym")
                        }
                    }
            }


        } else {
            // Request location permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    private fun fetchNearbyPlaces(currentLatLng: android.location.Location, type: String) {
        val apiKey = API_KEY
        val radius = 15000
        val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${currentLatLng.latitude},${currentLatLng.longitude}&radius=$radius&types=$type&key=$apiKey"

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = fetchData(url)
                withContext(Dispatchers.Main) {
                    parseAndDisplayMarkers(result)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle error
            }
        }
    }

    private suspend fun fetchData(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        return try {
            val inputStream: InputStream = connection.inputStream
            val reader = BufferedReader(InputStreamReader(inputStream))
            val result = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                result.append(line)
            }
            result.toString()
        } finally {
            connection.disconnect()
        }
    }

    private fun parseAndDisplayMarkers(jsonResult: String?) {
        val gson = Gson()
        val nearbyPlacesResponse = gson.fromJson(jsonResult, NearbyPlacesResponse::class.java)

        // Assuming mMap is your GoogleMap instance
        nearbyPlacesResponse.results.forEach { result ->
            val placeLatLng = LatLng(result.geometry.location.lat, result.geometry.location.lng)
            val placeName = result.name

            val markerOptions = MarkerOptions()
                .position(placeLatLng)
                .title(placeName)

            mMap.addMarker(markerOptions)
        }
    }

    data class NearbyPlacesResponse(
        val html_attributions: List<String>,
        val next_page_token: String,
        val results: List<PlaceResult>
    )

    data class PlaceResult(
        val business_status: String,
        val geometry: Geometry,
        val icon: String,
        val icon_background_color: String,
        val icon_mask_base_uri: String,
        val name: String,
        val opening_hours: OpeningHours,
        val photos: List<Photo>,
        val place_id: String,
        val plus_code: PlusCode,
        val rating: Double,
        val reference: String,
        val scope: String,
        val types: List<String>,
        val user_ratings_total: Int,
        val vicinity: String
    )

    data class Geometry(
        val location: Location,
        val viewport: Viewport
    )

    data class Location(
        val lat: Double,
        val lng: Double
    )

    data class Viewport(
        val northeast: Northeast,
        val southwest: Southwest
    )

    data class Northeast(
        val lat: Double,
        val lng: Double
    )

    data class Southwest(
        val lat: Double,
        val lng: Double
    )

    data class OpeningHours(
        val open_now: Boolean
    )

    data class Photo(
        val height: Int,
        val html_attributions: List<String>,
        val photo_reference: String,
        val width: Int
    )

    data class PlusCode(
        val compound_code: String,
        val global_code: String
    )


    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}
