package com.riky.museek.classes

import android.app.AlertDialog
import android.util.Log
import android.view.View
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.riky.museek.R
import com.riky.museek.fragments.ShowBandAdsBandFragment
import com.riky.museek.fragments.ShowMemberAdsBandFragment
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.ad_card_show_band_ads_band.view.*
import kotlinx.android.synthetic.main.ad_card_show_member_ads_band.view.*

class AdItemShowBandAdsBand(private val ad: AdBandBand, val view: View, val alertDialog: AlertDialog, private val stopLoading: Boolean): Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.ad_card_show_band_ads_band
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.regionTextViewShowBandAdsBand.text = DBManager.getRegionById(ad.region)
        viewHolder.itemView.musicianTextViewShowBandAdsBand.text = DBManager.getMusicianById(ad.musician)
        viewHolder.itemView.descriptionTextViewShowBandAdsBand.text = ad.description

        val ref = DBManager.database.getReference("/users/${ad.uid}")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(user: DataSnapshot) {
                if (user.exists()) {
                    val name = user.child("firstname").value.toString() + " " + user.child("lastname").value.toString()
                    val email = user.child("email").value.toString()
                    val phone = user.child("phone").value.toString()

                    viewHolder.itemView.userTextViewShowBandAdsBand.text = name
                    viewHolder.itemView.emailTextViewShowBandAdsBand.text = email
                    viewHolder.itemView.phoneTextViewShowBandAdsBand.text = phone
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(ShowBandAdsBandFragment::class.java.name, "ERROR on Database: ${databaseError.message}")
                ref.removeEventListener(this)
            }
        })

        if (stopLoading) {
            alertDialog.dismiss()
        }
    }
}