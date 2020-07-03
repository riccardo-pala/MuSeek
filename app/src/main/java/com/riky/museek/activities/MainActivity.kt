package com.riky.museek.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.riky.museek.R
import com.riky.museek.fragments.IndexFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction().replace(
            R.id.fragment,
            IndexFragment()
        ).commit()
    }
}