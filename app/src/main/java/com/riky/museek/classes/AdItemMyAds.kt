package com.riky.museek.classes

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import com.riky.museek.R
import com.riky.museek.fragments.EditAdInstrumentFragment
import com.riky.museek.fragments.MyAdsInstrumentFragment
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.ad_card_my_ads.view.*
import kotlinx.android.synthetic.main.fragment_edit_ad_instrument.view.*
import kotlinx.android.synthetic.main.fragment_edit_profile.view.*
import kotlinx.android.synthetic.main.fragment_my_ads_instrument.view.*
import java.text.NumberFormat
import java.util.*

class AdItemMyAds(val ad: Ad, val view: View, val stopLoading: Boolean): Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.ad_card_my_ads
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val formatter = NumberFormat.getCurrencyInstance()
        formatter.maximumFractionDigits = 2
        formatter.currency = Currency.getInstance("EUR")

        viewHolder.itemView.brandTextViewMyAdsInstr.text = ad.brand
        viewHolder.itemView.modelTextViewMyAdsInstr.text = ad.model
        viewHolder.itemView.categoryTextViewMyAdsInstr.text = DBManager.getCategoryById(ad.category)
        viewHolder.itemView.priceTextViewMyAdsInstr.text = formatter.format(ad.price)
        viewHolder.itemView.dateTextViewMyAdsInstr.text = ad.date.substring(0, 10)

        val ref = FirebaseStorage.getInstance().getReference("/images/instrument_ads/")
        ref.child(ad.photoId).getBytes(4*1024*1024)
            .addOnSuccessListener { bytes ->
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0 ,bytes.size)
                viewHolder.itemView.adPhotoMyAdsInstr.setImageBitmap(bitmap)
                viewHolder.itemView.adPhotoFrameMyAdsInstr.alpha = 1f
                if (stopLoading) {
                    view.loadingImageViewMyAdsInstr.clearAnimation()
                    view.loadingLayoutMyAdsInstr.visibility = View.GONE
                }
            }

        viewHolder.itemView.editButtonMyAdsInstr.setOnClickListener {
            val fragment = EditAdInstrumentFragment()
            val args = Bundle()
            args.putString("aid", ad.aid)
            args.putString("brand", ad.brand)
            args.putString("model", ad.model)
            args.putFloat("price", ad.price)
            args.putInt("category", ad.category)
            args.putString("photoId", ad.photoId)
            args.putString("date", ad.date)
            fragment.arguments = args

            val context = viewHolder.itemView.context as AppCompatActivity
            context.supportFragmentManager.beginTransaction().replace(R.id.fragment, fragment).addToBackStack(MyAdsInstrumentFragment::javaClass.name).commit()
        }

        viewHolder.itemView.deleteButtonMyAdsInstr.setOnClickListener {
            DBManager.deleteAdOnDatabase(ad.aid, ad.photoId)
            val context = viewHolder.itemView.context as AppCompatActivity
            context.supportFragmentManager.beginTransaction().replace(R.id.fragment, MyAdsInstrumentFragment()).addToBackStack(MyAdsInstrumentFragment::javaClass.name).commit()
        }

    }
}