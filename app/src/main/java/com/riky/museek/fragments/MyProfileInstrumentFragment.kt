package com.riky.museek.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.riky.museek.R
import com.riky.museek.activities.HomepageActivity
import kotlinx.android.synthetic.main.fragment_my_profile_instrument.view.*

class MyProfileInstrumentFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_my_profile_instrument, container, false)

        view.homeButtonMyProfileInstr.setOnClickListener {
            val intentHomepage = Intent(activity, HomepageActivity::class.java)
            intentHomepage.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentHomepage)
        }

        view.editAddressButtonMyProfileInstr.setOnClickListener {
            fragmentManager!!.beginTransaction()
                .replace(R.id.fragment, EditAddressInstrumentFragment())
                .addToBackStack(this.javaClass.name)
                .commit()
        }

        view.myAdsButtonMyProfileInstr.setOnClickListener {
            val fragment = MyAdsInstrumentFragment()
            val args = Bundle()
            args.putBoolean("imageLoaded", false)
            fragment.arguments = args
            fragmentManager!!.beginTransaction().replace(R.id.fragment, fragment).addToBackStack(this.javaClass.name).commit()
        }

        view.purchasedAdsButtonMyProfileInstr.setOnClickListener {
            fragmentManager!!.beginTransaction()
                .replace(R.id.fragment, PurchasedAdsInstrumentFragment())
                .addToBackStack(this.javaClass.name)
                .commit()
        }

        view.soldAdsButtonMyProfileInstr.setOnClickListener {
            fragmentManager!!.beginTransaction()
                .replace(R.id.fragment, SoldAdsInstrumentFragment())
                .addToBackStack(this.javaClass.name)
                .commit()
        }


        return view
    }
}