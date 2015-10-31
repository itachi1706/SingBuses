package com.itachi1706.busarrivalsg.RecyclerViews;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.itachi1706.busarrivalsg.BusServicesAtStopRecyclerActivity;
import com.itachi1706.busarrivalsg.Objects.BusServices;
import com.itachi1706.busarrivalsg.Objects.BusStatus;
import com.itachi1706.busarrivalsg.R;
import com.itachi1706.busarrivalsg.Services.BusStorage;
import com.itachi1706.busarrivalsg.Util.StaticVariables;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kenneth on 31/10/2015.
 * for SingBuses in package com.itachi1706.busarrivalsg.RecyclerViews
 */
public class FavouritesRecyclerAdapter extends RecyclerView.Adapter<FavouritesRecyclerAdapter.FavouritesViewHolder> {

    /**
     * This recycler adapter is used in the internal retrive all bus services from main activity's favourites list
     */

    private List<BusServices> items;
    private Activity activity;

    public FavouritesRecyclerAdapter(List<BusServices> objectList, Activity activity){
        this.items = objectList;
        this.activity = activity;
    }

    public void updateAdapter(List<BusServices> newObjects){
        this.items = newObjects;
        notifyDataSetChanged();
    }

    @Override
    public FavouritesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View busServiceView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_bus_numbers, parent, false);
        return new FavouritesViewHolder(busServiceView);
    }

    @Override
    public void onBindViewHolder(FavouritesViewHolder holder, int position) {
        BusServices i = items.get(position);

        holder.busOperator.setText(i.getOperator());
        switch (i.getOperator().toUpperCase()) {
            case "SMRT":
                holder.busOperator.setTextColor(Color.RED);
                break;
            case "SBST":
                holder.busOperator.setTextColor(Color.MAGENTA);
                break;
        }

        holder.busNumber.setText(i.getServiceNo());
        holder.stopName.setVisibility(View.VISIBLE);
        holder.stopName.setText((i.getStopName() == null) ? i.getStopID() : i.getStopName() + " (" + i.getStopID() + ")");

        if (!i.isObtainedNextData()) {
            processing(holder.busArrivalNow);
            processing(holder.busArrivalNext);
            return;
        }

        holder.operatingStatus.setText(i.getOperatingStatus());
        if (i.getOperatingStatus().contains("N") || i.getOperatingStatus().contains("not")) {
            holder.operatingStatus.setTextColor(Color.RED);
            notArriving(holder.busArrivalNow);
            notArriving(holder.busArrivalNext);
            return;
        } else {
            holder.operatingStatus.setTextColor(Color.GREEN);
        }

        //TODO: Implement 3rd arrival status
        //Current Bus
        if (i.getCurrentBus().getEstimatedArrival() == null) notArriving(holder.busArrivalNow);
        else {
            long est = StaticVariables.parseLTAEstimateArrival(i.getCurrentBus().getEstimatedArrival());
            String arrivalStatusNow;
            if (est <= 0)
                arrivalStatusNow = "Arr";
            else if (est == 1)
                arrivalStatusNow = est + " min";
            else
                arrivalStatusNow = est + " mins";
            if (!i.getCurrentBus().isMonitored()) arrivalStatusNow += "*";
            holder.busArrivalNow.setText(arrivalStatusNow);
            applyColorLoad(holder.busArrivalNow, i.getCurrentBus());
        }

        //2nd Bus (Next Bus)
        if (i.getNextBus().getEstimatedArrival() == null) notArriving(holder.busArrivalNext);
        else {
            long est = StaticVariables.parseLTAEstimateArrival(i.getNextBus().getEstimatedArrival());
            String arrivalStatusNext;
            if (est <= 0)
                arrivalStatusNext = "Arr";
            else if (est == 1)
                arrivalStatusNext = est + " min";
            else
                arrivalStatusNext = est + " mins";
            if (!i.getNextBus().isMonitored()) arrivalStatusNext += "*";
            holder.busArrivalNext.setText(arrivalStatusNext);
            applyColorLoad(holder.busArrivalNext, i.getNextBus());
        }
    }

    private void processing(TextView view){
        view.setText("...");
        view.setTextColor(Color.GRAY);
    }

    private void notArriving(TextView view){
        view.setText("-");
        view.setTextColor(Color.GRAY);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void applyColorLoad(TextView view, BusStatus obj){
        switch (obj.getLoad()){
            case 1: view.setTextColor(Color.GREEN); break;
            case 2: view.setTextColor(Color.YELLOW); break;
            case 3: view.setTextColor(Color.RED); break;
            default: view.setTextColor(Color.GRAY); break;
        }
    }


    public class FavouritesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        protected TextView busOperator, busNumber, busArrivalNow, busArrivalNext, operatingStatus, stopName;

        public FavouritesViewHolder(View v){
            super(v);
            busOperator = (TextView) v.findViewById(R.id.tvBusOperator);
            busNumber = (TextView) v.findViewById(R.id.tvBusService);
            busArrivalNow = (TextView) v.findViewById(R.id.tvBusArrivalNow);
            busArrivalNext = (TextView) v.findViewById(R.id.tvBusArrivalNext);
            operatingStatus = (TextView) v.findViewById(R.id.tvBusStatus);
            stopName = (TextView) v.findViewById(R.id.tvBusStopName);
            v.setOnLongClickListener(this);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = this.getLayoutPosition();
            final BusServices item = items.get(position);
            Log.d("Size", "" + items.size());
            Intent serviceIntent = new Intent(activity, BusServicesAtStopRecyclerActivity.class);
            serviceIntent.putExtra("stopCode", item.getStopID());
            if (item.getStopName() != null)
                serviceIntent.putExtra("stopName", item.getStopName());
            activity.startActivity(serviceIntent);

        }

        @Override
        public boolean onLongClick(View v) {
            int position = this.getLayoutPosition();
            final BusServices item = items.get(position);

            String message;
            if (item.getStopName() != null)
                message = "Are you sure you want to remove " + item.getServiceNo() + " from " + item.getStopName() + " (" + item.getStopID()
                        + ") from your favourites? This will also remove it from being accessible from your Pebble device";
            else
                message = "Are you sure you want to remove " + item.getServiceNo() + " from Bus Stop Code " + item.getStopID()
                        + " from your favourites? This will also remove it from being accessible from your Pebble device";

            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());

            new AlertDialog.Builder(activity).setTitle("Remove from Favourites")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Remove from favourites
                            for (int i = 0; i < items.size(); i++){
                                BusServices s = items.get(i);
                                if (s.getStopID().equalsIgnoreCase(item.getStopID()) && s.getServiceNo().equalsIgnoreCase(item.getServiceNo())) {
                                    items.remove(i);
                                    break;
                                }
                            }
                            BusStorage.updateBusJSON(sp, (ArrayList<BusServices>) items);
                            notifyDataSetChanged();
                            Toast.makeText(activity.getApplicationContext(), "Removed from favourites", Toast.LENGTH_SHORT).show();
                        }
                    }).setNegativeButton(android.R.string.no, null).show();
            return false;
        }
    }
}
