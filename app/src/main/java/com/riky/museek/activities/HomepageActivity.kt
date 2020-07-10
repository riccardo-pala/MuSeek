package com.riky.museek.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.riky.museek.R
import com.riky.museek.classes.DBManager
import com.riky.museek.fragments.HomepageFragment
import com.riky.museek.fragments.InstrumentFragment
import com.riky.museek.fragments.MyProfileFragment
import kotlinx.android.synthetic.main.fragment_homepage.*

class HomepageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        supportFragmentManager.beginTransaction().replace(R.id.fragment, HomepageFragment()).commit()
    }
}