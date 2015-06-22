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
                    long est = parseEstimateArrival(i.getNextBus().getEstimatedArrival());
                    if (est <= 0)
                        busArrivalNow.setText("Arr");
                    else if (est == 1)
                        busArrivalNow.setText(est + " min");
                    else
                        busArrivalNow.setText(est + " mins");
                    applyColorLoad(busArrivalNow, i.getNextBus());
                }

            }
            if (busArrivalNext != null) {
                if (i.getSubsequentBus().getEstimatedArrival() == null){
                    busArrivalNext.setText("-");
                    busArrivalNext.setTextColor(Color.GRAY);
                } else {
                    long est = parseEstimateArrival(i.getSubsequentBus().getEstimatedArrival());
                    if (est == 0)
                        busArrivalNext.setText("Arr");
                    else if (est == 1)
                        busArrivalNext.setText(est + " min");
                    else
                        busArrivalNext.setText(est + " mins");
                    applyColorLoad(busArrivalNext, i.getSubsequentBus());
                }
            }
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
            case "Limited Seating": view.setTextColor(Color.RED); break;
        }
    }

    private long parseEstimateArrival(String arrivalString){
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        final long[] networkTime = new long[1];
        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                networkTime[0] = location.getTime();
                Log.d("NETWORK", "Current Time: " + location.getTime());
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }
            @Override
            public void onProviderEnabled(String provider) { }
            @Override
            public void onProviderDisabled(String provider) { }
        },Looper.myLooper());

        Log.d("DATE", networkTime[0] + "");
        Log.d("DATE", "Current Time Millis: " + System.currentTimeMillis());
        //GregorianCalendar currentDate = new GregorianCalendar(new SimpleTimeZone(8000, "SST"));
        //currentDate.setTimeInMillis(networkTime[0]);
        //currentDate.setTimeInMillis(System.currentTimeMillis());
        Calendar currentDate = Calendar.getInstance();
        currentDate.add(Calendar.MONTH, 1);

        Calendar arrivalDate = splitDate(arrivalString);

        Log.d("COMPARE","Current: " + currentDate.toString() );
        Log.d("COMPARE", "Arrival: " + arrivalDate.toString() );
        long difference = arrivalDate.getTimeInMillis() - currentDate.getTimeInMillis();
        return TimeUnit.MILLISECONDS.toMinutes(difference);
    }

    private Calendar splitDate(String dateString){
        Log.d("SPLIT", "Date String to parse: " + dateString);
        String[] firstSplit = dateString.split("T");
        String date = firstSplit[0];
        String time = firstSplit[1];
        String[] timeSplit = time.split("\\+");
        String trueTime = timeSplit[0];

        String[] dateSplit = date.split("\\-");
        int year = Integer.parseInt(dateSplit[0]);
        int month = Integer.parseInt(dateSplit[1]);
        int dates = Integer.parseInt(dateSplit[2]);

        String[] trueTimeSplit = trueTime.split(":");
        int hr = Integer.parseInt(trueTimeSplit[0]);
        int min = Integer.parseInt(trueTimeSplit[1]);
        int sec = Integer.parseInt(trueTimeSplit[2]);

        Calendar tmp = new GregorianCalendar(year, month, dates, hr, min, sec);
        //Cause Server gives GMT, we need convert to SST
        tmp.add(Calendar.HOUR, 8);
        //tmp.setTimeZone(new SimpleTimeZone(8000, "SST"));
        return tmp;
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
