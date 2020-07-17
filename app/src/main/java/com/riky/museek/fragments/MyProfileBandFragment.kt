package com.riky.museek.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.riky.museek.R
import com.riky.museek.activities.HomepageActivity
import com.riky.museek.classes.DBManager
import kotlinx.android.synthetic.main.fragment_band.view.*
import kotlinx.android.synthetic.main.fragment_my_profile_band.view.*
import kotlinx.android.synthetic.main.fragment_my_profile_instrument.view.*

class MyProfileBandFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_my_profile_band, container, false)

        DBManager.verifyLoggedUser(context!!)

        view.homeButtonMyProfileBand.setOnClickListener {
            val intentHomepage = Intent(activity, HomepageActivity::class.java)
            intentHomepage.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentHomepage)
        }

        view.myBandAdsButtonMyProfileBand.setOnClickListener {
            val fragment = MyBandAdsBandFragment()
            val args = Bundle()
            args.putBoolean("loaded", false)
            fragment.arguments = args
            fragmentManager!!.beginTransaction()
                .replace(R.id.fragment, fragment)
                .addToBackStack(null)
                .commit()
        }

        view.myMemberAdsButtonMyProfileBand.setOnClickListener {
            val fragment = MyMemberAdsBandFragment()
            val args = Bundle()
            args.putBoolean("loaded", false)
            fragment.arguments = args
            fragmentManager!!.beginTransaction()
                .replace(R.id.fragment, fragment)
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}