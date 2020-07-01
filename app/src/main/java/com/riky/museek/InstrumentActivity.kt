package com.riky.museek

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_instrument.*

class InstrumentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instrument)

        val uid = FirebaseAuth.getInstance().uid

        if (uid == null) {
            var intentMain = Intent(this, IndexActivity::class.java)
            intentMain.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentMain)
        }

        homeButtonInstr.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            var intentHomepage = Intent(this, HomepageActivity::class.java)
            intentHomepage.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentHomepage)
        }

        newAdButtonInstr.setOnClickListener {
            var intentNewAd = Intent(this, NewAdActivity::class.java)
            startActivity(intentNewAd)
        }

    }
}