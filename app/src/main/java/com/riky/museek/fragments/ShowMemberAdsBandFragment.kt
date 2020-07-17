package com.riky.museek.fragments

import android.app.AlertDialog
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
import com.riky.museek.classes.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_show_ads_instrument.*
import kotlinx.android.synthetic.main.fragment_show_ads_instrument.view.*
import kotlinx.android.synthetic.main.fragment_show_member_ads_band.*
import kotlinx.android.synthetic.main.fragment_show_member_ads_band.view.*

class ShowMemberAdsBandFragment : Fragment() {

    private val adapter = GroupAdapter<ViewHolder>()
    private val adsMap = HashMap<String, AdMemberBand>()
    private var viewer: View? = null
    private val STOP_LOADING = 2
    private var alertDialog : AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        viewer = inflater.inflate(R.layout.fragment_show_member_ads_band, container, false)

        if (context != null) DBManager.verifyLoggedUser(context!!)

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

        viewer!!.recyclerViewShowMemberAdsBand.adapter = adapter

        fetchAdsFromDatabase(arguments!!.getInt("regionSearch", 0), arguments!!.getInt("musicianSearch", 0))

        return viewer!!
    }

    private fun refreshRecyclerView(){
        adapter.clear()
        if (adsMap.isEmpty()){
            alertDialog!!.dismiss()
            noResultsTextViewShowMemberAdsBand.visibility = View.VISIBLE
            return
        }
        val stop = if (adsMap.size>=STOP_LOADING) STOP_LOADING else adsMap.size
        var i = 1
        adsMap.values.forEach {
            adapter.add(AdItemShowMemberAdsBand(it, viewer!!, alertDialog!!, i == stop))
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

        val ref = FirebaseDatabase.getInstance().getReference("/band_member_ads/")

        var ad: AdMemberBand

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
                            ad = AdMemberBand(
                                ads.key as String,
                                ads.child("bandName").value.toString(),
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
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(ShowMemberAdsBandFragment::class.java.name, "ERROR on Database: ${databaseError.message}")
                ref.removeEventListener(this)
            }
        })
    }
}