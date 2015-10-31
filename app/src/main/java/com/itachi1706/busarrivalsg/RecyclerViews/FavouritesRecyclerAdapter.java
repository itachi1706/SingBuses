package com.itachi1706.busarrivalsg.RecyclerViews;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusArrivalArrayObject;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusArrivalArrayObjectEstimate;
import com.itachi1706.busarrivalsg.Objects.BusServices;
import com.itachi1706.busarrivalsg.Objects.BusStatus;
import com.itachi1706.busarrivalsg.R;
import com.itachi1706.busarrivalsg.StaticVariables;

import java.util.List;

/**
 * Created by Kenneth on 31/10/2015.
 * for SingBuses in package com.itachi1706.busarrivalsg.RecyclerViews
 */
public class FavouritesRecyclerAdapter extends RecyclerView.Adapter<FavouritesRecyclerAdapter.FavouritesViewHolder> {

    /**
     * This recycler adapter is used in the internal retrive all bus services from bus stop activity
     */

    private List<BusServices> items;
    private Context context;

    public FavouritesRecyclerAdapter(List<BusServices> objectList, Context context){
        this.items = objectList;
        this.context = context;
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


    public class FavouritesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected TextView busOperator, busNumber, busArrivalNow, busArrivalNext, operatingStatus, stopName;

        public FavouritesViewHolder(View v){
            super(v);
            busOperator = (TextView) v.findViewById(R.id.tvBusOperator);
            busNumber = (TextView) v.findViewById(R.id.tvBusService);
            busArrivalNow = (TextView) v.findViewById(R.id.tvBusArrivalNow);
            busArrivalNext = (TextView) v.findViewById(R.id.tvBusArrivalNext);
            operatingStatus = (TextView) v.findViewById(R.id.tvBusStatus);
            stopName = (TextView) v.findViewById(R.id.tvBusStopName);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
