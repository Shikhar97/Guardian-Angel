package com.example.guardianangel
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.example.guardianangel.sleep_wellness.SleepWellnessMain


class Settings : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_settings, container, false)

        val users = arrayOf(
            "Steps Monitor",
            "Sleep Wellness",
            "Cycle Tracking"
        )

        var mListView = rootView.findViewById<ListView>(R.id.settings_list)
        var arrayAdapter = ListViewAdapter(requireContext(), users.toList())
        mListView.adapter = arrayAdapter

        mListView.setOnItemClickListener { _, _, position, _ ->
            // Get the selected user
//            val selectedUser = users[position]

            // Pass any data to the target activity if needed
//            intent.putExtra("selectedUser", selectedUser)

            // Start the activity
            when (position) {
                // If "Steps Monitor" is clicked, start StepsMonitor activity
                0 -> {
                    val intent = Intent(requireContext(), StepsMonitor::class.java)
                    startActivity(intent)
                }
                // If "Sleep Wellness" is clicked, start SleepWellnessMain activity
                1 -> {
                    val intent = Intent(requireContext(), SleepWellnessMain::class.java)
                    startActivity(intent)
                }
                // If "Cycle Tracking" is clicked, start SleepWellnessMain activity
                2 -> {
                    val intent = Intent(requireContext(), CycleTrackingProfile::class.java)
                    startActivity(intent)
                }
            }
        }



        return rootView
    }
}