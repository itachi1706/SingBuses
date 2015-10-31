package com.itachi1706.busarrivalsg;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.itachi1706.busarrivalsg.AsyncTasks.DlAndInstallCompanionApp;
import com.itachi1706.busarrivalsg.AsyncTasks.GetAllBusStops;
import com.itachi1706.busarrivalsg.AsyncTasks.GetAllBusStopsGeo;
import com.itachi1706.busarrivalsg.AsyncTasks.GetBusServicesFavouritesRecycler;
import com.itachi1706.busarrivalsg.AsyncTasks.Updater.AppUpdateCheck;
import com.itachi1706.busarrivalsg.Database.BusStopsDB;
import com.itachi1706.busarrivalsg.Database.BusStopsGeoDB;
import com.itachi1706.busarrivalsg.Objects.BusServices;
import com.itachi1706.busarrivalsg.RecyclerViews.FavouritesRecyclerAdapter;
import com.itachi1706.busarrivalsg.Services.BusStorage;
import com.itachi1706.busarrivalsg.Services.PebbleCommunications;
import com.itachi1706.busarrivalsg.Util.PebbleEnum;
import com.itachi1706.busarrivalsg.Util.StaticVariables;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.fabric.sdk.android.Fabric;

public class MainMenuActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    //Pebble stuff
    private PebbleKit.PebbleDataReceiver mReceiver;

    //Android Stuff
    private TextView connectionStatus, pressedBtn, firmware, installPrompt;
    private FloatingActionButton fab;
    private RecyclerView favouritesList;
    private FavouritesRecyclerAdapter adapter;
    private SwipeRefreshLayout swipeToRefresh;
    private CardView pebbleCard;

    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main_menu_recycler);

        pebbleCard = (CardView) findViewById(R.id.card_view);
        installPrompt = (TextView) findViewById(R.id.pebbleInstallPrompt);
        connectionStatus = (TextView) findViewById(R.id.pebbleConnectionStatus);
        pressedBtn = (TextView) findViewById(R.id.pressedBtn);
        firmware = (TextView) findViewById(R.id.pebbleFW);
        fab = (FloatingActionButton) findViewById(R.id.add_fab);
        favouritesList = (RecyclerView) findViewById(R.id.rvFav);

        favouritesList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        favouritesList.setLayoutManager(linearLayoutManager);
        favouritesList.setItemAnimator(new DefaultItemAnimator());

        swipeToRefresh = (SwipeRefreshLayout) findViewById(R.id.refresh_favourites);
        swipeToRefresh.setOnRefreshListener(this);

        swipeToRefresh.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3,
                R.color.refresh_progress_4);

        adapter = new FavouritesRecyclerAdapter(new ArrayList<BusServices>(), this);
        favouritesList.setAdapter(adapter);

        sp = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());

        Log.d("MainMenu", "Checking for app updates");
        new AppUpdateCheck(this, sp, true).execute();
        Log.d("MainMenu", "onCreate complete");
    }

    @Override
    public void onResume(){
        super.onResume();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainMenuActivity.this, AddBusStopsRecyclerActivity.class));
            }
        });
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(MainMenuActivity.this, "Add a bus service into favourites", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        boolean checkIfPebbleConnected = PebbleKit.isWatchConnected(this);
        if (checkIfPebbleConnected){
            PebbleKit.FirmwareVersionInfo info = PebbleKit.getWatchFWVersion(this);
            installPrompt.setVisibility(View.VISIBLE);
            //Init
            connectionStatus.setText("Pebble Connected!");
            connectionStatus.setTextColor(Color.GREEN);
            firmware.setText("FW Version: " + info.getTag());
            mReceiver = new PebbleKit.PebbleDataReceiver(StaticVariables.PEBBLE_APP_UUID) {
                @Override
                public void receiveData(Context context, int i, PebbleDictionary pebbleDictionary) {
                    PebbleKit.sendAckToPebble(getApplicationContext(), i);

                    //Handle stuff in the dictionary
                    if (pebbleDictionary.contains(PebbleEnum.KEY_BUTTON_EVENT)){
                        switch (pebbleDictionary.getUnsignedIntegerAsLong(PebbleEnum.KEY_BUTTON_EVENT).intValue()){
                            case PebbleEnum.BUTTON_REFRESH: pressedBtn.setText("Refresh Pressed"); break;
                            case PebbleEnum.BUTTON_NEXT: pressedBtn.setText("Going Next"); break;
                            case PebbleEnum.BUTTON_PREVIOUS: pressedBtn.setText("Going Previous"); break;
                        }
                    }
                }
            };
            PebbleKit.registerReceivedDataHandler(this, mReceiver);

            pebbleCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    installPebbleApp();

                }
            });
        } else {
            connectionStatus.setText("Pebble not connected!");
            connectionStatus.setTextColor(Color.RED);
            firmware.setText("");
            installPrompt.setVisibility(View.INVISIBLE);
            pebbleCard.setOnClickListener(null);
        }

        //Android Stuff now again :D
        checkIfDatabaseUpdated();

        //Update Favourites
        swipeToRefresh.setRefreshing(true);
        updateFavourites();

        //Start Pebble Service if settings are set
        Intent pebbleService = new Intent(this, PebbleCommunications.class);
        if (sp.getBoolean("pebbleSvc", true)){
            startService(pebbleService);
        } else {
            stopService(pebbleService);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
    }

    public void installPebbleApp(){
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (rc == PackageManager.PERMISSION_GRANTED){
            hasPermissionToInstallPebbleApp();
        } else {
            requestStoragePermission();
        }
    }

    public void hasPermissionToInstallPebbleApp(){
        /*Uri url = Uri.parse("pebble://bundle/?addr=itachi1706.com&path=/android/SingBuses.pbw");
        Intent installCompanionApp = new Intent(Intent.ACTION_VIEW);
        installCompanionApp.setDataAndType(url, "application/octet-stream");
        installCompanionApp.setComponent(new ComponentName("com.getpebble.android", "com.getpebble.android.ui.UpdateActivity"));
        startActivity(installCompanionApp);*/
        new DlAndInstallCompanionApp(this).execute("http://itachi1706.com/android/SingBuses.pbw");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, MainSettings.class));
            return true;
        } else if (id == R.id.view_all_stops){
            startActivity(new Intent(this, ListAllBusStopsActivity.class));
            return true;
        } else if (id == R.id.action_refresh){
            swipeToRefresh.setRefreshing(true);
            updateFavourites();
            return true;
        } else if (id == R.id.action_install_companion){
            installPebbleApp();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateFavourites(){
        //Populate favourites from favourites list
        Log.d("FAVOURITES", "Favourites Pref: " + sp.getString("stored", "wot"));

        if (BusStorage.hasFavourites(sp)) {
            //Go ahead with loading and getting data
            Log.d("FAVOURITES", "Has Favourites. Processing");
            StaticVariables.favouritesList = BusStorage.getStoredBuses(sp);
            adapter.updateAdapter(StaticVariables.favouritesList);
            adapter.notifyDataSetChanged();

            Log.d("FAVOURITES", "Finished Processing, retrieving estimated arrival data now");
            for (BusServices s : StaticVariables.favouritesList) {
                new GetBusServicesFavouritesRecycler(this, adapter).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, s);
            }
            Log.d("FAVOURITES", "Finished casting AsyncTasks to retrieve estimated arrival data");
        }

        if (swipeToRefresh.isRefreshing()){
            swipeToRefresh.setRefreshing(false);
        }
    }

    private void checkIfDatabaseUpdated(){
        long busDBLastUpdate = sp.getLong("busDBTimeUpdated", -1);
        long geoDBLastUpdate = sp.getLong("geoDBTimeUpdated", -1);
        long currentTime = System.currentTimeMillis();
        boolean busDBUpdate = false, geoDBUpdate = false;
        if (busDBLastUpdate != -1){
            long lastUpdated = currentTime - busDBLastUpdate;
            long day = TimeUnit.MILLISECONDS.toDays(lastUpdated);
            Log.d("INIT", "Bus DB Last Update: " + day);
            if (day > 30)
                busDBUpdate = true;
            else
                StaticVariables.init1TaskFinished = true;
        }
        if (geoDBLastUpdate != -1){
            long lastUpdated = currentTime - geoDBLastUpdate;
            long day = TimeUnit.MILLISECONDS.toDays(lastUpdated);
            Log.d("INIT", "Geo DB Last Update: " + day);
            if (day > 60)
                geoDBUpdate = true;
        }

        //Main Database
        if (!sp.getBoolean("busDBLoaded", false) || busDBUpdate){
            //First Boot, populate database
            if (!isNetworkAvailable()){
                networkUnavailable("Bus Database");
            } else {
                Log.d("INIT", "Initializing Bus Stop Database");
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle("Bus Database");
                dialog.setMessage("Retriving data from server");
                dialog.setCancelable(false);
                dialog.setIndeterminate(true);
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

                BusStopsDB db = new BusStopsDB(this);
                db.dropAndRebuildDB();
                sp.edit().putBoolean("busDBLoaded", false).apply();
                dialog.show();

                new GetAllBusStops(dialog, db, this, sp).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0);
            }
        } else {
            //Legacy Check
            if (sp.getLong("busDBTimeUpdated", -1) == -1){
                sp.edit().putLong("busDBTimeUpdated", System.currentTimeMillis()).apply();
            }
        }

        if (!sp.getBoolean("geoDBLoaded", false) || geoDBUpdate){
            //First Boot with Geo Location db
            if (!isNetworkAvailable()){
                networkUnavailable("Bus Geo Location Database");
            } else {
                Log.d("INIT", "Initializing Bus Stop Geographical Database");
                ProgressDialog dialogs = new ProgressDialog(this);
                dialogs.setTitle("Bus Geographical Database");
                dialogs.setMessage("Retriving data from server");
                dialogs.setCancelable(false);
                dialogs.setIndeterminate(true);
                dialogs.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

                BusStopsGeoDB geoDB = new BusStopsGeoDB(this);
                geoDB.dropAndRebuildDB();
                sp.edit().putBoolean("geoDBLoaded", false).apply();

                new GetAllBusStopsGeo(dialogs, geoDB, this, sp).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        } else {
            //Legacy Check
            if (sp.getLong("geoDBTimeUpdated", -1) == -1){
                sp.edit().putLong("geoDBTimeUpdated", System.currentTimeMillis()).apply();
            }
        }
    }

    private void networkUnavailable(String reason){
        new AlertDialog.Builder(this).setTitle("No Internet Access")
                .setMessage("Internet Access is required to populate " + reason +", please ensure that you have internet access" +
                        " before relaunching this application").setCancelable(false)
                .setNeutralButton("Override", null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainMenuActivity.this.finish();
                    }
                }).show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onRefresh() {
        updateFavourites();
    }

    private static final int RC_HANDLE_REQUEST_EXTERNAL_STORAGE = 1;

    private void requestStoragePermission() {
        Log.w("Pebble", "Storage permission is not granted. Requesting permission");
        final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_REQUEST_EXTERNAL_STORAGE);
            return;
        }

        final Activity thisActivity = this;

        new AlertDialog.Builder(this).setTitle("Requesting External Storage Permission")
                .setMessage("In order for the app to download the Pebble App File for the Official Pebble App to install, " +
                        "we require permission to write to your storage.")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(thisActivity, permissions, RC_HANDLE_REQUEST_EXTERNAL_STORAGE);
                    }
                }).show();
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_REQUEST_EXTERNAL_STORAGE){
            Log.d("Pebble", "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("Pebble", "Storage permission granted - download pebble file");
            // we have permission, so create the camerasource
            hasPermissionToInstallPebbleApp();
            return;
        }

        Log.e("Pebble", "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        final Activity thisActivity = this;

        new AlertDialog.Builder(this).setTitle("Permission has been denied")
                .setMessage("You have denied the app access to your phone's storage. " +
                        "Due to the fact that installing the companion Pebble app requires you to use the Official " +
                        "Pebble App for installation, the app needs to save the Pebble App File onto your internal storage." +
                        " By denying this permission, install cannot proceed as the Offical Pebble App will not be able to" +
                        " access this app's private storage (which would not have required this permission).\n\n" +
                        "If you wish to grant the app permissions right now, click the settings button" +
                        " and grant it permissions there").setPositiveButton(android.R.string.ok, null)
                .setNeutralButton("APP SETTINGS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent permIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri packageURI = Uri.parse("package:" + thisActivity.getPackageName());
                        permIntent.setData(packageURI);
                        startActivity(permIntent);
                    }
                }).show();
    }
}
