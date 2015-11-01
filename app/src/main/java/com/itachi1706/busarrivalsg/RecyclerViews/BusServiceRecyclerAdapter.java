package com.itachi1706.busarrivalsg.RecyclerViews;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.itachi1706.busarrivalsg.BusServicesAtStopRecyclerActivity;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusArrivalArrayObject;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusArrivalArrayObjectEstimate;
import com.itachi1706.busarrivalsg.Objects.BusServices;
import com.itachi1706.busarrivalsg.R;
import com.itachi1706.busarrivalsg.Util.StaticVariables;

import java.util.List;

/**
 * Created by Kenneth on 31/10/2015.
 * for SingBuses in package com.itachi1706.busarrivalsg.RecyclerViews
 */
public class BusServiceRecyclerAdapter extends RecyclerView.Adapter<BusServiceRecyclerAdapter.BusServiceViewHolder> {

    /**
     * This recycler adapter is used in the internal retrieve all bus services from bus stop activity
     */

    private List<BusArrivalArrayObject> items;
    private Activity activity;

    public BusServiceRecyclerAdapter(List<BusArrivalArrayObject> objectList, Activity activity){
        this.items = objectList;
        this.activity = activity;
    }

    public void updateAdapter(List<BusArrivalArrayObject> newObjects){
        this.items = newObjects;
        notifyDataSetChanged();
    }

    @Override
    public BusServiceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View busServiceView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_bus_numbers, parent, false);
        return new BusServiceViewHolder(busServiceView);
    }

    @Override
    public void onBindViewHolder(BusServiceViewHolder holder, int position) {
        BusArrivalArrayObject i = items.get(position);

        holder.operatingStatus.setText(i.getStatus());
        if (i.getStatus().contains("Not") || i.getStatus().contains("not")){
            holder.operatingStatus.setTextColor(Color.RED);
        } else {
            holder.operatingStatus.setTextColor(Color.GREEN);
        }

        holder.busOperator.setText(i.getOperator());
        switch (i.getOperator().toUpperCase()){
            case "SMRT": holder.busOperator.setTextColor(Color.RED); break;
            case "SBST": holder.busOperator.setTextColor(Color.MAGENTA); break;
        }
        holder.busNumber.setText(i.getServiceNo());
        //TODO: Implement 3rd arrival status
        if (i.getStatus().equalsIgnoreCase("not")){
            notArriving(holder.busArrivalNow);
            notArriving(holder.busArrivalNext);
            return;
        }

        //Current Bus
        if (i.getNextBus().getEstimatedArrival() == null){
            notArriving(holder.busArrivalNow);
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
            holder.busArrivalNow.setText(arrivalStatusNow);
            applyColorLoad(holder.busArrivalNow, i.getNextBus());
            holder.wheelchairNow.setVisibility(View.INVISIBLE);
            if (i.getNextBus().isWheelchairAccessible())
                holder.wheelchairNow.setVisibility(View.VISIBLE);
        }

        //2nd bus (Next bus)
        if (i.getSubsequentBus().getEstimatedArrival() == null){
            notArriving(holder.busArrivalNext);
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
            holder.busArrivalNext.setText(arrivalStatusNext);
            applyColorLoad(holder.busArrivalNext, i.getSubsequentBus());
            holder.wheelchairNext.setVisibility(View.INVISIBLE);
            if (i.getSubsequentBus().isWheelchairAccessible())
                holder.wheelchairNext.setVisibility(View.VISIBLE);
        }
    }

    private void notArriving(TextView view){
        view.setText("-");
        view.setTextColor(Color.GRAY);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void applyColorLoad(TextView view, BusArrivalArrayObjectEstimate obj){
        switch (obj.getLoad()){
            case "Seats Available": view.setTextColor(Color.GREEN); break;
            case "Standing Available": view.setTextColor(Color.YELLOW); break;
            case "Limited Standing": view.setTextColor(Color.RED); break;
        }
    }


    public class BusServiceViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        protected TextView busOperator, busNumber, operatingStatus;
        protected Button busArrivalNow, busArrivalNext;
        protected ImageView wheelchairNow, wheelchairNext;

        public BusServiceViewHolder(View v){
            super(v);
            busOperator = (TextView) v.findViewById(R.id.tvBusOperator);
            busNumber = (TextView) v.findViewById(R.id.tvBusService);
            busArrivalNow = (Button) v.findViewById(R.id.tvBusArrivalNow);
            busArrivalNext = (Button) v.findViewById(R.id.tvBusArrivalNext);
            operatingStatus = (TextView) v.findViewById(R.id.tvBusStatus);
            wheelchairNow = (ImageView) v.findViewById(R.id.ivWheelchairNow);
            wheelchairNext = (ImageView) v.findViewById(R.id.ivWheelchairNext);
            wheelchairNow.setVisibility(View.INVISIBLE);
            wheelchairNext.setVisibility(View.INVISIBLE);
            v.setOnLongClickListener(this);
            busArrivalNext.setOnLongClickListener(this);
            busArrivalNow.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            int position = this.getLayoutPosition();
            final BusArrivalArrayObject item = items.get(position);

            if (activity instanceof BusServicesAtStopRecyclerActivity){
                BusServicesAtStopRecyclerActivity newAct = (BusServicesAtStopRecyclerActivity) activity;

                //Check based on thing and verify
                BusServices fav = new BusServices();
                fav.setObtainedNextData(false);
                fav.setOperator(item.getOperator());
                fav.setServiceNo(item.getServiceNo());
                fav.setStopID(item.getStopCode());

                newAct.favouriteOrUnfavourite(fav, item);
                return true;
            }
            return false;
        }
    }
}
