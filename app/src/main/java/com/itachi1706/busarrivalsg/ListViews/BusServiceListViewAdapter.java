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

import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusArrivalArrayObject;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusArrivalArrayObjectEstimate;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusStopJSON;
import com.itachi1706.busarrivalsg.R;
import com.itachi1706.busarrivalsg.StaticVariables;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kenneth on 20/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.ListViews
 */
public class BusServiceListViewAdapter extends ArrayAdapter<BusArrivalArrayObject> {

    private ArrayList<BusArrivalArrayObject> items;
    private Context context;

    public BusServiceListViewAdapter(Context context, int textViewResourceId, ArrayList<BusArrivalArrayObject> objects){
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

        BusArrivalArrayObject i = items.get(position);

        TextView busOperator = (TextView) v.findViewById(R.id.tvBusOperator);
        TextView busNumber = (TextView) v.findViewById(R.id.tvBusService);
        TextView busArrivalNow = (TextView) v.findViewById(R.id.tvBusArrivalNow);
        TextView busArrivalNext = (TextView) v.findViewById(R.id.tvBusArrivalNext);
        TextView operatingStatus = (TextView) v.findViewById(R.id.tvBusStatus);

        if (operatingStatus != null){
            operatingStatus.setText(i.getStatus());

            //Color based on status
            if (i.getStatus().contains("Not") || i.getStatus().contains("not")){
                operatingStatus.setTextColor(Color.RED);
            } else {
                operatingStatus.setTextColor(Color.GREEN);
            }
        }
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
        if (!(i.getStatus().contains("Not") || i.getStatus().contains("not"))) {
            if (busArrivalNow != null) {
                if (i.getNextBus().getEstimatedArrival() == null){
                    busArrivalNow.setText("-");
                    busArrivalNow.setTextColor(Color.GRAY);
                } else {
                    long est = StaticVariables.parseLTAEstimateArrival(i.getNextBus().getEstimatedArrival());
                    String arrivalStatusNow;
                    if (est <= 0)
                        arrivalStatusNow = "Arr";
                    else if (est == 1)
                        arrivalStatusNow = est + " min";
                    else
                        arrivalStatusNow = est + " mins";
                    if (!i.getNextBus().getMonitoredStatus()) arrivalStatusNow += "*";
                    busArrivalNow.setText(arrivalStatusNow);
                    applyColorLoad(busArrivalNow, i.getNextBus());
                }

            }
            if (busArrivalNext != null) {
                if (i.getSubsequentBus().getEstimatedArrival() == null){
                    busArrivalNext.setText("-");
                    busArrivalNext.setTextColor(Color.GRAY);
                } else {
                    long est = StaticVariables.parseLTAEstimateArrival(i.getSubsequentBus().getEstimatedArrival());
                    String arrivalStatusNext;
                    if (est <= 0)
                        arrivalStatusNext = "Arr";
                    else if (est == 1)
                        arrivalStatusNext = est + " min";
                    else
                        arrivalStatusNext = est + " mins";
                    if (!i.getSubsequentBus().getMonitoredStatus()) arrivalStatusNext += "*";
                    busArrivalNext.setText(arrivalStatusNext);
                    applyColorLoad(busArrivalNext, i.getSubsequentBus());
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


        return v;
    }

    private void applyColorLoad(TextView view, BusArrivalArrayObjectEstimate obj){
        switch (obj.getLoad()){
            case "Seats Available": view.setTextColor(Color.GREEN); break;
            case "Standing Available": view.setTextColor(Color.YELLOW); break;
            case "Limited Standing": view.setTextColor(Color.RED); break;
        }
    }

    @Override
    public int getCount(){
        return items != null? items.size() : 0;
    }

    @Override
    public BusArrivalArrayObject getItem(int arg0) {
        return items.get(arg0);
    }

    public void updateAdapter(ArrayList<BusArrivalArrayObject> newArrayData){
        this.items = newArrayData;
    }
}
