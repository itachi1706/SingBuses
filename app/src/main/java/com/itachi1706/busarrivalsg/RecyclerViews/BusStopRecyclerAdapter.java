package com.itachi1706.busarrivalsg.RecyclerViews;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.itachi1706.busarrivalsg.BusServicesAtStopRecyclerActivity;
import com.itachi1706.busarrivalsg.gsonObjects.sgLTA.BusStopJSON;
import com.itachi1706.busarrivalsg.R;

import java.util.LinkedList;
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

    public BusStopRecyclerAdapter(List<BusStopJSON> objectList){
        this.items = objectList;
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

        holder.stopName.setText(i.getDescription());
        holder.desc.setText((!i.isHasDistance()) ? i.getRoadName() + " (" + i.getBusStopCode() + ")" : String.format(Locale.getDefault(), "%s (%s) [%.2fm]",i.getRoadName(), i.getBusStopCode(), i.getDistance()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void handleClick(Context context, BusStopJSON stop) {
        Log.d("Size", "" + items.size());
        Intent serviceIntent = new Intent(context, BusServicesAtStopRecyclerActivity.class);
        serviceIntent.putExtra("stopCode", stop.getBusStopCode());
        serviceIntent.putExtra("stopName", stop.getDescription());
        serviceIntent.putExtra("busServices", stop.getServices());
        context.startActivity(serviceIntent);

        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Code: " + stop.getBusStopCode());
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "openBusStopDetail");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        // Add dynamic shortcuts
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
            LinkedList<ShortcutInfo> infos = new LinkedList<>(shortcutManager.getDynamicShortcuts());
            final int shortcutCount = shortcutManager.getMaxShortcutCountPerActivity() - 2;
            if (infos.size() >= shortcutCount) {
                Log.i("ShortcutManager", "Dynamic Shortcuts more than " + shortcutCount
                        + ". Removing extras");
                do {
                    infos.removeLast();
                } while (infos.size() > shortcutCount);
            }
            serviceIntent.setAction(Intent.ACTION_VIEW);
            ShortcutInfo newShortcut = new ShortcutInfo.Builder(context, "bus-" + stop.getBusStopCode())
                    .setShortLabel(stop.getDescription()).setLongLabel("Launch this bus stop directly")
                    .setIcon(Icon.createWithResource(context, R.mipmap.ic_launcher_round))
                    .setIntent(serviceIntent).build();

            infos.add(newShortcut);
            shortcutManager.setDynamicShortcuts(infos);
        }
    }


    class BusStopViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        TextView stopName, desc;

        BusStopViewHolder(View v){
            super(v);
            stopName = v.findViewById(R.id.tvName);
            desc = v.findViewById(R.id.tvSubText);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = this.getLayoutPosition();
            final BusStopJSON item = items.get(position);
            handleClick(v.getContext(), item);
        }
    }
}
