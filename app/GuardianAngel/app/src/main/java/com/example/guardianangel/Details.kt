package com.example.guardianangel


import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Collections

class Details : Fragment() {

    private val gson = Gson()
    private val apiKey = BuildConfig.HEROKU_API_KEY
    private val TAG = "Angel"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val autoCompleteTextView = view.findViewById<TextInputLayout>(R.id.gender)

        getUserAttributes()
        val items = arrayOf("Male", "Female", "Other")
        (autoCompleteTextView.editText as? MaterialAutoCompleteTextView)?.setSimpleItems(items)


        val topAppBar = view.findViewById<MaterialToolbar>(R.id.topAppBar)
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.save_icon -> {
                   saveAttributes()
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

    }
    private fun saveAttributes(userId: String = "655ad12b6ac4d71bf304c5eb"){
        val baseUrl = "https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/users/$userId"
        val client = OkHttpClient()

        val jsonBody = JSONObject()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        jsonBody.put("name", view?.findViewById<TextInputLayout>(R.id.name)?.editText?.text)
        jsonBody.put("age", view?.findViewById<TextInputLayout>(R.id.age)?.editText?.text)
        jsonBody.put("gender", view?.findViewById<AutoCompleteTextView>(R.id.genderoption)?.text)
        jsonBody.put("step_goal", view?.findViewById<TextInputLayout>(R.id.mainGoalField)?.editText?.text)
        jsonBody.put("weight", view?.findViewById<TextInputLayout>(R.id.weight)?.editText?.text)
        jsonBody.put("height", view?.findViewById<TextInputLayout>(R.id.height)?.editText?.text)
        jsonBody.put("blood_group", view?.findViewById<TextInputLayout>(R.id.bloodg)?.editText?.text)
        jsonBody.put("allergy", view?.findViewById<TextInputLayout>(R.id.allergy)?.editText?.text)
        jsonBody.put("medical_condition", view?.findViewById<TextInputLayout>(R.id.medc)?.editText?.text)
        jsonBody.put("medications", view?.findViewById<TextInputLayout>(R.id.medcs)?.editText?.text)

        val requestBody = jsonBody.toString().toRequestBody(mediaType)
        lifecycleScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url(baseUrl)
                .header("X-Api-Auth", apiKey)
                .post(requestBody)
                .build()

            coroutineScope {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    Log.i(TAG, "Attributes updated")
                }
                else {
                    Log.i(TAG, "Request failed with code: ${response.code}")
                }
                response.close()

            }
        }
    }

    private fun getUserAttributes(userId: String = "655ad12b6ac4d71bf304c5eb") {
        val baseUrl = "https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/users/$userId"
        val client = OkHttpClient()
        lifecycleScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url(baseUrl)
                .header("X-Api-Auth", apiKey)
                .method("GET", null)
                .build()

            coroutineScope {

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body
                    val responseText = responseBody?.string()
                    val jsonObject = gson.fromJson(responseText, JsonObject::class.java)
                    Log.i(TAG, jsonObject.toString())
                    lifecycleScope.launch{
                        view?.findViewById<TextInputLayout>(R.id.name)?.editText?.text =
                            Editable.Factory.getInstance().newEditable(jsonObject.get("name").asString)
                    }
                    val age = jsonObject.get("age")
                    if (age != null) {
                        lifecycleScope.launch{
                        view?.findViewById<TextInputLayout>(R.id.age)?.editText?.text =
                            Editable.Factory.getInstance().newEditable(age.asString)
                        }
                    }
                    val gender = jsonObject.get("gender")
                        if(gender != null) {
                            lifecycleScope.launch {
                                view?.findViewById<AutoCompleteTextView>(R.id.genderoption)
                                    ?.setText(gender.asString, false)
                            }
                        }
                    val weight = jsonObject.get("weight")
                    if (weight != null) {
                        lifecycleScope.launch {
                            view?.findViewById<TextInputLayout>(R.id.weight)?.editText?.text =
                                Editable.Factory.getInstance().newEditable(weight.asString)
                        }
                    }
                    val height = jsonObject.get("height")
                    if (height != null) {
                        lifecycleScope.launch {
                            view?.findViewById<TextInputLayout>(R.id.height)?.editText?.text =
                                Editable.Factory.getInstance().newEditable(height.asString)
                        }
                    }
                    val bloodGroup = jsonObject.get("blood_group")
                    if (bloodGroup != null) {
                        lifecycleScope.launch {
                            view?.findViewById<TextInputLayout>(R.id.bloodg)?.editText?.text =
                                Editable.Factory.getInstance().newEditable(bloodGroup.asString)
                        }
                    }
                    val allergy = jsonObject.get("allergy")
                    if (allergy != null) {
                        lifecycleScope.launch {
                            view?.findViewById<TextInputLayout>(R.id.allergy)?.editText?.text =
                                Editable.Factory.getInstance().newEditable(allergy.asString)
                        }
                    }
                    val medicalCondition = jsonObject.get("medical_condition")
                    if (medicalCondition != null) {
                        lifecycleScope.launch {
                            view?.findViewById<TextInputLayout>(R.id.medc)?.editText?.text =
                                Editable.Factory.getInstance()
                                    .newEditable(medicalCondition.asString)
                        }
                    }
                    val stepGoal = jsonObject.get("step_goal")
                    if (stepGoal != null) {
                        lifecycleScope.launch {
                            view?.findViewById<TextInputLayout>(R.id.mainGoalField)?.editText?.text =
                                Editable.Factory.getInstance()
                                    .newEditable(stepGoal.asString)
                        }
                    }
                    val medications = jsonObject.get("medications")
                    if (medications != null) {
                        lifecycleScope.launch {
                            view?.findViewById<TextInputLayout>(R.id.medcs)?.editText?.text =
                                Editable.Factory.getInstance()
                                    .newEditable(medications.asString)
                        }
                    }
                } else {
                    Log.i(TAG, "Request failed with code: ${response.code}")
                }
                response.close()
            }
        }
    }
}
