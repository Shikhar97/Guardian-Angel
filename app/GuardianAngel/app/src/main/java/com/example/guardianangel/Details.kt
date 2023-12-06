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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request


class Details : Fragment() {

    private val gson = Gson()
    private val API_KEY = BuildConfig.HEROKU_API_KEY


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        val policy = ThreadPolicy.Builder().permitAll().build()
//        StrictMode.setThreadPolicy(policy)

        val view: View = inflater.inflate(R.layout.fragment_details, container, false)
        view.findViewById<TextInputLayout>(R.id.name).editText?.text =
            Editable.Factory.getInstance().newEditable("Shikhar Gupta")
        view.findViewById<TextInputLayout>(R.id.age).editText?.text =
            Editable.Factory.getInstance().newEditable(27.toString())
//        val items = arrayOf("Male", "Female")
//        (view.findViewById<TextInputLayout>(R.id.textField4).editText as? MaterialAutoCompleteTextView)?.setSimpleItems(items)
//        .text = Editable.Factory.getInstance().newEditable("Male")
        view.findViewById<TextInputLayout>(R.id.weight).editText?.text =
            Editable.Factory.getInstance().newEditable(64.toString())
        view.findViewById<TextInputLayout>(R.id.height).editText?.text =
            Editable.Factory.getInstance().newEditable(175.toString())


        val topAppBar = view.findViewById<MaterialToolbar>(R.id.topAppBar)
        topAppBar.setNavigationOnClickListener {
            // Handle navigation icon press
        }

        val bloodG = view.findViewById<TextInputLayout>(R.id.bloodg)
        val allergy = view.findViewById<TextInputLayout>(R.id.allergy)
        val medicalCond = view.findViewById<TextInputLayout>(R.id.medc)
        val medic = view.findViewById<TextInputLayout>(R.id.medcs)

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

//        btn?.setOnClickListener {
//            val bundle = Bundle().apply {
//                putString("first_name", view.findViewById<TextInputLayout>(R.id.textField1).editText?.text.toString()) // Replace with your actual variable
//                putString("last_name", view.findViewById<TextInputLayout>(R.id.textField2).editText?.text.toString()) // Replace with your actual variable
//                putInt("age", view.findViewById<TextInputLayout>(R.id.textField3).editText?.text.toString().toInt()) // Replace with your actual variable
//                putString("gender", view.findViewById<TextInputLayout>(R.id.textField4).editText?.text.toString()) // Replace with your actual variable
//                putFloat("weight", view.findViewById<TextInputLayout>(R.id.textField8).editText?.text.toString().toFloat()) // Replace with your actual variable
//                putFloat("height", view.findViewById<TextInputLayout>(R.id.textField9).editText?.text.toString().toFloat()) // Replace with your actual variable
//            }
//            val fragment: Fragment = MoreDetails().apply {
//                arguments = bundle
//            }
//            val fm: FragmentManager = (activity as FragmentActivity).supportFragmentManager
//            fm.beginTransaction().replace(R.id.frame_layout, fragment).commit()
//        }
        getUserAttributes()
        return view
    }

    private fun getUserAttributes(userId: String = "655ad12b6ac4d71bf304c5eb"): String {
        var username: String = "n/a"
        val baseUrl = "https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/users/$userId"
        val client = OkHttpClient()

//        val url = baseUrl.toHttpUrlOrNull()!!.newBuilder()
//            .addQueryParameter("user_preference", wakeupPreference)
//            .build()

        lifecycleScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url(baseUrl)
                .header("X-Api-Auth", API_KEY)
                .method("GET", null)
                .build()
            Log.d("angel", request.toString())
            val response = client.newCall(request).execute()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val responseBody = response.body
                    val responseText = responseBody?.string()
                    val jsonObject = gson.fromJson(responseText, JsonObject::class.java)
                    username = jsonObject.get("name").asString
                    Log.d("angel", username)
                } else {
                    Log.i(tag, "Request failed with code: ${response.code}")
                }
                response.close()
            }
        }
        return username
    }
}
