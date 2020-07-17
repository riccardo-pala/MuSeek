package com.riky.museek.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import com.riky.museek.R
import com.riky.museek.activities.HomepageActivity
import com.riky.museek.classes.AlertDialogInflater
import com.riky.museek.classes.DBManager
import kotlinx.android.synthetic.main.choose_band_ad_notify_red.*
import kotlinx.android.synthetic.main.fragment_band.view.*

class BandFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_band, container, false)

        view.homeButtonBand.setOnClickListener {
            val intentHomepage = Intent(activity, HomepageActivity::class.java)
            intentHomepage.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentHomepage)
        }

        view.newAdButtonBand.setOnClickListener {
            var alertDialog = AlertDialogInflater.inflateLoadingDialog(context!!, AlertDialogInflater.RED)

            alertDialog.dismiss()

            alertDialog = AlertDialogInflater.inflateChooseBandAdDialog(context!!, AlertDialogInflater.RED)

            alertDialog.bandButtonChooseBandAdPopup.setOnClickListener {
                alertDialog.dismiss()
                fragmentManager!!.beginTransaction()
                    .replace(R.id.fragment, NewBandAdBandFragment())
                    .addToBackStack(null).commit()
            }

            alertDialog.memberButtonChooseBandAdPopup.setOnClickListener {
                alertDialog.dismiss()
                fragmentManager!!.beginTransaction()
                    .replace(R.id.fragment, NewMemberAdBandFragment())
                    .addToBackStack(null).commit()
            }

        }

        view.myProfileButtonBand.setOnClickListener {
            fragmentManager!!.beginTransaction().replace(R.id.fragment, MyProfileBandFragment()).addToBackStack(this.javaClass.name).commit()
        }

        return view
    }
}