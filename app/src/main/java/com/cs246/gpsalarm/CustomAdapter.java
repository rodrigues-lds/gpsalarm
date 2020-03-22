package com.cs246.gpsalarm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

public class CustomAdapter extends BaseAdapter {

    List<AddressToUse> the_list;
    Context context;

    public CustomAdapter(Context c, List<AddressToUse> my_list) {
        context=c;
        this.the_list=my_list;

    }
    @Override
    public int getCount() {
        return the_list.size();
    }

    @Override
    public Object getItem(int position) {
        return the_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView==null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.row,parent, false);
        }

        AddressToUse tempAddress=(AddressToUse) getItem(position);

        TextView desc=(TextView) convertView.findViewById(R.id.textView1);
        TextView latlon=(TextView) convertView.findViewById(R.id.textView2);

        desc.setText(tempAddress.getDescription());
        latlon.setText(tempAddress.getCoordinates().toString());

        return convertView;
    }
}
