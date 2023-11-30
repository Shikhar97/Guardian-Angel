package com.example.guardianangel


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

class Details : Fragment() {
    private var btn: Button? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_details, container, false)

        btn = view.findViewById(R.id.next_fab)
        btn?.setOnClickListener {

            val fragment: Fragment = MoreDetails()
            val fm: FragmentManager = (activity as FragmentActivity).supportFragmentManager
            fm.beginTransaction().replace(R.id.frame_layout, fragment).commit()
        }
        btn = view.findViewById(R.id.save_fab)
        btn?.setOnClickListener {
            Toast.makeText(
                activity,
                "Details saved",
                Toast.LENGTH_LONG
            ).show()
        }
        return view
    }
}
