package com.example.guardianangel;

import android.annotation.SuppressLint
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast

import java.util.ArrayList;

class RatingListCustomAdapter(private val context: Context, private val dataList: List<String>) : BaseAdapter() {
    var hashMap: HashMap<String, Float> = HashMap<String, Float>()

//    hashMap["Nausea"] = 0.0f
//    hashMap["Headache"] = 0.0f
//    hashMap["Diarrhoea"] = 0.0f

    override fun getCount(): Int {
        return dataList.size
    }

    override fun getItem(position: Int): Any {
        return dataList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }



    @SuppressLint("ClickableViewAccessibility")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        for (item in dataList) {
            hashMap[item] = 0.0f
        }
        if (convertView == null) {
            // Inflate the layout for each row
            view =
                LayoutInflater.from(context).inflate(R.layout.rating_bar_list_item, parent, false)

            // Initialize ViewHolder
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            // Reuse the recycled view
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        // Set data to views
        val item = getItem(position) as String
        viewHolder.ratingBar.rating = hashMap[item]!!
        viewHolder.textView.text = item
        viewHolder.ratingBar.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (fromUser) {
                hashMap[item] = rating
                Log.i("daysDiff", hashMap.toString())

            }
        }

        viewHolder.ratingBar.setOnClickListener {}
        return view
    }
    fun getHashMap(): Map<String, Float> {
        return hashMap
    }


    private class ViewHolder(view: View) {
        val textView: TextView = view.findViewById(R.id.textView)
        val ratingBar: RatingBar = view.findViewById(R.id.ratingBar)
    }
}
