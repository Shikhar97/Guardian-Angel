package com.example.guardianangel


import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request

class Details : Fragment() {
    private val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { UsersDb.AppDatabase.getDatabase(this.requireContext(), applicationScope) }
    private val repository by lazy { UsersRepository(database.userDao()) }
    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(repository)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_details, container, false)
        view.findViewById<TextInputLayout>(R.id.name).editText?.text = Editable.Factory.getInstance().newEditable("Shikhar Gupta")
        view.findViewById<TextInputLayout>(R.id.age).editText?.text = Editable.Factory.getInstance().newEditable(27.toString())
//        val items = arrayOf("Male", "Female")
//        (view.findViewById<TextInputLayout>(R.id.textField4).editText as? MaterialAutoCompleteTextView)?.setSimpleItems(items)
//        .text = Editable.Factory.getInstance().newEditable("Male")
        view.findViewById<TextInputLayout>(R.id.weight).editText?.text = Editable.Factory.getInstance().newEditable(64.toString())
        view.findViewById<TextInputLayout>(R.id.height).editText?.text = Editable.Factory.getInstance().newEditable(175.toString())


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
        return view
    }

    private fun getUserAttributes(userId: String="655ff2802c6a0e4de1d9a9d4"){
        val baseUrl = "https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/users/$userId/user_attributes"
        val apiKey = "<api_key>"
        // <For testing> Without API key the below code doesn't work. Reach out to aelango3@asu.edu for api key
        val client = OkHttpClient()


        val url = baseUrl.toHttpUrlOrNull()!!.newBuilder()
            .addQueryParameter("user_preference", wakeupPreference)
            .build()

        val request = Request.Builder()
            .url(url)
            .header("X-Api-Auth", apiKey)
            .method("GET", null)
            .build()

        println(request)

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful)
                throw IOException("Unexpected code $response")
            wakeUpTime = 420L

            for (header in response.headers) {
                println("${header.first}: ${header.second}")
            }

            val responseBody = response.body?.string()
            println(responseBody)
            val jsonResponse = parseJsonResponse(responseBody)
            wakeUpTime = jsonResponse?.optLong("wake_up_time")!!
        }
        // <For testing> Comment the above code and uncomment the below code for immediate testing
        return wakeUpTime
    }
}
