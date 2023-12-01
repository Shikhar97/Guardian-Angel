package com.example.guardianangel

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private var diagnoseButton: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        diagnoseButton = findViewById(R.id.button);
        diagnoseButton!!.setOnClickListener({ openUpdateActivity() })
    }
    fun openUpdateActivity() {
        try {
            val intent = Intent(this, UpdateActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace();
        }
    }
}