package com.example.guardianangel

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


class DietSuggest : AppCompatActivity() {
    private val TAG = "Angel"
    val fuzzyController = DietFuzzy()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = intent.extras
        setContentView(R.layout.activity_diet_suggest)

        findViewById<TextView>(R.id.address)!!.text = bundle?.getString("address")
        findViewById<TextView>(R.id.country)!!.text = bundle?.getString("country")
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
        val imageViews = ArrayList<ImageView>()
        imageViews.add(findViewById(R.id.card1img))
        imageViews.add(findViewById(R.id.card2img))
        imageViews.add(findViewById(R.id.card3img))

        val titles = ArrayList<TextView>()
        titles.add(findViewById(R.id.card1title1))
        titles.add(findViewById(R.id.card2title1))
        titles.add(findViewById(R.id.card3title1))

        val subtitles = ArrayList<TextView>()
        subtitles.add(findViewById(R.id.card1title2))
        subtitles.add(findViewById(R.id.card2title2))
        subtitles.add(findViewById(R.id.card3title2))

        val bodies = ArrayList<TextView>()
        bodies.add(findViewById(R.id.card1title3))
        bodies.add(findViewById(R.id.card2title3))
        bodies.add(findViewById(R.id.card3title3))


        lifecycleScope.launch(Dispatchers.IO) {

            coroutineScope {
                var i = 0
                val coffeeTypes = fuzzyController.getSuggestions()
                // Iterate through the coffee types
                coffeeTypes.forEach { (condition, pair) ->
                    Log.d(TAG,"$condition Recommendations:")

                    // Access the coffee types
                    val coffeeSet = pair.first
                    coffeeSet.forEach { coffee ->
                        Log.d(TAG,"- Coffee: $coffee")
                        val imgId = getImageResourceId(coffee)

                        // Access the descriptions and benefits
                        val descriptionsAndBenefits = pair.second[coffee]
                        if (descriptionsAndBenefits != null) {
                            val description = descriptionsAndBenefits[0]
                            val benefits = descriptionsAndBenefits.subList(1, descriptionsAndBenefits.size)
                            Log.d(TAG,"  Description: $description")
                            Log.d(TAG,"  Benefits: ${benefits.joinToString(", ")}")
                            updateSuggestionCardTexImg(imageViews[i], imgId)
                            updateSuggestionCardText(titles[i], coffee)
                            updateSuggestionCardText(subtitles[i], description)
                            updateSuggestionCardText(bodies[i], benefits.joinToString(", "))
                            i += 1
                        }
                    }
                }
            }
        }
    }
    private fun updateSuggestionCardTexImg(imgView: ImageView, imgId: Int) {

        lifecycleScope.launch {
            imgView.setImageResource(imgId)
        }
    }
    private fun updateSuggestionCardText(textView: TextView, coffee: String) {

        lifecycleScope.launch {
            textView.text = coffee
        }
    }

    private fun getImageResourceId(coffee: String): Int {
        if(coffee == "Almond Milk Latte"){
            return R.drawable.almond_milk_latte
        }
        if(coffee == "Black Coffee"){
            return R.drawable.black_coffee
        }
        if(coffee == "Blonde Roast"){
            return R.drawable.blonde_roast
        }
        if(coffee == "Vanilla Sweet Cream Cold Brewe"){
            return R.drawable.vanilla_latte
        }
        if(coffee == "Green Tea"){
            return R.drawable.ginger_tea
        }
        if(coffee == "Decaf Coffeee"){
            return R.drawable.black_coffee
        }
        if(coffee == "Coconut Mocha Frappuccino"){
            return R.drawable.coconut_mocha_frappe
        }
        if(coffee == "Herbal Infusion"){
            return R.drawable.herbal_tea
        }
        if(coffee == "Herbal Tea"){
            return R.drawable.herbal_tea
        }
        if(coffee == "Warm Water with Lemon"){
            return R.drawable.ginger_tea
        }
        if(coffee == "Honey Lemon Tea"){
            return R.drawable.honey_tea
        }
        if(coffee == "Warm Water with Honey"){
            return R.drawable.honey_tea
        }
        if(coffee == "Peppermint Tea"){
            return R.drawable.peppermint_tea
        }
        if(coffee == "Hot Ginger Tea"){
            return R.drawable.ginger_tea
        }
        if(coffee == "Chamomile Tea"){
            return R.drawable.chamomile_tea
        }
        return R.drawable.almond_milk_latte
    }
}