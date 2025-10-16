package com.itachi1706.busarrivalsg

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.itachi1706.cepaslib.CEPASLibBuilder
import com.itachi1706.cepaslib.app.feature.main.MainActivity

class CEPASScanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val primaryColorRec = obtainStyledAttributes(intArrayOf(com.google.android.material.R.attr.colorPrimarySurface))
        val accentColorRec = obtainStyledAttributes(intArrayOf(com.google.android.material.R.attr.colorSecondary))
        val errorColorRec = obtainStyledAttributes(intArrayOf(com.google.android.material.R.attr.colorOnError))

        CEPASLibBuilder.apply {
            setPreferenceClass(MainSettings::class.java)
            updateTitleBarColor(primaryColorRec.getResourceId(0, 0))
            updateAccentColor(accentColorRec.getResourceId(0, 0))
            updateErrorColor(errorColorRec.getResourceId(0, 0))
            customTitle = "Scan EZ-Link Card"
            homeScreenWithBackButton = true
        }
        primaryColorRec.recycle()
        accentColorRec.recycle()
        errorColorRec.recycle()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}