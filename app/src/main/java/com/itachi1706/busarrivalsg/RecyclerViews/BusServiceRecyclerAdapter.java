package com.itachi1706.busarrivalsg.RecyclerViews;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.itachi1706.busarrivalsg.BusLocationMapsActivity;
import com.itachi1706.busarrivalsg.BusLocationMapsDialogFragment;
import com.itachi1706.busarrivalsg.BusServicesAtStopRecyclerActivity;
import com.itachi1706.busarrivalsg.Database.BusStopsDB;
import com.itachi1706.busarrivalsg.R;
import com.itachi1706.busarrivalsg.gsonObjects.sgLTA.BusArrivalArrayObject;
import com.itachi1706.busarrivalsg.gsonObjects.sgLTA.BusArrivalArrayObjectEstimate;
import com.itachi1706.busarrivalsg.gsonObjects.sgLTA.BusStopJSON;
import com.itachi1706.busarrivalsg.objects.BusServices;
import com.itachi1706.busarrivalsg.util.BusesUtil;
import com.itachi1706.busarrivalsg.util.StaticVariables;
import com.itachi1706.helperlib.helpers.PrefHelper;

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
    private String currentTime;
    private final boolean serverTime;
    private final AppCompatActivity activity;

    public BusServiceRecyclerAdapter(List<BusArrivalArrayObject> objectList, AppCompatActivity activity, boolean useServerTime){
        this.items = objectList;
        this.activity = activity;
        this.serverTime = useServerTime;
    }

    public void updateAdapter(List<BusArrivalArrayObject> newObjects, String currentTime){
        this.items = newObjects;
        this.currentTime = currentTime;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BusServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View busServiceView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_bus_numbers, parent, false);
        return new BusServiceViewHolder(busServiceView);
    }

    @Override
    public void onBindViewHolder(@NonNull BusServiceViewHolder holder, int position) {
        BusArrivalArrayObject i = items.get(position);

        holder.busOperator.setText(i.getOperator());
        holder.busOperator.setTextColor(BusesUtil.INSTANCE.getOperatorColor(activity, i.getOperator()));
        holder.busNumber.setText(i.getServiceNo());

        if (!i.isSvcStatus()) {
            holder.operatingStatus.setText(activity.getString(R.string.service_not_operational));
            holder.operatingStatus.setTextColor(Color.RED);
            notArriving(holder.busArrivalNow, holder.wheelchairNow, holder.busTypeNow);
            notArriving(holder.busArrivalNext, holder.wheelchairNext, holder.busTypeNext);
            notArriving(holder.busArrivalSub, holder.wheelchairSub, holder.busTypeSub);
            return;
        }
        holder.operatingStatus.setText(activity.getString(R.string.service_operational));
        holder.operatingStatus.setTextColor(PrefHelper.isNightModeEnabled(activity) ? Color.GREEN : ContextCompat.getColor(activity, R.color.dark_green));

        assert i.getNextBus() != null;
        assert i.getNextBus2() != null;
        assert i.getNextBus3() != null;

        //Current Bus
        if (i.getNextBus().getEstimatedArrival() == null){
            notArriving(holder.busArrivalNow, holder.wheelchairNow, holder.busTypeNow);
        } else {
            long est = StaticVariables.INSTANCE.parseLTAEstimateArrival(i.getNextBus().getEstimatedArrival(), serverTime, currentTime);
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
            BusesUtil.INSTANCE.applyColorLoad(holder.busArrivalNow, i.getNextBus().getLoadInt());
            holder.wheelchairNow.setVisibility(View.INVISIBLE);
            if (i.getNextBus().isWheelchairAccessible())
                holder.wheelchairNow.setVisibility(View.VISIBLE);
            holder.busTypeNow.setVisibility(View.INVISIBLE);
            if (!arrivalStatusNow.equalsIgnoreCase("-")) {
                holder.busTypeNow.setText(BusesUtil.INSTANCE.getType(i.getNextBus().getTypeInt()));
                holder.busTypeNow.setVisibility(View.VISIBLE);
            }
            holder.busArrivalNow.setOnClickListener(new ArrivalButton(i, i.getStopCode(), i.getServiceNo(), StaticVariables.CUR));
        }

        //2nd bus (Next bus)
        if (i.getNextBus2().getEstimatedArrival() == null){
            notArriving(holder.busArrivalNext, holder.wheelchairNext, holder.busTypeNext);
        } else {
            long est = StaticVariables.INSTANCE.parseLTAEstimateArrival(i.getNextBus2().getEstimatedArrival(), serverTime, currentTime);
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
            BusesUtil.INSTANCE.applyColorLoad(holder.busArrivalNext, i.getNextBus2().getLoadInt());
            holder.wheelchairNext.setVisibility(View.INVISIBLE);
            if (i.getNextBus2().isWheelchairAccessible())
                holder.wheelchairNext.setVisibility(View.VISIBLE);
            holder.busTypeNext.setVisibility(View.INVISIBLE);
            if (!arrivalStatusNext.equalsIgnoreCase("-")) {
                holder.busTypeNext.setText(BusesUtil.INSTANCE.getType(i.getNextBus2().getTypeInt()));
                holder.busTypeNext.setVisibility(View.VISIBLE);
            }
            holder.busArrivalNext.setOnClickListener(new ArrivalButton(i, i.getStopCode(), i.getServiceNo(), StaticVariables.NEXT));
        }

        //3rd bus (Subsequent Bus)
        if (i.getNextBus3() == null) {
            comingSoon(holder.busArrivalSub);
            return;
        }
        if (i.getNextBus3().getEstimatedArrival() == null) notArriving(holder.busArrivalSub, holder.wheelchairSub, holder.busTypeSub);
        else {
            long est = StaticVariables.INSTANCE.parseLTAEstimateArrival(i.getNextBus3().getEstimatedArrival(), serverTime, currentTime);
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
            BusesUtil.INSTANCE.applyColorLoad(holder.busArrivalSub, i.getNextBus3().getLoadInt());
            holder.wheelchairSub.setVisibility(View.INVISIBLE);
            if (i.getNextBus3().isWheelchairAccessible())
                holder.wheelchairSub.setVisibility(View.VISIBLE);
            holder.busTypeSub.setVisibility(View.INVISIBLE);
            if (!arrivalStatusSub.equalsIgnoreCase("-")) {
                holder.busTypeSub.setText(BusesUtil.INSTANCE.getType(i.getNextBus3().getTypeInt()));
                holder.busTypeSub.setVisibility(View.VISIBLE);
            }
            holder.busArrivalSub.setOnClickListener(new ArrivalButton(i, i.getStopCode(), i.getServiceNo(), StaticVariables.SUB));
        }
    }

    private void comingSoon(TextView view){
        view.setText(R.string.feature_coming_soon);
        view.setTextColor(Color.GRAY);
    }

    private void notArriving(TextView view, ImageView wheelchair, TextView busType){
        view.setText("-");
        view.setTextColor(Color.GRAY);
        wheelchair.setVisibility(View.INVISIBLE);
        busType.setVisibility(View.INVISIBLE);
        view.setOnClickListener(new UnavailableButton());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public BusArrivalArrayObject getItem(int position) {
        return items.get(position);
    }


    class BusServiceViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        TextView busOperator, busNumber, operatingStatus;
        TextView busTypeNow, busTypeNext, busTypeSub;
        Button busArrivalNow, busArrivalNext, busArrivalSub;
        ImageView wheelchairNow, wheelchairNext, wheelchairSub;

        BusServiceViewHolder(View v){
            super(v);
            busOperator = v.findViewById(R.id.tvBusOperator);
            busNumber = v.findViewById(R.id.tvBusService);
            busArrivalNow = v.findViewById(R.id.btnBusArrivalNow);
            busArrivalNext = v.findViewById(R.id.btnBusArrivalNext);
            busArrivalSub = v.findViewById(R.id.btnBusArrivalSub);
            operatingStatus = v.findViewById(R.id.tvBusStatus);
            wheelchairNow = v.findViewById(R.id.ivWheelchairNow);
            wheelchairNext = v.findViewById(R.id.ivWheelchairNext);
            wheelchairSub = v.findViewById(R.id.ivWheelchairSub);
            busTypeNow = v.findViewById(R.id.tvBusTypeNow);
            busTypeNext = v.findViewById(R.id.tvBusTypeNext);
            busTypeSub = v.findViewById(R.id.tvBusTypeSub);
            wheelchairNow.setVisibility(View.INVISIBLE);
            wheelchairNext.setVisibility(View.INVISIBLE);
            wheelchairSub.setVisibility(View.INVISIBLE);
            busTypeNow.setVisibility(View.INVISIBLE);
            busTypeNext.setVisibility(View.INVISIBLE);
            busTypeSub.setVisibility(View.INVISIBLE);
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

    private static class UnavailableButton implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(v.getContext()).setTitle(R.string.dialog_title_bus_timing_unavailable)
                    .setMessage(R.string.dialog_message_bus_timing_unavailable)
                    .setPositiveButton(R.string.dialog_action_positive_close, null).show();
        }
    }

    private class ArrivalButton implements View.OnClickListener {

        private final double longitude;
        private final double latitude;
        private final String stopCode;
        private final String serviceNo;
        private final BusArrivalArrayObject busObj;
        private final int state;

        ArrivalButton(BusArrivalArrayObject busObj, String busStopCode, String svcNo, int state) {
            BusArrivalArrayObjectEstimate status = (state == StaticVariables.CUR) ? busObj.getNextBus() :
                    (state == StaticVariables.NEXT) ? busObj.getNextBus2() : busObj.getNextBus3();
            this.busObj = busObj;
            assert status != null;
            this.state = state;
            this.longitude = status.getLongitudeD();
            this.latitude = status.getLatitudeD();
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
            assert busObj != null;
            assert busObj.getNextBus() != null;
            assert busObj.getNextBus2() != null;
            assert busObj.getNextBus3() != null;

            // 3 Bus statuses
            mapsIntent.putExtra("lat1", busObj.getNextBus().getLatitudeD());
            mapsIntent.putExtra("lng1", busObj.getNextBus().getLongitudeD());
            mapsIntent.putExtra("arr1", busObj.getNextBus().getEstimatedArrival());
            mapsIntent.putExtra("type1", busObj.getNextBus().getTypeInt());
            mapsIntent.putExtra("lat2", busObj.getNextBus2().getLatitudeD());
            mapsIntent.putExtra("lng2", busObj.getNextBus2().getLongitudeD());
            mapsIntent.putExtra("arr2", busObj.getNextBus2().getEstimatedArrival());
            mapsIntent.putExtra("type2", busObj.getNextBus2().getTypeInt());
            mapsIntent.putExtra("lat3", busObj.getNextBus3().getLatitudeD());
            mapsIntent.putExtra("lng3", busObj.getNextBus3().getLongitudeD());
            mapsIntent.putExtra("arr3", busObj.getNextBus3().getEstimatedArrival());
            mapsIntent.putExtra("type3", busObj.getNextBus3().getTypeInt());
            mapsIntent.putExtra("sTime", currentTime);
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
