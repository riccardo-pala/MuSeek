package com.riky.museek.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.riky.museek.R
import com.riky.museek.classes.DBManager
import kotlinx.android.synthetic.main.activity_homepage.*

class HomepageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        DBManager.verifyLoggedUser(this)

        instrumentButton.setOnClickListener {
            val intentInstrument = Intent(this, InstrumentActivity::class.java)
            startActivity(intentInstrument)
        }

        bandButton.setOnClickListener {
            val intentBand = Intent(this, BandActivity::class.java)
            startActivity(intentBand)
        }

        logoutButtonHome.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intentMain = Intent(this, MainActivity::class.java)
            intentMain.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentMain)
        }
    }
}