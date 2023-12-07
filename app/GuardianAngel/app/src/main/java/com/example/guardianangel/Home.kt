package com.example.guardianangel
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment


class Home : Fragment() {
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
        val card5 = view.findViewById<CardView>(R.id.card5)
        card5?.setOnClickListener {
            val intentCard5 = Intent(requireContext(), MainView::class.java)
            startActivity(intentCard5)

        }

        // Card 6
        val card6 = view.findViewById<CardView>(R.id.card6)
        card6?.setOnClickListener {
            val intentCard6 = Intent(requireContext(), SleepInfo::class.java)
            startActivity(intentCard6)
        }


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