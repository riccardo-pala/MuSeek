package com.riky.museek.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.riky.museek.R
import com.riky.museek.activities.BandActivity
import com.riky.museek.activities.InstrumentActivity
import com.riky.museek.classes.DBManager
import kotlinx.android.synthetic.main.fragment_homepage.view.*

class HomepageFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_homepage, container, false)

        DBManager.verifyLoggedUser(context!!)

        view.instrumentButton.setOnClickListener {
            val intentInstrument = Intent(activity, InstrumentActivity::class.java)
            startActivity(intentInstrument)
        }

        view.bandButton.setOnClickListener {
            val intentBand = Intent(activity, BandActivity::class.java)
            startActivity(intentBand)
        }

        view.myProfileButtonHome.setOnClickListener {
            fragmentManager!!.beginTransaction()
                .replace(R.id.fragment, MyProfileFragment())
                .addToBackStack(this.javaClass.name)
                .commit()
        }

        return view
    }
}