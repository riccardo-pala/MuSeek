package com.riky.museek.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.riky.museek.activities.HomepageActivity
import com.riky.museek.R
import kotlinx.android.synthetic.main.fragment_instrument.view.*

class InstrumentFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_instrument, container, false)

        view.homeButtonInstr.setOnClickListener {
            val intentHomepage = Intent(activity, HomepageActivity::class.java)
            intentHomepage.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentHomepage)
        }

        view.newAdButtonInstr.setOnClickListener {
            fragmentManager!!.beginTransaction().replace(R.id.fragment, NewAdInstrumentFragment()).addToBackStack(null).commit()
        }

        view.myAdsButtonInstr.setOnClickListener {
            fragmentManager!!.beginTransaction().replace(R.id.fragment, MyAdsInstrumentFragment()).addToBackStack(null).commit()
        }

        return view
    }
}