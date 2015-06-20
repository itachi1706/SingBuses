package com.itachi1706.busarrivalsg.ListViews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusStopJSON;
import com.itachi1706.busarrivalsg.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Kenneth on 20/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.ListViews
 */
public class BusStopListView extends ArrayAdapter<BusStopJSON> {

    private ArrayList<BusStopJSON> items;

    public BusStopListView(Context context, int textViewResourceId, ArrayList<BusStopJSON> objects){
        super(context, textViewResourceId, objects);
        this.items = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View v = convertView;
        if (v == null){
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.listview_bus_stops, parent, false);
        }

        BusStopJSON i = items.get(position);

        TextView stopName = (TextView) v.findViewById(R.id.tvName);
        TextView desc = (TextView) v.findViewById(R.id.tvSubText);

        if (stopName != null){
            stopName.setText(i.getBusStopName());
        }
        if (desc != null){
            desc.setText(i.getRoad() + " (" + i.getCode() + ")");
        }

        return v;
    }

    @Override
    public int getCount(){
        return items != null? items.size() : 0;
    }

    public void updateAdapter(ArrayList<BusStopJSON> newArrayData){
        this.items = newArrayData;
    }
}
