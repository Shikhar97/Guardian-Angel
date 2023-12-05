package com.example.guardianangel
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment


class Settings : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_settings, container, false)

        val users = arrayOf(
            "Steps Monitor"
        )

        var mListView = rootView.findViewById<ListView>(R.id.settings_list)
        var arrayAdapter = ListViewAdapter(requireContext(), users.toList())
        mListView.adapter = arrayAdapter

        mListView.setOnItemClickListener { _, _, position, _ ->
            // Get the selected user
//            val selectedUser = users[position]

            // Create an Intent to start the TargetActivity
            val intent = Intent(requireContext(), StepsMonitor::class.java)

            // Pass any data to the target activity if needed
//            intent.putExtra("selectedUser", selectedUser)

            // Start the activity
            startActivity(intent)
        }

        return rootView
    }
}