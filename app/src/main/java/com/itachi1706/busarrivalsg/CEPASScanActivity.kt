package com.itachi1706.busarrivalsg

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.itachi1706.cepaslib.CEPASLibBuilder
import com.itachi1706.cepaslib.app.feature.main.MainActivity

class CEPASScanActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CEPASLibBuilder.apply {
            setPreferenceClass(MainSettings::class.java)
            updateTitleBarColor(R.color.primary)
            updateAccentColor(R.color.accent)
            updateErrorColor(R.color.primaryDark)
            customTitle = "Scan EZ-Link Card"
            homeScreenWithBackButton = true
        }
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}