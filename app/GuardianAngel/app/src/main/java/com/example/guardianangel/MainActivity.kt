package com.example.guardianangel

import android.content.Context
import androidx.appcompat.app.AlertDialog

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private val apiKey = "abc7341003b82ad4b351851fe4ae7e26"
    private var apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=London,uk&APPID=$apiKey"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val weatherButton: Button = findViewById(R.id.weatherButton)

        weatherButton.setOnClickListener {
            showCityInputDialog()
        }
    }

    private fun showCityInputDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter Place")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val cityName = input.text.toString().trim()
            if (cityName.isNotEmpty()) {
                apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=$cityName&APPID=$apiKey"
                FetchWeatherTask().execute()
            } else {
                Toast.makeText(applicationContext, "Please enter a city name", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private inner class FetchWeatherTask : AsyncTask<Void, Void, String>() {

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

                // Determine weather message
                //val weatherMessage = determineWeatherMessage(description, temperature, cloudiness, windSpeed)

                // Guardian Angel's recommendation using data and fuzzy logic
                val recommendation = determineWeatherRecommendation(description, temperature, cloudiness, windSpeed)

                showPopup("$description\n\n$weatherDetails\n\nGuardian Angel's recommendation using data and fuzzy logic:\n\n${recommendation.toUpperCase(Locale.getDefault())}")

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

        private fun kelvinToCelsius(kelvin: Double): Double {
            return kelvin - 273.15
        }

        private fun hPaToAtmospheres(hPa: Int): Double {
            return hPa / 1013.25
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

    private fun showPopup(weatherDescription: String) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_layout, null)

        val popupWindow = PopupWindow(
            view,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        val closePopupBtn = view.findViewById<Button>(R.id.closePopupBtn)
        val popupDescription = view.findViewById<TextView>(R.id.popupDescription)

        popupDescription.text = "WEATHER: $weatherDescription"

        closePopupBtn.setOnClickListener {
            popupWindow.dismiss()
        }

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
    }
}




