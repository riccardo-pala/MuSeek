package com.riky.museek.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.riky.museek.R
import com.riky.museek.classes.DBManager
import com.riky.museek.fragments.InstrumentFragment
import com.riky.museek.fragments.MyProfileFragment
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

        myProfileButtonHome.setOnClickListener {
            supportFragmentManager.beginTransaction().replace(R.id.fragment, MyProfileFragment()).commit()
        }
    }
}