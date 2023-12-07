package com.example.guardianangel

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.app.NotificationCompat
import com.google.android.material.appbar.MaterialToolbar

class UpdateActivity : ComponentActivity() {


    private lateinit var responseTextView: TextView
    private lateinit var customImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.update_layout)
        responseTextView = findViewById(R.id.responseTextView)
        customImageView = findViewById(R.id.customImageView)
        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)

        topAppBar.setNavigationOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.help -> {
                    // Handle more item (inside overflow menu) press
                    true
                }

                else -> false
            }
        }

        val health_update = intent.getStringExtra("HEALTH_UPDATE_KEY")

        setPage(health_update)

        val backButton: Button = findViewById(R.id.backButton)


        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setPage(health_update: String?) {
        setLayoutBackground(health_update)
        responseTextView.text = health_update
    }

    private fun setLayoutBackground(fuzzyResponse: String?) {
        customImageView.setVisibility(View.VISIBLE)
        if (fuzzyResponse == null || fuzzyResponse.isEmpty()) {
            customImageView.setImageResource(R.drawable.general_health_2)
            return
        }
        if (fuzzyResponse.contains("sleep", ignoreCase = true)) {
            customImageView.setImageResource(R.drawable.check)
        } else if (fuzzyResponse.contains("critical", ignoreCase = true)) {
            customImageView.setImageResource(R.drawable.ambulance)
        } else if (fuzzyResponse.contains("walked too much", ignoreCase = true)){
            customImageView.setImageResource(R.drawable.more_steps)
        } else if (fuzzyResponse.contains("less steps", ignoreCase = true)){
            customImageView.setImageResource(R.drawable.less_steps)
        } else {
            customImageView.setImageResource(R.drawable.general_health_2)
        }
    }
}