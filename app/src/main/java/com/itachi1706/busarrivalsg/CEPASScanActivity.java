package com.itachi1706.busarrivalsg;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.itachi1706.appupdater.Util.PrefHelper;
import com.itachi1706.cepaslib.CEPASLibBuilder;
import com.itachi1706.cepaslib.app.feature.main.MainActivity;

public class CEPASScanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrefHelper.changeDarkModeTheme(AppCompatDelegate.MODE_NIGHT_YES, "default dark"); // TODO: Default app is on dark theme, remove after we support day/night mode
        CEPASLibBuilder builder = CEPASLibBuilder.INSTANCE;
        builder.setPreferenceClass(MainSettings.class);
        builder.updateTitleBarColor(R.color.primary);
        builder.updateAccentColor(R.color.accent);
        builder.updateErrorColor(R.color.primaryDark);
        builder.setCustomTitle("Scan EZ-Link Card");
        builder.setHomeScreenWithBackButton(true);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
