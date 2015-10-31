package com.itachi1706.busarrivalsg.ListViews;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.itachi1706.busarrivalsg.Objects.BusServices;
import com.itachi1706.busarrivalsg.Objects.BusStatus;
import com.itachi1706.busarrivalsg.R;
import com.itachi1706.busarrivalsg.StaticVariables;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kenneth on 20/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.ListViews
 */
public class FavouritesListViewAdapter extends ArrayAdapter<BusServices> {

    private ArrayList<BusServices> items;
    private Context context;

    public FavouritesListViewAdapter(Context context, int textViewResourceId, ArrayList<BusServices> objects){
        super(context, textViewResourceId, objects);
        this.items = objects;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View v = convertView;
        if (v == null){
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.listview_bus_numbers, parent, false);
        }

        BusServices i = items.get(position);

        TextView busOperator = (TextView) v.findViewById(R.id.tvBusOperator);
        TextView busNumber = (TextView) v.findViewById(R.id.tvBusService);
        TextView busArrivalNow = (TextView) v.findViewById(R.id.tvBusArrivalNow);
        TextView busArrivalNext = (TextView) v.findViewById(R.id.tvBusArrivalNext);
        TextView operatingStatus = (TextView) v.findViewById(R.id.tvBusStatus);
        TextView stopName = (TextView) v.findViewById(R.id.tvBusStopName);

        if (busOperator != null){
            busOperator.setText(i.getOperator());

            //Do coloring based on operator
            if (i.getOperator().equalsIgnoreCase("SMRT")){
                busOperator.setTextColor(Color.RED);
            } else if (i.getOperator().equalsIgnoreCase("SBST")){
                busOperator.setTextColor(Color.MAGENTA);
            }
        }
        if (busNumber != null){
            busNumber.setText(i.getServiceNo());
        }
        if (stopName != null){
            stopName.setVisibility(View.VISIBLE);
            if (i.getStopName() == null){
                //Only show stop code
                stopName.setText(i.getStopID());
            } else {
                //Show stop name AND code
                stopName.setText(i.getStopName() + " (" + i.getStopID() + ")");
            }
        }

        if (i.isObtainedNextData()) {
            if (operatingStatus != null) {
                operatingStatus.setText(i.getOperatingStatus());

                //Color based on status
                if (i.getOperatingStatus().contains("Not") || i.getOperatingStatus().contains("not")) {
                    operatingStatus.setTextColor(Color.RED);
                } else {
                    operatingStatus.setTextColor(Color.GREEN);
                }
            }
            if (!(i.getOperatingStatus().contains("Not") || i.getOperatingStatus().contains("not"))) {
                if (busArrivalNow != null) {
                    if (i.getCurrentBus().getEstimatedArrival() == null){
                        busArrivalNow.setText("-");
                        busArrivalNow.setTextColor(Color.GRAY);
                    } else {
                        long est = StaticVariables.parseLTAEstimateArrival(i.getCurrentBus().getEstimatedArrival());
                        String arrivalStatusNow;
                        if (est <= 0)
                            arrivalStatusNow = "Arr";
                        else if (est == 1)
                            arrivalStatusNow = est + " min";
                        else
                            arrivalStatusNow = est + " mins";
                        if (!i.getCurrentBus().isMonitored()) arrivalStatusNow += "*";
                        busArrivalNow.setText(arrivalStatusNow);
                        applyColorLoad(busArrivalNow, i.getNextBus());
                    }

                }
                if (busArrivalNext != null) {
                    if (i.getNextBus().getEstimatedArrival() == null){
                        busArrivalNext.setText("-");
                        busArrivalNext.setTextColor(Color.GRAY);
                    } else {
                        long est = StaticVariables.parseLTAEstimateArrival(i.getNextBus().getEstimatedArrival());
                        String arrivalStatusNext;
                        if (est <= 0)
                            arrivalStatusNext = "Arr";
                        else if (est == 1)
                            arrivalStatusNext = est + " min";
                        else
                            arrivalStatusNext = est + " mins";
                        if (!i.getNextBus().isMonitored()) arrivalStatusNext += "*";
                        busArrivalNext.setText(arrivalStatusNext);
                        applyColorLoad(busArrivalNext, i.getNextBus());
                    }
                }
                //TODO: Implement 3rd arrival status
            } else {
                if (busArrivalNow != null) {
                    busArrivalNow.setText("-");
                    busArrivalNow.setTextColor(Color.GRAY);
                }
                if (busArrivalNext != null) {
                    busArrivalNext.setText("-");
                    busArrivalNext.setTextColor(Color.GRAY);
                }
            }
        } else {
            if (busArrivalNow != null) {
                busArrivalNow.setText("...");
                busArrivalNow.setTextColor(Color.GRAY);
            }
            if (busArrivalNext != null) {
                busArrivalNext.setText("...");
                busArrivalNext.setTextColor(Color.GRAY);
            }
        }


        return v;
    }

    private void applyColorLoad(TextView view, BusStatus obj){
        switch (obj.getLoad()){
            case 1: view.setTextColor(Color.GREEN); break;
            case 2: view.setTextColor(Color.YELLOW); break;
            case 3: view.setTextColor(Color.RED); break;
            default: view.setTextColor(Color.GRAY); break;
        }
    }

    @Override
    public int getCount(){
        return items != null? items.size() : 0;
    }

    @Override
    public BusServices getItem(int arg0) {
        return items.get(arg0);
    }

    public void updateAdapter(ArrayList<BusServices> newArrayData){
        this.items = newArrayData;
    }
}
