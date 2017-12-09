package com.itachi1706.busarrivalsg;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.itachi1706.appupdater.AppUpdateInitializer;
import com.itachi1706.appupdater.Util.ConnectivityHelper;
import com.itachi1706.busarrivalsg.AsyncTasks.DlAndInstallCompanionApp;
import com.itachi1706.busarrivalsg.AsyncTasks.GetAllBusStops;
import com.itachi1706.busarrivalsg.AsyncTasks.GetBusServicesFavouritesRecycler;
import com.itachi1706.busarrivalsg.Database.BusStopsDB;
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

    //Android Stuff
    private TextView connectionStatus, pressedBtn, firmware, installPrompt, syncState;
    private FloatingActionButton fab;
    private RecyclerView favouritesList;
    private FavouritesRecyclerAdapter adapter;
    private SwipeRefreshLayout swipeToRefresh;
    private CardView pebbleCard;

    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics crashlyticsKit = new Crashlytics.Builder().core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build();
        Fabric.with(this, crashlyticsKit);
        setContentView(R.layout.activity_main_menu_recycler);

        pebbleCard = findViewById(R.id.card_view);
        installPrompt = findViewById(R.id.pebbleInstallPrompt);
        connectionStatus = findViewById(R.id.pebbleConnectionStatus);
        pressedBtn = findViewById(R.id.pressedBtn);
        firmware = findViewById(R.id.pebbleFW);
        fab = findViewById(R.id.add_fab);
        favouritesList = findViewById(R.id.rvFav);
        syncState = findViewById(R.id.firebase_sync_status);

        // Obtain the FirebaseAnalytics instance.
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null);

        if (favouritesList != null) favouritesList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
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

        adapter = new FavouritesRecyclerAdapter(new ArrayList<>(), this);
        favouritesList.setAdapter(adapter);

        ItemTouchHelper moveAdapter = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView,
                                           RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                final int fromPos = viewHolder.getAdapterPosition();
                final int toPos = target.getAdapterPosition();
                // move item in `fromPos` to `toPos` in adapter.
                return adapter.moveItem(fromPos, toPos);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                adapter.removeFavourite(position);
            }

        });
        moveAdapter.attachToRecyclerView(favouritesList);

        sp = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());

        Log.d("MainMenu", "Checking for app updates");
        new AppUpdateInitializer(this, sp, R.drawable.notification_icon, StaticVariables.BASE_SERVER_URL, true).checkForUpdate(true);
        Log.d("MainMenu", "onCreate complete");
    }

    @Override
    public void onResume(){
        super.onResume();

        fab.setOnClickListener(v -> startActivity(new Intent(MainMenuActivity.this, AddBusStopsRecyclerActivity.class)));
        fab.setOnLongClickListener(v -> {
            Toast.makeText(MainMenuActivity.this, R.string.fab_hint_main_menu, Toast.LENGTH_SHORT).show();
            return true;
        });

        //Android Stuff now again :D
        checkIfDatabaseUpdated();

        //Update Favourites
        swipeToRefresh.setRefreshing(true);
        updateFavourites();

        switch (sp.getString("companionDevice", "none")) {
            case "pebble": pebbleCard.setVisibility(View.VISIBLE); pebbleInit(); break;
            case "none":
                default: pebbleCard.setVisibility(View.GONE); deinitPebble();  break;
        }

        syncState.setClickable(true);
        syncState.setOnClickListener(v -> {
            // TODO: Start Activity for result
            startActivity(new Intent(getApplicationContext(), FirebaseLoginActivity.class));
        });
    }

    @Override
    public void onPause(){
        super.onPause();

        // Pebble related code
        deinitPebble();
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
        } else if (id == R.id.view_all_stops) {
            startActivity(new Intent(this, ListAllBusStopsActivity.class));
            return true;
        } else if (id == R.id.action_refresh) {
            swipeToRefresh.setRefreshing(true);
            updateFavourites();
            return true;
        } else if (id == R.id.action_install_companion) {
            switch (sp.getString("companionDevice", "none")) {
                case "pebble": installPebbleApp(); break;
                case "none":
                    default:
                        new AlertDialog.Builder(this).setTitle("No Companion Device Configured")
                            .setMessage("You have not configured a companion device! \n\nYou can do so in the app settings")
                            .setPositiveButton(android.R.string.ok, null)
                            .setNeutralButton(R.string.action_settings, (dialog, which) ->
                                    startActivity(new Intent(getApplicationContext(), MainSettings.class))).show();
                        break;
            }
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
        boolean busDBUpdate = false;
        if (busDBLastUpdate != -1){
            long day = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - busDBLastUpdate);
            Log.d("INIT", "Bus DB Last Update: " + day);
            if (day > 30)
                busDBUpdate = true;
        }

        // Check upgrade
        int dbver = sp.getInt("busDBVerCheck", 0);
        Log.d("DB UPGRADE", "Current DB Version: " + dbver);
        switch (dbver) {
            case 0:
            case 1: Log.i("DB UPGRADE", "Upgrading to V2 API DB"); busDBUpdate = true; sp.edit().putInt("busDBVerCheck", 2).apply(); break;
            case 2: Log.i("DB UPGRADE", "Upgrading to DB with Bus Services"); busDBUpdate = true; sp.edit().putInt("busDBVerCheck", 3).apply(); break;
        }

        //Main Database
        if (!sp.getBoolean("busDBLoaded", false) || busDBUpdate){
            //First Boot, populate database
            if (!ConnectivityHelper.hasInternetConnection(getApplicationContext())) {
                networkUnavailable(getString(R.string.database_name_bus));
            } else {
                Log.d("INIT", "Initializing Bus Stop Database");
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

                new GetAllBusStops(dialog, db, this, sp).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0);
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

    private static final int RC_HANDLE_REQUEST_EXTERNAL_STORAGE = 1;

    private void requestStoragePermission() {
        Log.w(PEBBLE_TAG, "Storage permission is not granted. Requesting permission");
        final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_REQUEST_EXTERNAL_STORAGE);
            return;
        }

        final Activity thisActivity = this;

        new AlertDialog.Builder(this).setTitle(R.string.dialog_title_request_permission_external_storage)
                .setMessage(R.string.dialog_message_request_permission_external_storage_rationale)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> ActivityCompat.requestPermissions(thisActivity, permissions, RC_HANDLE_REQUEST_EXTERNAL_STORAGE)).show();
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
            Log.d(PEBBLE_TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(PEBBLE_TAG, "Storage permission granted - download pebble file");
            // we have permission, so create the camerasource
            hasPermissionToInstallPebbleApp();
            return;
        }

        Log.e(PEBBLE_TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        final Activity thisActivity = this;

        new AlertDialog.Builder(this).setTitle(R.string.dialog_title_permission_denied)
                .setMessage(R.string.dialog_message_no_permission_external_storage).setPositiveButton(android.R.string.ok, null)
                .setNeutralButton(R.string.dialog_action_neutral_app_settings, (dialog, which) -> {
                    Intent permIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri packageURI = Uri.parse("package:" + thisActivity.getPackageName());
                    permIntent.setData(packageURI);
                    startActivity(permIntent);
                }).show();
    }

    // Pebble Related Code
    private PebbleKit.PebbleDataReceiver pebbleDataReceiver;
    private final String PEBBLE_TAG = "Pebble";

    private void pebbleInit() {
        boolean checkIfPebbleConnected = PebbleKit.isWatchConnected(this);
        if (checkIfPebbleConnected){
            PebbleKit.FirmwareVersionInfo info = PebbleKit.getWatchFWVersion(this);
            installPrompt.setVisibility(View.VISIBLE);
            //Init
            connectionStatus.setText(R.string.pebble_connected);
            connectionStatus.setTextColor(Color.GREEN);
            firmware.setText(getString(R.string.pebble_firmware_version, info.getTag()));
            pebbleDataReceiver = new PebbleKit.PebbleDataReceiver(StaticVariables.PEBBLE_APP_UUID) {
                @Override
                public void receiveData(Context context, int i, PebbleDictionary pebbleDictionary) {
                    PebbleKit.sendAckToPebble(getApplicationContext(), i);

                    //Handle stuff in the dictionary
                    if (pebbleDictionary.contains(PebbleEnum.KEY_BUTTON_EVENT)){
                        switch (pebbleDictionary.getUnsignedIntegerAsLong(PebbleEnum.KEY_BUTTON_EVENT).intValue()){
                            case PebbleEnum.BUTTON_REFRESH: pressedBtn.setText(R.string.pebble_button_refresh); break;
                            case PebbleEnum.BUTTON_NEXT: pressedBtn.setText(R.string.pebble_button_next); break;
                            case PebbleEnum.BUTTON_PREVIOUS: pressedBtn.setText(R.string.pebble_button_previous); break;
                        }
                    }
                }
            };
            PebbleKit.registerReceivedDataHandler(this, pebbleDataReceiver);

            pebbleCard.setOnClickListener(v -> installPebbleApp());
        } else {
            connectionStatus.setText(R.string.pebble_disconnected);
            connectionStatus.setTextColor(Color.RED);
            firmware.setText("");
            installPrompt.setVisibility(View.INVISIBLE);
            pebbleCard.setOnClickListener(null);
        }

        //Start Pebble Service if settings are set
        Intent pebbleService = new Intent(this, PebbleCommunications.class);
        if (sp.getBoolean("pebbleSvc", true)){
            startService(pebbleService);
        } else {
            stopService(pebbleService);
        }
    }

    private void deinitPebble() {
        if (pebbleDataReceiver != null)
            unregisterReceiver(pebbleDataReceiver);
    }

    private void installPebbleApp(){
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (rc == PackageManager.PERMISSION_GRANTED){
            hasPermissionToInstallPebbleApp();
        } else {
            requestStoragePermission();
        }
    }

    private void hasPermissionToInstallPebbleApp(){
        final Activity activity = this;
        new AlertDialog.Builder(this).setTitle("Which OS to install App to").setMessage("Based on your pebble device, " +
                "where should we launch the install request to?\n\nPebble Time: Select Pebble Time\n" +
                "Pebble with Time OS: Select Pebble Time\nPebble with 2.0 OS: Select Pebble").setPositiveButton("Pebble Time", (dialog, which) -> new DlAndInstallCompanionApp(activity, true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getString(R.string.link_pebble_app))).setNegativeButton("Pebble", (dialog, which) -> new DlAndInstallCompanionApp(activity, false).execute(getString(R.string.link_pebble_app))).setNeutralButton(android.R.string.cancel, null).show();
    }
}
