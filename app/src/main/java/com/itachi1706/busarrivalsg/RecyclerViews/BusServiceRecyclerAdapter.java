package com.itachi1706.busarrivalsg.RecyclerViews;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.itachi1706.busarrivalsg.BusLocationMapsActivity;
import com.itachi1706.busarrivalsg.BusLocationMapsDialogFragment;
import com.itachi1706.busarrivalsg.BusServicesAtStopRecyclerActivity;
import com.itachi1706.busarrivalsg.Database.BusStopsDB;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusArrivalArrayObject;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusArrivalArrayObjectEstimate;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusStopJSON;
import com.itachi1706.busarrivalsg.Objects.BusServices;
import com.itachi1706.busarrivalsg.R;
import com.itachi1706.busarrivalsg.Util.StaticVariables;

import java.util.List;

import static com.itachi1706.busarrivalsg.Util.StaticVariables.CUR;
import static com.itachi1706.busarrivalsg.Util.StaticVariables.NEXT;
import static com.itachi1706.busarrivalsg.Util.StaticVariables.SUB;

/**
 * Created by Kenneth on 31/10/2015.
 * for SingBuses in package com.itachi1706.busarrivalsg.RecyclerViews
 */
public class BusServiceRecyclerAdapter extends RecyclerView.Adapter<BusServiceRecyclerAdapter.BusServiceViewHolder> {

    /**
     * This recycler adapter is used in the internal retrieve all bus services from bus stop activity
     */

    private List<BusArrivalArrayObject> items;
    private AppCompatActivity activity;

