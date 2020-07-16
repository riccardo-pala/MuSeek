package com.riky.museek.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.riky.museek.R
import com.riky.museek.fragments.BandFragment
import com.riky.museek.fragments.InstrumentFragment

class BandActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_band)

        supportFragmentManager.beginTransaction().replace(R.id.fragment, BandFragment()).commit()
    }
}