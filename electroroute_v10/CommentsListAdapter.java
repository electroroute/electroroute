package com.tfg.evelyn.electroroute_v10;
import android.content.ClipData;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Evelyn on 17/05/2016.
 */
public class CommentsListAdapter extends BaseAdapter{

        private Context context;
        private ArrayList<CommentsAsListObject> items;

        public CommentsListAdapter(Context context, ArrayList<CommentsAsListObject> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public int getCount() {
            return this.items.size();
        }

        @Override
        public Object getItem(int position) {
            return this.items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View rowView = convertView;

            if (convertView == null) {
                // Create a new view into the list.
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.comments_list_item, parent, false);
            }

            // Set data into the view.
            TextView username= (TextView) rowView.findViewById(R.id.comment_username);
            TextView date = (TextView) rowView.findViewById(R.id.commnet_date);
            TextView comm = (TextView) rowView.findViewById(R.id.comment_text);
            TextView title = (TextView) rowView.findViewById(R.id.comment_title);
            RatingBar rBar = (RatingBar) rowView.findViewById(R.id.comment_ratingBar_info);

            String fecha= dateFormat(items.get(position).getDate());

            username.setText(items.get(position).getUser());
            date.setText(fecha);
            comm.setText(items.get(position).getComment());
            title.setText(items.get(position).getTitle());
            rBar.setRating(items.get(position).getRating());

            return rowView;
        }


    private String dateFormat(String date){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        try {

            Date data = formatter.parse(date);
            return data.toString();

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

}
