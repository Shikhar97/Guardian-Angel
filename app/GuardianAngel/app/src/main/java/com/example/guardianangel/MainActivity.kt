package com.example.guardianangel

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.navigation.NavigationBarView


class MainActivity : AppCompatActivity() {
    private var bottomNavigationView: NavigationBarView? = null
    private var frameLayout: FrameLayout? = null
    private var tag = "Angel"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        frameLayout = findViewById(R.id.frame_layout)

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView?.setOnItemSelectedListener { item ->
            val v = item.itemId
            if (v == R.id.home) {
                val fragment: Fragment = Home()
                val fm: FragmentManager = supportFragmentManager
                fm.beginTransaction().replace(R.id.frame_layout, fragment).commit()
            } else if (v == R.id.details) {
                val fragment: Fragment = Details()
                val fm: FragmentManager = supportFragmentManager
                fm.beginTransaction().replace(R.id.frame_layout, fragment).commit()
            }
            true
        }
        val fragment: Fragment = Home()
        val fm: FragmentManager = supportFragmentManager
        fm.beginTransaction().replace(R.id.frame_layout, fragment).commit()
    }
}