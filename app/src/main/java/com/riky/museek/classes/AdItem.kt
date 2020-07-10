package com.riky.museek.classes

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import com.riky.museek.R
import com.riky.museek.fragments.AdDetailsInstrumentFragment
import com.riky.museek.fragments.ShowAdsInstrumentFragment
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.ad_card.view.*
import java.text.NumberFormat
import java.util.*

class AdItem (val ad: Ad): Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.ad_card
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val formatter = NumberFormat.getCurrencyInstance()
        formatter.maximumFractionDigits = 2
        formatter.currency = Currency.getInstance("EUR")

        viewHolder.itemView.brandTextViewShowAdsInstr.text = ad.brand
        viewHolder.itemView.modelTextViewShowAdsInstr.text = ad.model
        viewHolder.itemView.categoryTextViewShowAdsInstr.text = DBManager.getCategoryById(ad.category)
        viewHolder.itemView.priceTextViewShowAdsInstr.text = formatter.format(ad.price)

        val ref = FirebaseStorage.getInstance().getReference("/images/")
        ref.child(ad.photoId).getBytes(4*1024*1024)
            .addOnSuccessListener { bytes ->
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0 ,bytes.size)
                viewHolder.itemView.adPhotoShowAdsInstr.setImageBitmap(bitmap)
                viewHolder.itemView.adPhotoFrameShowAdsInstr.alpha = 1f
            }

        viewHolder.itemView.detailsButtonShowAdsInstr.setOnClickListener {

            val adFragment = AdDetailsInstrumentFragment()
            val args = Bundle()
            args.putString("aid", ad.aid)
            adFragment.arguments = args

            val context = viewHolder.itemView.context as AppCompatActivity
            context.supportFragmentManager.beginTransaction().replace(R.id.fragment, adFragment).addToBackStack(ShowAdsInstrumentFragment::javaClass.name).commit()
        }
    }
}