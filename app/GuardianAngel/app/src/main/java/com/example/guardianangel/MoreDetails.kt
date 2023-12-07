//package com.example.guardianangel
//
//import android.os.Bundle
//import android.text.Editable
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.FragmentActivity
//import androidx.fragment.app.FragmentManager
//import androidx.fragment.app.viewModels
//import com.google.android.material.textfield.TextInputLayout
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.SupervisorJob
//
//class MoreDetails : Fragment() {
//
//    private val applicationScope = CoroutineScope(SupervisorJob())
//    private val database by lazy { UsersDb.AppDatabase.getDatabase(this.requireContext(), applicationScope) }
//    private val repository by lazy { UsersRepository(database.userDao()) }
//    private val userViewModel: UserViewModel by viewModels {
//        UserViewModelFactory(repository)
//    }
//    private var btn: Button? = null
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        val view: View = inflater.inflate(R.layout.fragment_more_details, container, false)
//        view.findViewById<TextInputLayout>(R.id.textField).editText?.text = Editable.Factory.getInstance().newEditable("B-")
//        view.findViewById<TextInputLayout>(R.id.textField1).editText?.text = Editable.Factory.getInstance().newEditable("None")
//        view.findViewById<TextInputLayout>(R.id.textField2).editText?.text = Editable.Factory.getInstance().newEditable("Cold, Cough")
//        view.findViewById<TextInputLayout>(R.id.textField7).editText?.text = Editable.Factory.getInstance().newEditable("None")
//        val backBtn = view.findViewById<Button>(R.id.back_fab)
//        backBtn?.setOnClickListener {
//
//            val fragment: Fragment = Details()
//            val fm: FragmentManager = (activity as FragmentActivity).supportFragmentManager
//            fm.beginTransaction().replace(R.id.frame_layout, fragment).commit()
//        }
//
//        return view
//
//    }
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // Retrieve the variables from the arguments
//
//        btn = view.findViewById(R.id.save_fab)
//        val bloodG = view.findViewById<TextInputLayout>(R.id.textField)
//        val allergy = view.findViewById<TextInputLayout>(R.id.textField1)
//        val medicalCond = view.findViewById<TextInputLayout>(R.id.textField2)
//        val medic = view.findViewById<TextInputLayout>(R.id.textField7)
//        btn?.setOnClickListener {
//            val dataToInsert = UsersDb.User(
//                firstName = arguments?.getString("first_name"),
//                lastName = arguments?.getString("last_name"),
//                age = arguments?.getInt("age"),
//                gender = arguments?.getString("gender"),
//                weight = arguments?.getFloat("weight"),
//                height = arguments?.getFloat("height"),
//                bloodGroup = bloodG.editText?.text.toString(),
//                allergy = allergy.editText?.text.toString(),
//                medicalCond = medicalCond.editText?.text.toString(),
//                medic = medic.editText?.text.toString(),
//            )
//            Log.d("Angel", dataToInsert.toString())
//            userViewModel.insert(dataToInsert)
//
//            Toast.makeText(
//                activity,
//                "Details saved",
//                Toast.LENGTH_LONG
//            ).show()
//            val fragment: Fragment = Details()
//            val fm: FragmentManager = (activity as FragmentActivity).supportFragmentManager
//            fm.beginTransaction().replace(R.id.frame_layout, fragment).commit()
//        }
//    }
//}