    public BusServiceRecyclerAdapter(List<BusArrivalArrayObject> objectList, AppCompatActivity activity){
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
            case "TTS": holder.busOperator.setTextColor(Color.GREEN); break;
            case "GAS": holder.busOperator.setTextColor(Color.YELLOW); break;
        }
        holder.busNumber.setText(i.getServiceNo());
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
            if (est == -9999)
                arrivalStatusNow = "-";
            else if (est <= 0)
                arrivalStatusNow = "Arr";
            else if (est == 1)
                arrivalStatusNow = est + "";
            else
                arrivalStatusNow = est + "";
            holder.busArrivalNow.setText(arrivalStatusNow);
            applyColorLoad(holder.busArrivalNow, i.getNextBus());
            holder.wheelchairNow.setVisibility(View.INVISIBLE);
            if (i.getNextBus().isWheelchairAccessible())
                holder.wheelchairNow.setVisibility(View.VISIBLE);
            holder.busArrivalNow.setOnClickListener(new ArrivalButton(i, i.getStopCode(), i.getServiceNo(), CUR));
        }

        //2nd bus (Next bus)
        if (i.getNextBus2().getEstimatedArrival() == null){
            notArriving(holder.busArrivalNext);
        } else {
            long est = StaticVariables.parseLTAEstimateArrival(i.getNextBus2().getEstimatedArrival());
            String arrivalStatusNext;
            if (est == -9999)
                arrivalStatusNext = "-";
            else if (est <= 0)
                arrivalStatusNext = "Arr";
            else if (est == 1)
                arrivalStatusNext = est + "";
            else
                arrivalStatusNext = est + "";
            holder.busArrivalNext.setText(arrivalStatusNext);
            applyColorLoad(holder.busArrivalNext, i.getNextBus2());
            holder.wheelchairNext.setVisibility(View.INVISIBLE);
            if (i.getNextBus2().isWheelchairAccessible())
                holder.wheelchairNext.setVisibility(View.VISIBLE);
            holder.busArrivalNext.setOnClickListener(new ArrivalButton(i, i.getStopCode(), i.getServiceNo(), NEXT));
        }

        //3rd bus (Subsequent Bus)
        if (i.getNextBus3() == null) {
            comingSoon(holder.busArrivalSub);
            return;
        }
        if (i.getNextBus3().getEstimatedArrival() == null) notArriving(holder.busArrivalSub);
        else {
            long est = StaticVariables.parseLTAEstimateArrival(i.getNextBus3().getEstimatedArrival());
            String arrivalStatusSub;
            if (est == -9999)
                arrivalStatusSub = "-";
            else if (est <= 0)
                arrivalStatusSub = "Arr";
            else if (est == 1)
                arrivalStatusSub = est + "";
            else
                arrivalStatusSub = est + "";
            holder.busArrivalSub.setText(arrivalStatusSub);
            applyColorLoad(holder.busArrivalSub, i.getNextBus3());
            holder.wheelchairSub.setVisibility(View.INVISIBLE);
            if (i.getNextBus3().isWheelchairAccessible())
                holder.wheelchairSub.setVisibility(View.VISIBLE);
            holder.busArrivalSub.setOnClickListener(new ArrivalButton(i, i.getStopCode(), i.getServiceNo(), SUB));
        }
    }

    private void comingSoon(TextView view){
        view.setText(R.string.feature_coming_soon);
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

    private void applyColorLoad(TextView view, BusArrivalArrayObjectEstimate obj){
        if (view.getText().toString().equalsIgnoreCase("") || obj.getLoad() == null || view.getText().toString().equalsIgnoreCase("-")) {
            view.setTextColor(Color.GRAY);
            return;
        }
        switch (obj.getLoad()){
            case "Seats Available": view.setTextColor(Color.GREEN); break;
            case "Standing Available": view.setTextColor(Color.YELLOW); break;
            case "Limited Standing": view.setTextColor(Color.RED); break;
        }
    }


    class BusServiceViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        TextView busOperator, busNumber, operatingStatus;
        Button busArrivalNow, busArrivalNext, busArrivalSub;
        ImageView wheelchairNow, wheelchairNext, wheelchairSub;

        BusServiceViewHolder(View v){
            super(v);
            busOperator = (TextView) v.findViewById(R.id.tvBusOperator);
            busNumber = (TextView) v.findViewById(R.id.tvBusService);
            busArrivalNow = (Button) v.findViewById(R.id.btnBusArrivalNow);
            busArrivalNext = (Button) v.findViewById(R.id.btnBusArrivalNext);
            busArrivalSub = (Button) v.findViewById(R.id.btnBusArrivalSub);
            operatingStatus = (TextView) v.findViewById(R.id.tvBusStatus);
            wheelchairNow = (ImageView) v.findViewById(R.id.ivWheelchairNow);
            wheelchairNext = (ImageView) v.findViewById(R.id.ivWheelchairNext);
            wheelchairSub = (ImageView) v.findViewById(R.id.ivWheelchairSub);
            wheelchairNow.setVisibility(View.INVISIBLE);
            wheelchairNext.setVisibility(View.INVISIBLE);
            wheelchairSub.setVisibility(View.INVISIBLE);
            v.setOnLongClickListener(this);
            busArrivalNext.setOnLongClickListener(this);
            busArrivalNow.setOnLongClickListener(this);
            busArrivalSub.setOnLongClickListener(this);
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

    private class ArrivalButton implements View.OnClickListener {

        private double longitude = -1000, latitude = -1000;
        private String stopCode = "", serviceNo = "Unknown";
        private BusArrivalArrayObject busObj;
        private int state;

        ArrivalButton(BusArrivalArrayObject busObj, String busStopCode, String svcNo, int state) {
            BusArrivalArrayObjectEstimate status = (state == CUR) ? busObj.getNextBus() :
                    (state == NEXT) ? busObj.getNextBus2() : busObj.getNextBus3();
            this.busObj = busObj;
            this.state = state;
            this.longitude = status.getLongitude();
            this.latitude = status.getLatitude();
            this.stopCode = busStopCode.trim();
            this.serviceNo = svcNo.trim();
        }

        @Override
        public void onClick(View v) {
            if (longitude == -1000 || latitude == -1000){
                //Error, invalid location
                new AlertDialog.Builder(activity).setTitle(R.string.dialog_title_bus_location_unavailable)
                        .setMessage(R.string.dialog_message_bus_location_unavailable)
                        .setPositiveButton(R.string.dialog_action_positive_close, null).show();
                return;
            }
            if (longitude == -11 && latitude == -11){
                new AlertDialog.Builder(activity).setTitle(R.string.dialog_title_bus_timing_unavailable)
                        .setMessage(R.string.dialog_message_bus_timing_unavailable)
                        .setPositiveButton(R.string.dialog_action_positive_close, null).show();
                return;
            }

            if (latitude == 0 && longitude == 0) {
                new AlertDialog.Builder(activity).setTitle("Bus Service in Depot")
                        .setMessage("The Bus Service is currently still in the depot so no location can be obtained!")
                        .setPositiveButton("Close", null).show();
                return;
            }

            //Check if Google Play Services is enabled
            int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity);
            if (code != ConnectionResult.SUCCESS){
                GoogleApiAvailability.getInstance().getErrorDialog(activity, code, 0);
                return;
            }

            Intent mapsIntent = new Intent(activity, BusLocationMapsActivity.class);
            mapsIntent.putExtra("busCode", stopCode);
            mapsIntent.putExtra("busSvcNo", serviceNo);

            // 3 Bus statuses
            mapsIntent.putExtra("lat1", busObj.getNextBus().getLatitude());
            mapsIntent.putExtra("lng1", busObj.getNextBus().getLongitude());
            mapsIntent.putExtra("arr1", busObj.getNextBus().getEstimatedArrival());
            mapsIntent.putExtra("lat2", busObj.getNextBus2().getLatitude());
            mapsIntent.putExtra("lng2", busObj.getNextBus2().getLongitude());
            mapsIntent.putExtra("arr2", busObj.getNextBus2().getEstimatedArrival());
            mapsIntent.putExtra("lat3", busObj.getNextBus3().getLatitude());
            mapsIntent.putExtra("lng3", busObj.getNextBus3().getLongitude());
            mapsIntent.putExtra("arr3", busObj.getNextBus3().getEstimatedArrival());
            mapsIntent.putExtra("state", state);

            //Get Bus stop longitude and latitude
            BusStopsDB db = new BusStopsDB(activity);
            BusStopJSON busStop = db.getBusStopByBusStopCode(stopCode);
            if (busStop != null) {
                mapsIntent.putExtra("buslat", busStop.getLatitude());
                mapsIntent.putExtra("buslng", busStop.getLongitude());
            }

            if (!PreferenceManager.getDefaultSharedPreferences(v.getContext()).getBoolean("mapPopup", true))
                activity.startActivity(mapsIntent);
            else {
                final BusLocationMapsDialogFragment dialog = new BusLocationMapsDialogFragment();
                dialog.setArguments(mapsIntent.getExtras());
                dialog.show(activity.getSupportFragmentManager(), "123");
            }
        }
    }
}
