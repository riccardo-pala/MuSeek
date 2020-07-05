package com.riky.museek.classes

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.google.firebase.storage.FirebaseStorage
import com.riky.museek.R
import com.riky.museek.activities.InstrumentActivity
import com.riky.museek.fragments.EditAdInstrumentFragment
import com.riky.museek.fragments.MyAdsInstrumentFragment
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.ad_card.view.*
import kotlinx.android.synthetic.main.fragment_my_ads_instrument.view.*
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
/*
        val ref = FirebaseStorage.getInstance().getReference("/images/")
        ref.child(ad.photoId).downloadUrl.addOnSuccessListener {
            Picasso.get().load(it).into(viewHolder.itemView.adPhotoMyAdsInstr)
            viewHolder.itemView.adPhotoFrameMyAdsInstr.alpha = 1f
        }.addOnFailureListener {
            Log.d(MyAdsInstrumentFragment::class.java.name, "ERROR while loading image from Storage")
        }
*/
        val ref = FirebaseStorage.getInstance().getReference("/images/")
        ref.child(ad.photoId).getBytes(1024*1024)
            .addOnSuccessListener { bytes ->
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0 ,bytes.size)
                viewHolder.itemView.adPhotoMyAdsInstr.setImageBitmap(bitmap)
                viewHolder.itemView.adPhotoFrameMyAdsInstr.alpha = 1f
            }

        viewHolder.itemView.editButtonMyAdsInstr.setOnClickListener {
            val adFragment = EditAdInstrumentFragment()
            val args = Bundle()
            args.putString("aid", ad.aid)
            args.putString("brand", ad.brand)
            args.putString("model", ad.model)
            args.putFloat("price", ad.price)
            args.putString("category", ad.category)
            args.putString("photoId", ad.photoId)
            args.putString("date", ad.date)
            adFragment.arguments = args

            val context = viewHolder.itemView.context as AppCompatActivity
            context.supportFragmentManager.beginTransaction().replace(R.id.fragment, adFragment).addToBackStack(null).commit()
        }

        viewHolder.itemView.deleteButtonMyAdsInstr.setOnClickListener {
            DBManager.deleteAdOnDatabase(ad.aid, ad.photoId)
            val context = viewHolder.itemView.context as AppCompatActivity
            context.supportFragmentManager.beginTransaction().replace(R.id.fragment, MyAdsInstrumentFragment()).addToBackStack(null).commit()
        }

    }
}