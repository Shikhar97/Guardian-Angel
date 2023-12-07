package com.example.guardianangel

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.Request

class DietFuzzy {
    private val gson = Gson()
    private val apiKey = BuildConfig.HEROKU_API_KEY
    private val TAG = "Angel"

    fun getSuggestions(userId: String = "655ad12b6ac4d71bf304c5eb"): Map<String, Pair<Set<String>, Map<String, List<String>>>> {
        // Example input parameters
        var hasCold = false
        var hasCough = false
        var hasFever = false
        var hasDiabetes = false
        var hasGlutenAllergy = false
        var hasLactoseIntolerance = false


        val baseUrl = "https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/users/$userId"
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(baseUrl)
            .header("X-Api-Auth", apiKey)
            .method("GET", null)
            .build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val responseBody = response.body
            val responseText = responseBody?.string()
            val jsonObject = gson.fromJson(responseText, JsonObject::class.java)
            Log.i(TAG, jsonObject.toString())
            val allergy = jsonObject.get("allergy")
            if (allergy != null) {
                if (allergy.asString.contains("gluten", ignoreCase = true)) {
                    hasGlutenAllergy = true
                }
                if (allergy.asString.contains("lactose", ignoreCase = true)) {
                    hasLactoseIntolerance = true
                }
            }
            val medicalCondition = jsonObject.get("medical_condition")
            if (medicalCondition != null) {
                if (medicalCondition.asString.contains("fever", ignoreCase = true)) {
                    hasFever = true
                }
                if (medicalCondition.asString.contains("cold", ignoreCase = true)) {
                    hasCold = true
                }
                if (medicalCondition.asString.contains("cough", ignoreCase = true)) {
                    hasCough = true
                }
                if (medicalCondition.asString.contains("diabetes", ignoreCase = true)) {
                    hasDiabetes = true
                }
            }

        } else {
            Log.i(TAG, "Request failed with code: ${response.code}")
        }
        response.close()

        // Fuzzy logic to decide types of coffee for each condition
        return decideCoffeeForConditions(
            hasCold, hasCough, hasFever, hasDiabetes, hasGlutenAllergy, hasLactoseIntolerance
        )
    }


    fun decideCoffeeForConditions(
        hasCold: Boolean,
        hasCough: Boolean,
        hasFever: Boolean,
        hasDiabetes: Boolean,
        hasGlutenAllergy: Boolean,
        hasLactoseIntolerance: Boolean
    ): Map<String, Pair<Set<String>, Map<String, List<String>>>> {
        val coffeeTypes = mutableMapOf<String, Pair<Set<String>, Map<String, List<String>>>>()

        // Fuzzy logic rules for each condition
        if (hasCold) {
            val coffees = setOf("Hot Ginger Tea", "Chamomile Tea", "Peppermint Tea")
            val descriptions = mapOf(
                "Hot Ginger Tea" to listOf("Warming herbal tea", "Soothing for a sore throat"),
                "Chamomile Tea" to listOf("Calming chamomile blend", "Promotes relaxation"),
                "Peppermint Tea" to listOf(
                    "Refreshing peppermint flavor",
                    "Relieves nasal congestion"
                )
            )
            coffeeTypes["Cold"] = Pair(coffees, descriptions)
        } else if (hasCough) {
            val coffees = setOf("Honey Lemon Tea", "Herbal Tea", "Warm Water with Honey")
            val descriptions = mapOf(
                "Honey Lemon Tea" to listOf("Soothing honey and lemon", "Eases cough symptoms"),
                "Herbal Tea" to listOf("Natural herbal infusion", "Supports respiratory health"),
                "Warm Water with Honey" to listOf("Warm water remedy", "Calms irritated throat")
            )
            coffeeTypes["Cough"] = Pair(coffees, descriptions)
        } else if (hasFever) {
            val coffees = setOf("Black Coffee", "Herbal Infusion", "Warm Water with Lemon")
            val descriptions = mapOf(
                "Black Coffee" to listOf("Bold and rich flavor", "Boosts alertness"),
                "Herbal Infusion" to listOf("Gentle herbal blend", "Hydrating and comforting"),
                "Warm Water with Lemon" to listOf(
                    "Hydrating with a hint of lemon",
                    "Soothes throat"
                )
            )
            coffeeTypes["Fever"] = Pair(coffees, descriptions)
        } else if (hasDiabetes) {
            val coffees = setOf("Black Coffee", "Green Tea", "Decaf Coffee")
            val descriptions = mapOf(
                "Black Coffee" to listOf("Classic black coffee", "Low in calories"),
                "Green Tea" to listOf(
                    "Antioxidant-rich green tea",
                    "May help regulate blood sugar"
                ),
                "Decaf Coffee" to listOf(
                    "Caffeine-free option",
                    "Enjoy the coffee flavor without caffeine"
                )
            )
            coffeeTypes["Diabetes"] = Pair(coffees, descriptions)
        } else if (hasGlutenAllergy) {
            val coffees = setOf(
                "Vanilla Sweet Cream Cold Brew",
                "Blonde Roast",
                "Coconut Mocha Frappuccino"
            )
            val descriptions = mapOf(
                "Vanilla Sweet Cream Cold Brew" to listOf(
                    "Smooth cold brew with sweet cream",
                    "Gluten-free"
                ),
                "Blonde Roast" to listOf(
                    "Light and mellow coffee",
                    "No gluten-containing ingredients"
                ),
                "Coconut Mocha Frappuccino" to listOf(
                    "Iced coconut mocha blend",
                    "Dairy-free and gluten-free"
                )
            )
            coffeeTypes["Gluten Allergy"] = Pair(coffees, descriptions)
        } else if (hasLactoseIntolerance) {
            val coffees = setOf(
                "Almond Milk Latte",
                "Coconut Milk Coffee",
                "Black Coffee"
            )
            val descriptions = mapOf(
                "Almond Milk Latte" to listOf(
                    "Creamy latte with almond milk",
                    "Dairy-free and lactose-free"
                ),
                "Coconut Milk Coffee" to listOf(
                    "Coffee with coconut milk",
                    "Suitable for lactose intolerant individuals"
                ),
                "Black Coffee" to listOf("Simple black coffee", "No added dairy or lactose")
            )
            coffeeTypes["Lactose Intolerance"] = Pair(coffees, descriptions)
        }

        return coffeeTypes
    }
}
