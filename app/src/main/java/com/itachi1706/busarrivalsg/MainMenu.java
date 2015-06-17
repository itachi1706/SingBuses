package com.itachi1706.busarrivalsg;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;

public class MainMenu extends AppCompatActivity {

    //Pebble stuff
    private PebbleKit.PebbleDataReceiver mReceiver;
    private final static UUID PEBBLE_APP_UUID = UUID.fromString("11198668-4e27-4e94-b51c-a27a1ea5cd82");

    //Android Stuff
    private TextView connectionStatus, pressedBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        connectionStatus = (TextView) findViewById(R.id.pebbleConnectionStatus);
        pressedBtn = (TextView) findViewById(R.id.pressedBtn);
    }

    @Override
    public void onResume(){
        super.onResume();

        boolean checkIfPebbleConnected = PebbleKit.isWatchConnected(this);
        if (checkIfPebbleConnected){
            //Init
            connectionStatus.setText("Pebble Connected!");
            connectionStatus.setTextColor(Color.GREEN);
            mReceiver = new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {
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
        } else {
            connectionStatus.setText("Pebble not connected!");
            connectionStatus.setTextColor(Color.RED);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        unregisterReceiver(mReceiver);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
