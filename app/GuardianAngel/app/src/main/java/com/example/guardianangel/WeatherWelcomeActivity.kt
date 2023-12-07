package com.example.guardianangel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.os.AsyncTask

import android.util.Log
import android.widget.*

import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager

import android.widget.TextView
import android.widget.Toast

import androidx.core.app.ActivityCompat
import com.google.android.material.appbar.MaterialToolbar

class WeatherWelcomeActivity : AppCompatActivity() {

    private val apiKey = BuildConfig.WEATHER_API_KEY
    private lateinit var apiUrl: String
    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_welcome)

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

        // Initialize LocationManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Check if location permissions are granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            // Request location updates
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
        } else {
            // Request location permissions if not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }

        val backButton: Button = findViewById(R.id.backButton)

        // Set click listener for the back button
        backButton.setOnClickListener {
            // Handle the back button click
            onBackPressed()
        }

    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            // Stop receiving location updates
            locationManager.removeUpdates(this)

            // Formulate the API URL with the current location
            apiUrl = "https://api.openweathermap.org/data/2.5/weather?lat=${location.latitude}&lon=${location.longitude}&APPID=$apiKey"

            // Execute the weather task
            FetchWeatherTask(apiUrl).execute()
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // If location permissions are granted, request location updates again
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
            } else {
                // Handle the case when location permissions are not granted
                Toast.makeText(this, "Location permissions are required to fetch weather information.", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private inner class FetchWeatherTask(private val apiUrl: String) : AsyncTask<Void, Void, String>() {

        override fun doInBackground(vararg params: Void?): String {
            return try {
                val url = URL(apiUrl)
                val urlConnection = url.openConnection() as HttpURLConnection
                try {
                    val reader = BufferedReader(InputStreamReader(urlConnection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    response.toString()
                } finally {
                    urlConnection.disconnect()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                "Error fetching data"
            }
        }
        private fun setRecommendationText(recommendation: String) {
            val recommendationTextView: TextView = findViewById(R.id.recommendationTextView)
            recommendationTextView.text = recommendation

            // You can customize the text size, color, and style further here if needed.
        }
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            try {
                if (result.isNullOrEmpty()) {
                    Log.e("WeatherApp", "Empty or null response")
                    Toast.makeText(applicationContext, "Error: Empty or null response", Toast.LENGTH_SHORT).show()
                    return
                }

                val jsonObject = JSONObject(result)

                // Extract additional weather details
                val mainObject = jsonObject.getJSONObject("main")
                val visibility = jsonObject.optInt("visibility", -1)
                val windObject = jsonObject.getJSONObject("wind")
                val windSpeed = windObject.optDouble("speed", 0.0)
                val cloudsObject = jsonObject.getJSONObject("clouds")
                val cloudiness = cloudsObject.optInt("all", 0)
                val dt = jsonObject.optLong("dt", -1)
                val sysObject = jsonObject.getJSONObject("sys")
                val sunrise = sysObject.optLong("sunrise", -1)
                val sunset = sysObject.optLong("sunset", -1)

                // Convert time to hours and minutes
                val sunriseTime = convertUnixTimeToHoursAndMinutes(sunrise)
                val sunsetTime = convertUnixTimeToHoursAndMinutes(sunset)

                // Extract weather description
                val weatherArray = jsonObject.getJSONArray("weather")
                val firstWeatherObject = weatherArray.getJSONObject(0)
                val description = firstWeatherObject.getString("description")

                // Convert temperatures to Celsius
                val temperature = kelvinToCelsius(mainObject.optDouble("temp", 0.0))
                val feelsLike = kelvinToCelsius(mainObject.optDouble("feels_like", 0.0))
                val minTemperature = kelvinToCelsius(mainObject.optDouble("temp_min", 0.0))
                val maxTemperature = kelvinToCelsius(mainObject.optDouble("temp_max", 0.0))

                // Convert pressure to atmospheres
                val pressure = hPaToAtmospheres(mainObject.optInt("pressure", 0))

                // Build weather details string
                val weatherDetails = """
            Temperature: ${formatDecimal(temperature)}°C
            Feels Like: ${formatDecimal(feelsLike)}°C
            Min Temperature: ${formatDecimal(minTemperature)}°C
            Max Temperature: ${formatDecimal(maxTemperature)}°C
            Pressure: ${formatDecimal(pressure)} atm
            Humidity: ${mainObject.optInt("humidity", 0)}%
            
            Visibility: $visibility meters
            Wind Speed: ${formatDecimal(windSpeed)} m/s
            Cloudiness: $cloudiness%
            Date/Time: ${convertUnixTimeToHoursAndMinutes(dt)}
            Sunrise: $sunriseTime
            Sunset: $sunsetTime
        """.trimIndent()

                // Guardian Angel's recommendation using data and fuzzy logic
                val recommendation = determineWeatherRecommendation(description, temperature, cloudiness, windSpeed)
                setRecommendationText(recommendation)
                val weatherBackgroundImageView: ImageView = findViewById(R.id.weatherBackgroundImageView)
                val backgroundImageResource = when {
                    recommendation.contains("rain", ignoreCase = true) -> R.drawable.rainy_background
                    recommendation.contains("snow", ignoreCase = true) -> R.drawable.snowy_background
                    recommendation.contains("clear", ignoreCase = true) -> R.drawable.default_background
                    recommendation.contains("cloud", ignoreCase = true) && cloudiness > 50 -> R.drawable.cloudy_background
                    recommendation.contains("thunderstorm", ignoreCase = true) -> R.drawable.thunderstorm_background
                    recommendation.contains("fog", ignoreCase = true) -> R.drawable.foggy_background
                    recommendation.contains("haze", ignoreCase = true) -> R.drawable.hazy_background
                    temperature < 0 -> R.drawable.freezing_background
                    temperature > 30 -> R.drawable.hot_background
                    windSpeed > 5.0 -> R.drawable.windy_background
                    cloudiness > 80 -> R.drawable.overcast_background
                    temperature > 20 && temperature < 30 && cloudiness < 30 && windSpeed < 5.0 -> R.drawable.default_background
                    else -> R.drawable.default_background
                }

                weatherBackgroundImageView.setImageResource(backgroundImageResource)
                // Update TextViews in your layout
                val weatherDetailsTextView: TextView = findViewById(R.id.weatherDetailsTextView)
                val recommendationTextView: TextView = findViewById(R.id.recommendationTextView)

                weatherDetailsTextView.text = "WEATHER DETAILS:\n\n$weatherDetails"
                recommendationTextView.text = "Guardian Angel's recommendation:\n\n${recommendation.toUpperCase(Locale.getDefault())}"

            } catch (e: JSONException) {
                e.printStackTrace()
                Log.e("WeatherApp", "Error parsing JSON: ${e.message}")
                Toast.makeText(applicationContext, "Error parsing data", Toast.LENGTH_SHORT).show()
            }
        }
        private fun convertUnixTimeToHoursAndMinutes(unixTime: Long): String {
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = Date(unixTime * 1000L)
            return dateFormat.format(date)
        }



        private fun hPaToAtmospheres(hPa: Int): Double {
            return hPa / 1013.25
        }



        private fun kelvinToCelsius(kelvin: Double): Double {
            return kelvin - 273.15
        }

        private fun formatDecimal(value: Double): String {
            val decimalFormat = DecimalFormat("#.##")
            return decimalFormat.format(value)
        }

        private fun determineWeatherRecommendation(description: String, temperature: Double, cloudiness: Int, windSpeed: Double): String {
            return when {
                description.contains("rain", ignoreCase = true) -> "CARRY AN UMBRELLA AND STAY DRY."
                description.contains("snow", ignoreCase = true) -> "WARM UP INDOORS. IT'S SNOWING!"
                description.contains("clear", ignoreCase = true) -> "PERFECT WEATHER FOR OUTDOOR ACTIVITIES!"
                description.contains("cloud", ignoreCase = true) && cloudiness > 50 -> "CONSIDER INDOOR PLANS AS IT MIGHT RAIN."
                temperature < 0 -> "IT'S FREEZING! STAY WARM INSIDE."
                temperature > 30 -> "HOT DAY AHEAD! HYDRATE AND STAY COOL."
                windSpeed > 5.0 -> "HOLD ON TO YOUR HAT! WINDY WEATHER OUTSIDE."
                cloudiness > 80 -> "OVERCAST SKY. RELAX INDOORS."
                description.contains("thunderstorm", ignoreCase = true) && temperature > 10 && windSpeed > 7.0 -> "THUNDERSTORM ALERT! STAY SAFE INDOORS."
                description.contains("fog", ignoreCase = true) -> "LOW VISIBILITY DUE TO FOG. DRIVE OR WALK WITH CAUTION."
                description.contains("haze", ignoreCase = true) -> "HAZY CONDITIONS. CONSIDER WEARING A MASK IF NEEDED."
                temperature > 20 && temperature < 20 && cloudiness < 30 && windSpeed < 5.0 -> "WEATHER CONDITIONS ARE FAVORABLE. ENJOY OUTDOOR ACTIVITIES!"
                else -> "ENJOY THE WEATHER!"
            }
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}

