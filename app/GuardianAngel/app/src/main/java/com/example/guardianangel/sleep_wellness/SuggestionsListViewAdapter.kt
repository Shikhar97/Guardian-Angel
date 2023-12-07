package com.example.guardianangel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class SuggestionsListViewAdapter(private val context: Context, private val dataList: List<Pair<String, String>>) : BaseAdapter() {

    override fun getCount(): Int {
        return dataList.size
    }

    override fun getItem(position: Int): Any {
        return dataList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            // Inflate the layout for each row
            view = LayoutInflater.from(context).inflate(R.layout.suggestions_row, parent, false)

            // Initialize ViewHolder
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            // Reuse the recycled view
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        // Set data to views
        val item = getItem(position) as Pair<String, String>
        viewHolder.leftTextView.text = item.first
        viewHolder.rightTextView.text = item.second

        return view
    }

    private class ViewHolder(view: View) {
        val leftTextView: TextView = view.findViewById(R.id.leftTextView)
        val rightTextView: TextView = view.findViewById(R.id.rightTextView)
    }
}
