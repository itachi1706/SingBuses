package com.itachi1706.busarrivalsg.RecyclerViews;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.itachi1706.busarrivalsg.BusServicesAtStopRecyclerActivity;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusStopJSON;
import com.itachi1706.busarrivalsg.R;

import java.util.List;
import java.util.Locale;

/**
 * Created by Kenneth on 31/10/2015.
 * for SingBuses in package com.itachi1706.busarrivalsg.RecyclerViews
 */
public class BusStopRecyclerAdapter extends RecyclerView.Adapter<BusStopRecyclerAdapter.BusStopViewHolder> {

    /**
     * This recycler adapter is used in the internal retrive all bus services from bus stop activity
     */

    private List<BusStopJSON> items;
    private Activity activity;

    public BusStopRecyclerAdapter(List<BusStopJSON> objectList, Activity activity){
        this.items = objectList;
        this.activity = activity;
    }

    public void updateAdapter(List<BusStopJSON> newObjects){
        this.items = newObjects;
        notifyDataSetChanged();
    }

    @Override
    public BusStopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View busServiceView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_bus_stops, parent, false);
        return new BusStopViewHolder(busServiceView);
    }

    @Override
    public void onBindViewHolder(BusStopViewHolder holder, int position) {
        BusStopJSON i = items.get(position);

        holder.stopName.setText(i.getBusStopName());
        holder.desc.setText((!i.isHasDistance()) ? i.getRoad() + " (" + i.getCode() + ")" : String.format(Locale.getDefault(), "%s (%s) [%.2fm]",i.getRoad(), i.getCode(), i.getDistance()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public class BusStopViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        protected TextView stopName, desc;

        public BusStopViewHolder(View v){
            super(v);
            stopName = (TextView) v.findViewById(R.id.tvName);
            desc = (TextView) v.findViewById(R.id.tvSubText);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = this.getLayoutPosition();
            final BusStopJSON item = items.get(position);

            Log.d("Size", "" + items.size());
            Intent serviceIntent = new Intent(activity, BusServicesAtStopRecyclerActivity.class);
            serviceIntent.putExtra("stopCode", item.getCode());
            serviceIntent.putExtra("stopName", item.getBusStopName());
            activity.startActivity(serviceIntent);
        }
    }
}
