package com.example.guardianangel;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

public class RatingListCustomAdapter extends BaseAdapter {

    ArrayList<String> ratingItemName;
    Context context;
    RatingListCustomAdapter(Context context, ArrayList<String> list){
        ratingItemName = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = view;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.rating_bar_list_item, viewGroup, false);


        TextView itemName = (TextView) v.findViewById(R.id.textView);
        itemName.setText(ratingItemName.get(i).toString());
        RatingBar ratingBar = (RatingBar) v.findViewById(R.id.ratingBar);

        ratingBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*...Here you can get which rating bar is clicked.. where i is the position of the rating bar in list */
            }
        });

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                //Here handle your rating bar clicked operation ... get id of clicked rating bar
                Log.d("rating_bar_clicked","Rating bar id: "+ratingBar.getId()+ " Value:"+String.valueOf(v));
            }
        });


        return v;
    }
}