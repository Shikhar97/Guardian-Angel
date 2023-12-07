package com.example.guardianangel


import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Collections

class Details : Fragment() {

    private val gson = Gson()
    private val apiKey = BuildConfig.HEROKU_API_KEY
    private val tag = "Angel"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        val policy = ThreadPolicy.Builder().permitAll().build()
//        StrictMode.setThreadPolicy(policy)
        val userAttributes = getUserAttributes()

        val view: View = inflater.inflate(R.layout.fragment_details, container, false)
        view.findViewById<TextInputLayout>(R.id.name).editText?.text =
            Editable.Factory.getInstance().newEditable(userAttributes[0].toString())
//        view.findViewById<TextInputLayout>(R.id.age).editText?.text =
//            Editable.Factory.getInstance().newEditable(userAttributes[1].toString())
//        val items = arrayOf("Male", "Female")
//        (view.findViewById<TextInputLayout>(R.id.textField4).editText as? MaterialAutoCompleteTextView)?.setSimpleItems(items)
//        .text = Editable.Factory.getInstance().newEditable("Male")
//        view.findViewById<TextInputLayout>(R.id.weight).editText?.text =
//            Editable.Factory.getInstance().newEditable(userAttributes[3].toString())
//        view.findViewById<TextInputLayout>(R.id.height).editText?.text =
//            Editable.Factory.getInstance().newEditable(userAttributes[4].toString())
//
//        view.findViewById<TextInputLayout>(R.id.bloodg).editText?.text = Editable.Factory.getInstance().newEditable("B-")
//        view.findViewById<TextInputLayout>(R.id.allergy).editText?.text = Editable.Factory.getInstance().newEditable("None")
//        view.findViewById<TextInputLayout>(R.id.medc).editText?.text = Editable.Factory.getInstance().newEditable("Cold, Cough")
//        view.findViewById<TextInputLayout>(R.id.medcs).editText?.text = Editable.Factory.getInstance().newEditable("None")

        val topAppBar = view.findViewById<MaterialToolbar>(R.id.topAppBar)
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.save_icon -> {
//                    val dataToInsert = UsersDb.User(
//                        firstName = arguments?.getString("first_name"),
//                        lastName = arguments?.getString("last_name"),
//                        age = arguments?.getInt("age"),
//                        gender = arguments?.getString("gender"),
//                        weight = arguments?.getFloat("weight"),
//                        height = arguments?.getFloat("height"),
//                        bloodGroup = bloodG.editText?.text.toString(),
//                        allergy = allergy.editText?.text.toString(),
//                        medicalCond = medicalCond.editText?.text.toString(),
//                        medic = medic.editText?.text.toString(),
//                    )
//                    Log.d("Angel", dataToInsert.toString())
//                    userViewModel.insert(dataToInsert)

                    Toast.makeText(
                        activity,
                        "Details saved",
                        Toast.LENGTH_LONG
                    ).show()
                    true
                }

                R.id.help -> {
                    // Handle more item (inside overflow menu) press
                    true
                }

                else -> false
            }
        }
        return view
    }

    private fun getUserAttributes(userId: String = "655ad12b6ac4d71bf304c5eb"): ArrayList<Any> {
        val result: ArrayList<Any> = ArrayList()
        val baseUrl = "https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/users/$userId"
        val client = OkHttpClient()
        var congestion = mutableListOf<Job>()

        runBlocking {
            var request = Request.Builder()
                .url(baseUrl)
                .header("X-Api-Auth", apiKey)
                .method("GET", null)
                .build()

            val job = launch(context = Dispatchers.Default) {
                coroutineScope {
                    Log.d(tag, request.toString())
                    var response = client.newCall(request).execute()

                    if (response.isSuccessful) {
                        val responseBody = response.body
                        val responseText = responseBody?.string()
                        val jsonObject = gson.fromJson(responseText, JsonObject::class.java)
                        Log.i(tag, jsonObject.toString())
                        result.add(jsonObject.get("name").asString)
//                        result.add(jsonObject.get("age").asInt)
//                        result.add(jsonObject.get("gender").asString)
//                        result.add(jsonObject.get("weight").asFloat)
//                        result.add(jsonObject.get("height").asFloat)
                    } else {
                        Log.i(tag, "Request failed with code: ${response.code}")
                    }
                    response.close()

//                    request = Request.Builder()
//                        .url("$baseUrl/user_attributes")
//                        .header("X-Api-Auth", apiKey)
//                        .method("GET", null)
//                        .build()
//                    Log.d(tag, request.toString())
//                    response = client.newCall(request).execute()
//                    if (response.isSuccessful) {
//                        val responseBody = response.body
//                        val responseText = responseBody?.string()
//                        val jsonObject = gson.fromJson(responseText, JsonObject::class.java)
//                        Log.i(tag, jsonObject.toString())
//                        result.add(jsonObject.get("steps_count").asString)
//                    } else {
//                        Log.i(tag, "Request failed with code: ${response.code}")
//                    }
//                    response.close()
                }
            }
            congestion.add(job)
            congestion = Collections.unmodifiableList(congestion)
            congestion.joinAll()
        }
        return result
    }
}
