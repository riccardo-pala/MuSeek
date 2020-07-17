package com.riky.museek.classes

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import com.riky.museek.R
import com.riky.museek.fragments.SoldAdDetailsInstrumentFragment
import com.riky.museek.fragments.SoldAdsInstrumentFragment
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.ad_card_sold_ads_instrument.view.*
import java.text.NumberFormat
import java.util.*

class AdItemSoldAdsInstrument (private val ad: PurchasedAdInstrument, val view: View, val alertDialog: AlertDialog, private val stopLoading: Boolean): Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.ad_card_sold_ads_instrument
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val formatter = NumberFormat.getCurrencyInstance()
        formatter.maximumFractionDigits = 2
        formatter.currency = Currency.getInstance("EUR")

        viewHolder.itemView.brandTextViewSoldAdsInstr.text = ad.brand
        viewHolder.itemView.modelTextViewSoldAdsInstr.text = ad.model
        viewHolder.itemView.categoryTextViewSoldAdsInstr.text = DBManager.getCategoryById(ad.category)
        viewHolder.itemView.priceTextViewSoldAdsInstr.text = formatter.format(ad.price)

        setListeners(viewHolder)

        val ref = FirebaseStorage.getInstance().getReference("/images/instrument_ads/")
        ref.child(ad.photoId).getBytes(4*1024*1024)
            .addOnSuccessListener { bytes ->
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0 ,bytes.size)
                viewHolder.itemView.adPhotoSoldAdsInstr.setImageBitmap(bitmap)
                viewHolder.itemView.adPhotoFrameSoldAdsInstr.alpha = 1f
                if (stopLoading) {
                    alertDialog.dismiss()
                }
            }
            .addOnFailureListener {
                if (stopLoading) {
                    alertDialog.dismiss()
                }
            }
    }

    private fun setListeners(viewHolder: ViewHolder) {

        viewHolder.itemView.detailsButtonSoldAdsInstr.setOnClickListener {

            val adFragment = SoldAdDetailsInstrumentFragment()
            val args = Bundle()
            args.putString("aid", ad.aid)
            adFragment.arguments = args

            val context = viewHolder.itemView.context as AppCompatActivity
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, adFragment)
                .addToBackStack(SoldAdsInstrumentFragment::javaClass.name)
                .commit()
        }
    }
}