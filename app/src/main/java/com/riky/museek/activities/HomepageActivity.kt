package com.riky.museek.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.riky.museek.R
import com.riky.museek.fragments.HomepageFragment
import com.riky.museek.services.MuSeekNotificationService

class HomepageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        val serviceIntent = Intent(this, MuSeekNotificationService::class.java)
        startService(serviceIntent)

        supportFragmentManager.beginTransaction().replace(R.id.fragment, HomepageFragment()).commit()
    }
}