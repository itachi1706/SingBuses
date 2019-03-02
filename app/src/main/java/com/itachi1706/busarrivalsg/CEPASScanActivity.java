package com.itachi1706.busarrivalsg;

import android.os.Bundle;

import com.itachi1706.cepaslib.fragment.CEPASCardScanFragment;

import androidx.appcompat.app.AppCompatActivity;

public class CEPASScanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportFragmentManager().findFragmentById(android.R.id.content)==null) {
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, new CEPASCardScanFragment())
                    .commit();
        }
    }
}
