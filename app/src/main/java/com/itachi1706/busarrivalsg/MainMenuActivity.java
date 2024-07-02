package com.itachi1706.busarrivalsg;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.itachi1706.appupdater.AppUpdateInitializer;
import com.itachi1706.appupdater.object.CAAnalytics;
import com.itachi1706.appupdater.utils.AnalyticsHelper;
import com.itachi1706.busarrivalsg.AsyncTasks.GetAllBusStops;
import com.itachi1706.busarrivalsg.AsyncTasks.GetBusServicesFavouritesRecycler;
import com.itachi1706.busarrivalsg.Database.BusStopsDB;
import com.itachi1706.busarrivalsg.RecyclerViews.FavouritesRecyclerAdapter;
import com.itachi1706.busarrivalsg.Services.BusStorage;
import com.itachi1706.busarrivalsg.objects.BusServices;
import com.itachi1706.busarrivalsg.util.LogInitializer;
import com.itachi1706.busarrivalsg.util.StaticVariables;
import com.itachi1706.busarrivalsg.util.SwipeFavouriteCallback;
import com.itachi1706.busarrivalsg.util.SwipeMoveFavouriteCallback;
import com.itachi1706.helperlib.helpers.ConnectivityHelper;
import com.itachi1706.helperlib.helpers.LogHelper;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainMenuActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    static { AppCompatDelegate.setCompatVectorFromResourcesEnabled(true); }

    //Android Stuff
    private TextView syncState;
    private FloatingActionButton fab;
    private FavouritesRecyclerAdapter adapter;
    private SwipeRefreshLayout swipeToRefresh;

    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG);
        LogInitializer.initLogger();
        setContentView(R.layout.activity_main_menu_recycler);

        fab = findViewById(R.id.add_fab);
        RecyclerView favouritesList = findViewById(R.id.rvFav);
        syncState = findViewById(R.id.firebase_sync_status);

        // Obtain the FirebaseAnalytics instance.
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        AnalyticsHelper helper = new AnalyticsHelper(this, true);
        @SuppressLint("WrongThread") CAAnalytics analytics = helper.getData(BuildConfig.DEBUG);
        setAnalyticsData(analytics != null, mFirebaseAnalytics, analytics); // Update Firebase User Properties
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null);

        if (favouritesList != null) favouritesList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        favouritesList.setLayoutManager(linearLayoutManager);
        favouritesList.setItemAnimator(new DefaultItemAnimator());

        swipeToRefresh = findViewById(R.id.refresh_favourites);
        if (swipeToRefresh != null) {
            swipeToRefresh.setOnRefreshListener(this);
            swipeToRefresh.setColorSchemeResources(
                    R.color.refresh_progress_1,
                    R.color.refresh_progress_2,
                    R.color.refresh_progress_3,
                    R.color.refresh_progress_4);
        }

        sp = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        sp.edit().putBoolean("cepas_dark_theme", true).apply();
        adapter = new FavouritesRecyclerAdapter(new ArrayList<>(), this, StaticVariables.INSTANCE.useServerTime(sp));
        favouritesList.setAdapter(adapter);

        ItemTouchHelper moveAdapter = new ItemTouchHelper(new SwipeMoveFavouriteCallback(this, new SwipeFavouriteCallback.ISwipeCallback() {
            @Override public boolean getFavouriteState(int position) { return true; } // Always favourited
            @Override public boolean moveFavourite(int oldPosition, int newPosition) { return adapter.moveItem(oldPosition, newPosition); }
            @Override public boolean toggleFavourite(int position) { return adapter.removeFavourite(position); }
        }));
        moveAdapter.attachToRecyclerView(favouritesList);

        LogHelper.d("MainMenu", "Checking for app updates");
        new AppUpdateInitializer(this, sp, R.drawable.notification_icon, StaticVariables.BASE_SERVER_URL, true).setOnlyOnWifiCheck(true).checkForUpdate();
        LogHelper.d("MainMenu", "onCreate complete");

        // Create the Firebase Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createFirebaseNotifChannel();
    }

    @Override
    public void onResume(){
        super.onResume();

        fab.setOnClickListener(v -> startActivity(new Intent(MainMenuActivity.this, BusStopsTabbedActivity.class)));
        fab.setOnLongClickListener(v -> {
            Toast.makeText(MainMenuActivity.this, R.string.fab_hint_main_menu, Toast.LENGTH_SHORT).show();
            return true;
        });

        //Android Stuff now again :D
        checkIfDatabaseUpdated();

        //Update Favourites
        swipeToRefresh.setRefreshing(true);
        updateFavourites();

        /*switch (sp.getString("companionDevice", "none")) {
            case "none":
            default:  break;
        }*/

        syncState.setClickable(true);
        syncState.setOnClickListener(v -> {
            // TODO: Start Activity for result
            startActivity(new Intent(getApplicationContext(), FirebaseLoginActivity.class));
        });

        invalidateOptionsMenu();
    }

    private void setAnalyticsData(boolean enabled, FirebaseAnalytics firebaseAnalytics, CAAnalytics analytics) {
        firebaseAnalytics.setUserProperty("debug_mode", (enabled) ? analytics.isDebug() + "" : null);
        firebaseAnalytics.setUserProperty("device_manufacturer", (enabled) ? analytics.getdManufacturer() : null);
        firebaseAnalytics.setUserProperty("device_codename", (enabled) ? analytics.getdCodename() : null);
        firebaseAnalytics.setUserProperty("device_fingerprint", (enabled) ? analytics.getdFingerprint() : null);
        firebaseAnalytics.setUserProperty("device_cpu_abi", (enabled) ? analytics.getdCPU() : null);
        firebaseAnalytics.setUserProperty("device_tags", (enabled) ? analytics.getdTags() : null);
        firebaseAnalytics.setUserProperty("app_version_code", (enabled) ? Long.toString(analytics.getAppVerCode()) : null);
        firebaseAnalytics.setUserProperty("android_sec_patch", (enabled) ? analytics.getSdkPatch() : null);
        firebaseAnalytics.setUserProperty("AndroidOS", (enabled) ? Integer.toString(analytics.getSdkver()) : null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.ntu_tracker).setVisible(sp.getBoolean("showntushuttle", false));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) startActivity(new Intent(this, MainSettings.class));
        else if (id == R.id.view_all_stops) startActivity(new Intent(this, ListAllBusStopsActivity.class));
        else if (id == R.id.action_refresh) {
            swipeToRefresh.setRefreshing(true);
            updateFavourites();
        } else if (id == R.id.ntu_tracker) startActivity(new Intent(this, NTUBusActivity.class));
        else if (id == R.id.scan_cepas) startActivity(new Intent(this, CEPASScanActivity.class));
        else return super.onOptionsItemSelected(item);

        return true;
    }

    private void updateFavourites(){
        final String TAG = "FAVOURITES";
        //Populate favourites from favourites list
        LogHelper.d(TAG, "Favourites Pref: " + sp.getString("stored", "wot"));

        if (BusStorage.hasFavourites(sp)) {
            //Go ahead with loading and getting data
            LogHelper.d(TAG, "Has Favourites. Processing");
            StaticVariables.INSTANCE.setFavouritesList(BusStorage.getStoredBuses(sp));
            adapter.updateAdapter(StaticVariables.INSTANCE.getFavouritesList(), null);
            adapter.notifyDataSetChanged();

            LogHelper.d(TAG, "Finished Processing, retrieving estimated arrival data now");
            new GetBusServicesFavouritesRecycler(this, adapter).executeOnExecutor(StaticVariables.INSTANCE.getFavouritesList().toArray(new BusServices[0]));
            LogHelper.d(TAG, "Finished creating AsyncTasks to retrieve estimated arrival data");
        }

        if (swipeToRefresh.isRefreshing()){
            swipeToRefresh.setRefreshing(false);
        }
    }

    private void checkIfDatabaseUpdated(){
        long busDBLastUpdate = sp.getLong("busDBTimeUpdated", -1);
        boolean busDBUpdate = false;
        if (busDBLastUpdate != -1){
            long day = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - busDBLastUpdate);
            LogHelper.d("INIT", "Bus DB Last Update: " + day);
            if (day > 30)
                busDBUpdate = true;
        }

        // Check upgrade
        final String DBTAG = "DB UPGRADE";
        int dbver = sp.getInt("busDBVerCheck", 0);
        LogHelper.d(DBTAG, "Current DB Version: " + dbver);
        switch (dbver) {
            case 0:
            case 1: LogHelper.i(DBTAG, "Upgrading to V2 API DB"); busDBUpdate = true; sp.edit().putInt("busDBVerCheck", 2).apply(); break;
            case 2: LogHelper.i(DBTAG, "Upgrading to DB with Bus Services"); busDBUpdate = true; sp.edit().putInt("busDBVerCheck", 3).apply(); break;
        }

        //Main Database
        if (!sp.getBoolean("busDBLoaded", false) || busDBUpdate){
            //First Boot, populate database
            if (!ConnectivityHelper.hasInternetConnection(getApplicationContext())) {
                networkUnavailable(getString(R.string.database_name_bus));
            } else {
                LogHelper.d("INIT", "Initializing Bus Stop Database");
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle(getString(R.string.database_name_bus));
                dialog.setMessage(getString(R.string.dialog_message_retrieve_data_from_server));
                dialog.setCancelable(false);
                dialog.setIndeterminate(true);
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

                BusStopsDB db = new BusStopsDB(this);
                db.dropAndRebuildDB();
                sp.edit().putBoolean("busDBLoaded", false).apply();
                dialog.show();

                new GetAllBusStops(dialog, db, this, sp).executeOnExecutor(0);
            }
        } else {
            //Legacy Check
            if (sp.getLong("busDBTimeUpdated", -1) == -1){
                sp.edit().putLong("busDBTimeUpdated", System.currentTimeMillis()).apply();
            }
        }
    }

    private void networkUnavailable(String reason){
        new AlertDialog.Builder(this).setTitle(R.string.dialog_title_no_internet)
                .setMessage(getString(R.string.dialog_message_no_internet, reason)).setCancelable(false)
                .setNeutralButton(R.string.dialog_action_neutral_override, null)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> MainMenuActivity.this.finish()).show();
    }

    @Override
    public void onRefresh() {
        updateFavourites();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createFirebaseNotifChannel() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager == null) return;
        NotificationChannel notificationChannel = new NotificationChannel("firebase-msg", "Server Alerts (FB)", NotificationManager.IMPORTANCE_LOW);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);
        notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        notificationChannel.setGroup("server-msg");

        NotificationChannelGroup notificationChannelGroup = new NotificationChannelGroup("server-msg", "Server Messages");
        notificationManager.createNotificationChannelGroup(notificationChannelGroup);
        notificationManager.createNotificationChannel(notificationChannel);
    }
}
