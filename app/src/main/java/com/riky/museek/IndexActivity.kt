package com.riky.museek

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_index.*

class IndexActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_index)

        regButtonIndex.setOnClickListener {
            Log.d(IndexActivity::class.java.name, "Start Registration Intent")
            var intentReg = Intent(this, RegistrationActivity::class.java)
            startActivity(intentReg)
        }

        logButtonIndex.setOnClickListener {
            Log.d(IndexActivity::class.java.name, "Start Login Intent")
            var intentLogin = Intent(this, LoginActivity::class.java)
            startActivity(intentLogin)
        }
    }
}