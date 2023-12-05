package com.example.guardianangel


import android.os.Bundle
import android.text.Editable
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
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow

class Details : Fragment() {
    private var btn: Button? = null
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
        view.findViewById<TextInputLayout>(R.id.textField1).editText?.text = Editable.Factory.getInstance().newEditable("Shikhar")
//        view.findViewById<TextInputLayout>(R.id.textField2).editText?.text = Editable.Factory.getInstance().newEditable("Gupta")
        view.findViewById<TextInputLayout>(R.id.textField3).editText?.text = Editable.Factory.getInstance().newEditable(27.toString())
//        val items = arrayOf("Male", "Female")
//        (view.findViewById<TextInputLayout>(R.id.textField4).editText as? MaterialAutoCompleteTextView)?.setSimpleItems(items)
//        .text = Editable.Factory.getInstance().newEditable("Male")
        view.findViewById<TextInputLayout>(R.id.textField8).editText?.text = Editable.Factory.getInstance().newEditable(64.toString())
        view.findViewById<TextInputLayout>(R.id.textField9).editText?.text = Editable.Factory.getInstance().newEditable(175.toString())


//        btn = view.findViewById(R.id.next_fab)
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
}
