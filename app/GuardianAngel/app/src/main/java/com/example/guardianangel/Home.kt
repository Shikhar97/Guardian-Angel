package com.example.guardianangel
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject


class Home : Fragment() {

    lateinit var stepsField: TextView
    lateinit var progressIcon: CircularProgressIndicator

    private val SERVER_API_KEY = BuildConfig.HEROKU_API_KEY
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val progress = 0
        val maxProgress = 1000

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
        // Card 2
        // Card 3
        val card3 = view.findViewById<CardView>(R.id.card3)
        card3?.setOnClickListener {
            val intentCard6 = Intent(requireContext(), HealthStats::class.java)
            startActivity(intentCard6)
        }

        // Card 4
        val card4 = view.findViewById<CardView>(R.id.card4)
        card4?.setOnClickListener {
            val intentCard4 = Intent(requireContext(), DietSuggest::class.java)
            startActivity(intentCard4)

        }

        // Card 5

        // Card 6
        val card6 = view.findViewById<CardView>(R.id.card6)
        card6?.setOnClickListener {
            val intentCard6 = Intent(requireContext(), SleepInfo::class.java)
            startActivity(intentCard6)
        }

        // Card 5


        // Card 7
    }

    private fun getRecentUserAttributes(userId: String="655ad12b6ac4d71bf304c5eb"): Int {
        val baseUrl = "https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/users/$userId/user_attributes/recent?count=50"
        val apiKey = SERVER_API_KEY
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

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        showHealthStats()
//    }
//
//    private fun showHealthStats() {
//        val intent = Intent(this.requireContext(), HealthStats::class.java)
//        startActivity(intent)
//    }

}