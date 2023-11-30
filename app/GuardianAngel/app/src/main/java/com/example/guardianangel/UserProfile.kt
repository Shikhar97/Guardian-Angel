
package com.example.guardianangel

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.NumberPicker.OnValueChangeListener
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout

var cycleLength = 28
var periodLength = 5
class UserProfile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_user)

        var numFeetPicker = findViewById<NumberPicker>(R.id.num_ft);
        numFeetPicker.maxValue = 9;
        numFeetPicker.minValue = 0;

        var numInchPicker = findViewById<NumberPicker>(R.id.num_inches);
        numInchPicker.maxValue = 12;
        numInchPicker.minValue = 0;


        var numKgPicker = findViewById<NumberPicker>(R.id.num_kg);
        numKgPicker.maxValue = 700;
        numKgPicker.minValue = 1;

        var numGPicker = findViewById<NumberPicker>(R.id.num_g);
        numGPicker.maxValue = 9;
        numGPicker.minValue = 0;

        numFeetPicker.setOnValueChangedListener(OnValueChangeListener { numberPicker, oldFeetVal, newFeetval ->
            Log.d("Feet value", newFeetval.toString() + "")
            val FeetPicker1: Int = numFeetPicker.value
        })

        numInchPicker.setOnValueChangedListener(OnValueChangeListener { numberPicker, oldInchVal, newInchval ->
            Log.d("Inch value", newInchval.toString() + "")
            val InchPicker1: Int = numInchPicker.value

        })
        numKgPicker.setOnValueChangedListener(OnValueChangeListener { numberPicker, oldKgVal, newKgval ->
            Log.d("Kg value", newKgval.toString() + "")
            val KgPicker1: Int = numKgPicker.value

        })
        numGPicker.setOnValueChangedListener(OnValueChangeListener { numberPicker, oldGVal, newGval ->
            Log.d("G value", newGval.toString() + "")
            val GPicker1: Int = newGval

        })

        var lengthcycle = findViewById<EditText>(R.id.cyclelength)
        var lengthperiod = findViewById<EditText>(R.id.periodlength)

        val cycleButtonClick = findViewById<ImageView>(R.id.myCalendar)
        cycleButtonClick.setOnClickListener {
            val intent = Intent(this, MyCycle::class.java)
            cycleLength = lengthcycle.text.toString().toInt()
            periodLength = lengthperiod.text.toString().toInt()
            intent.putExtra("cycleLength", cycleLength)
            intent.putExtra("periodLength", periodLength)
            startActivity(intent)
        }
    }
}