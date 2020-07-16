package com.riky.museek.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.riky.museek.R
import com.riky.museek.classes.DBManager
import kotlinx.android.synthetic.main.fragment_my_profile_band.view.*
import kotlinx.android.synthetic.main.fragment_my_profile_instrument.view.*

class MyProfileBandFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_my_profile_band, container, false)

        DBManager.verifyLoggedUser(context!!)

        view.editPreferencesButtonMyProfileBand.setOnClickListener {
            fragmentManager!!.beginTransaction()
                .replace(R.id.fragment, EditPreferencesBandFragment())
                .addToBackStack(this.javaClass.name)
                .commit()
        }

        return view
    }
}