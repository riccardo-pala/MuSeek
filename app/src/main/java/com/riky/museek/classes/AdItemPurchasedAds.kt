package com.riky.museek.classes

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.riky.museek.R
import com.riky.museek.activities.HomepageActivity
import com.riky.museek.fragments.EditAdInstrumentFragment
import com.riky.museek.fragments.MyAdsInstrumentFragment
import com.riky.museek.fragments.PurchasedAdsInstrumentFragment
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.ad_card_my_ads.view.*
import kotlinx.android.synthetic.main.ad_card_purchased_ads.view.*
import kotlinx.android.synthetic.main.confirm_delete_popup_blue.*
import kotlinx.android.synthetic.main.fragment_ad_details_instrument.*
import kotlinx.android.synthetic.main.fragment_my_ads_instrument.view.*
import kotlinx.android.synthetic.main.fragment_purchased_ads_instrument.view.*
import java.text.NumberFormat
import java.util.*

class AdItemPurchasedAds (private val ad: PurchasedAd, val view: View, val alertDialog: AlertDialog, private val stopLoading: Boolean): Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.ad_card_purchased_ads
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val formatter = NumberFormat.getCurrencyInstance()
        formatter.maximumFractionDigits = 2
        formatter.currency = Currency.getInstance("EUR")

        viewHolder.itemView.brandTextViewPurchasedAdsInstr.text = ad.brand
        viewHolder.itemView.modelTextViewPurchasedAdsInstr.text = ad.model
        viewHolder.itemView.categoryTextViewPurchasedAdsInstr.text = DBManager.getCategoryById(ad.category)
        viewHolder.itemView.priceTextViewPurchasedAdsInstr.text = formatter.format(ad.price)
        viewHolder.itemView.dateTextViewPurchasedAdsInstr.text = ad.date.substring(0, 10)

        setListeners(viewHolder)

        val ref = FirebaseStorage.getInstance().getReference("/images/instrument_ads/")
        ref.child(ad.photoId).getBytes(4*1024*1024)
            .addOnSuccessListener { bytes ->
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0 ,bytes.size)
                viewHolder.itemView.adPhotoPurchasedAdsInstr.setImageBitmap(bitmap)
                viewHolder.itemView.adPhotoFramePurchasedAdsInstr.alpha = 1f
                if (stopLoading) {
                    alertDialog.dismiss()
                }
            }
            .addOnFailureListener {
                if (stopLoading) {
                    alertDialog.dismiss()
                }
            }

        val ref2 = DBManager.database.getReference("/users/${ad.selleruid}")
        ref2.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val name = "${dataSnapshot.child("firstname").value.toString()} ${dataSnapshot.child("lastname").value.toString()}"
                    viewHolder.itemView.sellerTextViewPurchasedAdsInstr.text = name
                    ref2.removeEventListener(this)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(DBManager::class.java.name, "ERROR on Database: ${databaseError.message}")
                ref2.removeEventListener(this)
            }
        })
    }

    private fun setListeners(viewHolder: ViewHolder) {

        view.homeButtonPurchasedAdsInstr.setOnClickListener {
            val intentHomepage = Intent(viewHolder.itemView.context, HomepageActivity::class.java)
            intentHomepage.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            ContextCompat.startActivity(viewHolder.itemView.context, intentHomepage, null)
        }
    }
}