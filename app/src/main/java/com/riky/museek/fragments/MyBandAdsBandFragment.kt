package com.riky.museek.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.riky.museek.R
import com.riky.museek.activities.HomepageActivity
import com.riky.museek.classes.AdBandBand
import com.riky.museek.classes.AdItemMyBandAdsBand
import com.riky.museek.classes.AlertDialogInflater
import com.riky.museek.classes.DBManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_my_ads_instrument.*
import kotlinx.android.synthetic.main.fragment_my_band_ads_band.*
import kotlinx.android.synthetic.main.fragment_my_band_ads_band.view.*

class MyBandAdsBandFragment : Fragment() {

    private val adapter = GroupAdapter<ViewHolder>()
    private val adsMap = HashMap<String, AdBandBand>()
    private var viewer: View? = null
    private val STOP_LOADING = 3
    private var alertDialog : AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        viewer = inflater.inflate(R.layout.fragment_my_band_ads_band, container, false)

        if (context != null) DBManager.verifyLoggedUser(context!!)

        viewer!!.homeButtonMyBandAdsBand.setOnClickListener {
            val intentHomepage = Intent(activity, HomepageActivity::class.java)
            intentHomepage.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentHomepage)
        }

        alertDialog = AlertDialogInflater.inflateLoadingDialog(context!!, AlertDialogInflater.BLUE)
        alertDialog!!.dismiss()

        if (!arguments!!.getBoolean("loaded", true)) {
            alertDialog!!.show()
            arguments!!.clear()
        }

        viewer!!.recyclerViewMyBandAdsBand.adapter = adapter

        fetchMyAdsFromDatabase()

        return viewer
    }

    private fun refreshRecyclerView(){
        adapter.clear()
        if (adsMap.isEmpty()) {
            alertDialog!!.dismiss()
            noResultsTextViewMyBandAdsBand.visibility = View.VISIBLE
            return
        }
        val stop = if (adsMap.size>=STOP_LOADING) STOP_LOADING else adsMap.size
        var i = 1
        adsMap.values.forEach {
            adapter.add(AdItemMyBandAdsBand(it, viewer!!, alertDialog!!, i == stop))
            i++
        }
    }

    private fun fetchMyAdsFromDatabase() {

        val ref = FirebaseDatabase.getInstance().getReference("/band_band_ads/")

        val uid = FirebaseAuth.getInstance().uid

        var ad: AdBandBand

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (ads in dataSnapshot.children) {
                        if (ads.child("uid").value == uid) {
                            ad = AdBandBand(
                                ads.key as String,
                                ads.child("region").value.toString().toInt(),
                                ads.child("musician").value.toString().toInt(),
                                ads.child("description").value as String,
                                ads.child("uid").value as String)
                            adsMap[ads.value.toString()] = ad
                        }
                    }
                    refreshRecyclerView()
                }
                else {
                    alertDialog!!.dismiss()
                    noResultsTextViewMyAdsInstr.visibility = View.VISIBLE
                }
                ref.removeEventListener(this)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                alertDialog!!.dismiss()
                noResultsTextViewMyAdsInstr.visibility = View.VISIBLE
                Log.d(MyAdsInstrumentFragment::class.java.name, "ERROR on Database: ${databaseError.message}")
                ref.removeEventListener(this)
            }
        })
    }
}