package com.riky.museek.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.riky.museek.R
import com.riky.museek.activities.HomepageActivity
import com.riky.museek.activities.MainActivity
import com.riky.museek.classes.Ad
import com.riky.museek.classes.AdItemMyAds
import com.riky.museek.classes.DBManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_edit_profile.view.*
import kotlinx.android.synthetic.main.fragment_my_ads_instrument.*
import kotlinx.android.synthetic.main.fragment_my_ads_instrument.view.*
import kotlinx.android.synthetic.main.fragment_show_ads_instrument.*

class MyAdsInstrumentFragment : Fragment() {

    private val adapter = GroupAdapter<ViewHolder>()
    private val adsMap = HashMap<String, Ad>()
    private var viewer: View? = null
    private val STOP_LOADING = 3

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        viewer = inflater.inflate(R.layout.fragment_my_ads_instrument, container, false)

        if (context != null) DBManager.verifyLoggedUser(context!!)

        val animation = RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        animation.interpolator = LinearInterpolator()
        animation.repeatCount = Animation.INFINITE
        animation.duration = 700

        viewer!!.loadingImageViewMyAdsInstr.startAnimation(animation);

        viewer!!.homeButtonMyAdsInstr.setOnClickListener {
            val intentHomepage = Intent(activity, HomepageActivity::class.java)
            intentHomepage.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentHomepage)
        }

        viewer!!.recyclerViewMyAdsInstr.adapter = adapter

        fetchMyAdsFromDatabase()

        return viewer
    }

    private fun refreshRecyclerView(){
        adapter.clear()
        if (adsMap.isEmpty()){
            loadingImageViewMyAdsInstr.clearAnimation()
            loadingLayoutMyAdsInstr.visibility = View.GONE
            noResultsTextViewMyAdsInstr.visibility = View.VISIBLE
            return
        }
        var i = 1
        adsMap.values.forEach{
            adapter.add(AdItemMyAds(it, viewer!!, i == STOP_LOADING))
            i++
        }
    }

    fun fetchMyAdsFromDatabase() {

        val ref = FirebaseDatabase.getInstance().getReference("/instrument_ads/")

        val uid = FirebaseAuth.getInstance().uid
        Log.d(MyAdsInstrumentFragment::class.java.name, "Uid: $uid")

        var ad: Ad

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (ads in dataSnapshot.children) {
                        if (ads.child("uid").value == uid) {
                            ad = Ad(
                                ads.key as String,
                                ads.child("brand").value as String,
                                ads.child("model").value as String,
                                ads.child("price").value.toString().toFloat(),
                                ads.child("category").value.toString().toInt(),
                                ads.child("photoId").value as String,
                                ads.child("uid").value as String,
                                ads.child("date").value as String)
                            adsMap[ads.value.toString()] = ad
                        }
                    }
                    refreshRecyclerView()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(MyAdsInstrumentFragment::class.java.name, "ERROR on Database: ${databaseError.message}")
            }
        })
    }
}