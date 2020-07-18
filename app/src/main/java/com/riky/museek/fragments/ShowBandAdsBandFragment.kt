package com.riky.museek.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.riky.museek.R
import com.riky.museek.activities.HomepageActivity
import com.riky.museek.classes.AdBandBand
import com.riky.museek.classes.AdItemShowBandAdsBand
import com.riky.museek.classes.AlertDialogInflater
import com.riky.museek.classes.DBManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_show_band_ads_band.*
import kotlinx.android.synthetic.main.fragment_show_band_ads_band.view.*

class ShowBandAdsBandFragment : Fragment() {

    private val adapter = GroupAdapter<ViewHolder>()
    private val adsMap = HashMap<String, AdBandBand>()
    private var viewer: View? = null
    private val STOP_LOADING = 2
    private var alertDialog : AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        viewer = inflater.inflate(R.layout.fragment_show_band_ads_band, container, false)

        if (context != null) DBManager.verifyLoggedUser(context!!)

        viewer!!.homeButtonShowBandAdsBand.setOnClickListener {
            val intentHomepage = Intent(activity, HomepageActivity::class.java)
            intentHomepage.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentHomepage)
        }

        try {
            requireArguments()
        }
        catch (e : IllegalStateException) {
            Toast.makeText(activity, "Errore durante il caricamento degli annunci. Riprova.", Toast.LENGTH_LONG).show()
            fragmentManager!!.popBackStack()
            fragmentManager!!.beginTransaction().replace(R.id.fragment, BandFragment()).commit()
            return viewer!!
        }

        alertDialog = AlertDialogInflater.inflateLoadingDialog(context!!, AlertDialogInflater.BLUE)

        viewer!!.recyclerViewShowBandAdsBand.adapter = adapter

        fetchAdsFromDatabase(arguments!!.getInt("regionSearch", 0), arguments!!.getInt("musicianSearch", 0))

        return viewer!!
    }

    private fun refreshRecyclerView(){
        adapter.clear()
        if (adsMap.isEmpty()){
            alertDialog!!.dismiss()
            noResultsTextViewShowBandAdsBand.visibility = View.VISIBLE
            return
        }
        val stop = if (adsMap.size>=STOP_LOADING) STOP_LOADING else adsMap.size
        var i = 1
        adsMap.values.forEach {
            adapter.add(AdItemShowBandAdsBand(it, viewer!!, alertDialog!!, i == stop))
            i++
        }
    }

    private fun fetchAdsFromDatabase(regionSearch: Int, musicianSearch: Int) {

        if (regionSearch == 0 || musicianSearch == 0) {
            alertDialog!!.dismiss()
            Toast.makeText(activity, "Errore durante il caricamento degli annunci. Riprova.", Toast.LENGTH_LONG).show()
            return
        }

        if (context != null) DBManager.verifyLoggedUser(context!!)

        val uid = FirebaseAuth.getInstance().uid

        val ref = FirebaseDatabase.getInstance().getReference("/band_band_ads/")

        var ad: AdBandBand

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (ads in dataSnapshot.children) {
                        val region = ads.child("region").value.toString().toInt()
                        val musician = ads.child("musician").value.toString().toInt()
                        val aduid = ads.child("uid").value.toString()
                        if (uid != aduid &&
                            region == regionSearch &&
                            musician == musicianSearch) {
                            ad = AdBandBand(
                                ads.key as String,
                                region,
                                musician,
                                ads.child("description").value.toString(),
                                aduid)
                            adsMap[ads.value.toString()] = ad
                        }
                    }
                    refreshRecyclerView()
                    ref.removeEventListener(this)
                }
                else {
                    alertDialog!!.dismiss()
                    noResultsTextViewShowBandAdsBand.visibility = View.VISIBLE
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                alertDialog!!.dismiss()
                noResultsTextViewShowBandAdsBand.visibility = View.VISIBLE
                Log.d(ShowBandAdsBandFragment::class.java.name, "ERROR on Database: ${databaseError.message}")
                ref.removeEventListener(this)
            }
        })
    }
}