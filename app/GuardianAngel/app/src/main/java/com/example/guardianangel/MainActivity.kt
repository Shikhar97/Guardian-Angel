package com.example.guardianangel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.navigation.NavigationBarView


class MainActivity : AppCompatActivity() {

    private var bottomNavigationView: NavigationBarView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    val fragment: Fragment = Home()
                    val fm: FragmentManager = supportFragmentManager
                    fm.beginTransaction().replace(R.id.frame_layout, fragment).commit()

                }

                R.id.details -> {
                    val fragment: Fragment = Details()
                    val fm: FragmentManager = supportFragmentManager
                    fm.beginTransaction().replace(R.id.frame_layout, fragment).commit()
                }

                R.id.notifications -> {
                    val fragment: Fragment = Notification()
                    val fm: FragmentManager = supportFragmentManager
                    fm.beginTransaction().replace(R.id.frame_layout, fragment).commit()
                }

                R.id.settings -> {
                    val fragment: Fragment = Settings()
                    val fm: FragmentManager = supportFragmentManager
                    fm.beginTransaction().replace(R.id.frame_layout, fragment).commit()
                }
            }
            true
        }
        val fragment: Fragment = Home()
        val fm: FragmentManager = supportFragmentManager
        fm.beginTransaction().replace(R.id.frame_layout, fragment).commit()
        fm.executePendingTransactions()

    }
}