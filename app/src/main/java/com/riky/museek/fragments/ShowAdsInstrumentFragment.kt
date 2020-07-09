package com.riky.museek.fragments

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
import com.riky.museek.activities.MainActivity
import com.riky.museek.classes.Ad
import com.riky.museek.classes.AdItem
import com.riky.museek.classes.DBManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_show_ads_instrument.view.*

class ShowAdsInstrumentFragment : Fragment() {

    private val adapter = GroupAdapter<ViewHolder>()
    private val adsMap = HashMap<String, Ad>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_show_ads_instrument, container, false)

        if (context != null) DBManager.verifyLoggedUser(context!!)

        try {
            requireArguments()
        }
        catch (e : IllegalStateException) {
            Toast.makeText(activity, "Errore durante il caricamento degli annunci. Riprova.", Toast.LENGTH_LONG).show()
            fragmentManager!!.beginTransaction().replace(R.id.fragment, InstrumentFragment()).commit()
        }

        view.homeButtonShowAdsInstr.setOnClickListener {
            val intentHomepage = Intent(activity, HomepageActivity::class.java)
            intentHomepage.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentHomepage)
        }

        view.recyclerViewShowAdsInstr.adapter = adapter

        fetchAdsFromDatabase(arguments!!.getInt("searchType", 0), arguments!!.getString("searchValue", ""))

        return view
    }

    private fun refreshRecyclerView(){
        adapter.clear()
        adsMap.values.forEach{
            adapter.add(AdItem(it))
        }
    }

    fun fetchAdsFromDatabase(searchType: Int, searchValue: String) {

        if (context != null) DBManager.verifyLoggedUser(context!!)

        val uid = FirebaseAuth.getInstance().uid

        val ref = FirebaseDatabase.getInstance().getReference("/instrument_ads/")

        var ad: Ad

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    when (searchType) {
                        0 -> {
                            for (ads in dataSnapshot.children) {
                                val model = ads.child("model").value as String
                                val brand = ads.child("brand").value as String
                                val category = ads.child("category").value.toString().toInt()
                                val aduid = ads.child("uid").value as String
                                if ((model.contains(searchValue) ||
                                    brand.contains(searchValue) ||
                                    DBManager.getCategoryById(category).contains(searchValue)) &&
                                    aduid != uid) {
                                    ad = Ad(
                                        ads.key as String,
                                        brand,
                                        model,
                                        ads.child("price").value.toString().toFloat(),
                                        category,
                                        ads.child("photoId").value as String,
                                        ads.child("uid").value as String,
                                        ads.child("date").value as String)
                                    adsMap[ads.value.toString()] = ad
                                }
                            }
                        }
                        1 -> {
                            val categories = DBManager.getCategoryStringByType(searchValue)
                            for (ads in dataSnapshot.children) {
                                val category = ads.child("category").value.toString().toInt()
                                if (categories.contains(category)) {
                                    ad = Ad(
                                        ads.key as String,
                                        ads.child("brand").value as String,
                                        ads.child("model").value as String,
                                        ads.child("price").value.toString().toFloat(),
                                        category,
                                        ads.child("photoId").value as String,
                                        ads.child("uid").value as String,
                                        ads.child("date").value as String)
                                    adsMap[ads.value.toString()] = ad
                                }
                            }
                        }
                    }
                    refreshRecyclerView()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(ShowAdsInstrumentFragment::class.java.name, "ERROR on Database: ${databaseError.message}")
            }
        })
    }
}