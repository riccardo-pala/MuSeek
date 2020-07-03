package com.riky.museek.classes

import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.riky.museek.R
import com.riky.museek.fragments.MyAdsInstrumentFragment
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.ad_card.view.*
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class AdItem(val ad: Ad): Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.ad_card
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val formatter = NumberFormat.getCurrencyInstance()
        formatter.maximumFractionDigits = 2
        formatter.currency = Currency.getInstance("EUR")

        viewHolder.itemView.brandTextViewMyAdsInstr.text = ad.brand
        viewHolder.itemView.modelTextViewMyAdsInstr.text = ad.model
        viewHolder.itemView.categoryTextViewMyAdsInstr.text = ad.category
        viewHolder.itemView.priceTextViewMyAdsInstr.text = formatter.format(ad.price)
        viewHolder.itemView.dateTextViewMyAdsInstr.text = ad.date.substring(0, 10)
        viewHolder.itemView.userTextViewMyAdsInstr.text = DBManager.getEmailByUid(ad.uid)

        val ref = FirebaseStorage.getInstance().getReference("/images/")
        ref.child(ad.photoId).downloadUrl.addOnSuccessListener {
            Picasso.get().load(it).into(viewHolder.itemView.adPhotoMyAdsInstr)
        }.addOnFailureListener {
            Log.d(MyAdsInstrumentFragment::class.java.name, "ERROR while loading image from Storage")
        }
    }
}