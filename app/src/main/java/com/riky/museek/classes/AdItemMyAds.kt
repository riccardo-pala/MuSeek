package com.riky.museek.classes

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.google.firebase.storage.FirebaseStorage
import com.riky.museek.R
import com.riky.museek.activities.HomepageActivity
import com.riky.museek.fragments.EditAdInstrumentFragment
import com.riky.museek.fragments.MyAdsInstrumentFragment
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.ad_card_my_ads.view.*
import kotlinx.android.synthetic.main.confirm_delete_popup_blue.*
import kotlinx.android.synthetic.main.fragment_my_ads_instrument.view.*
import java.text.NumberFormat
import java.util.*

class AdItemMyAds(private val ad: Ad, val view: View, val alertDialog: AlertDialog, private val stopLoading: Boolean): Item<ViewHolder>() {

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

        setListeners(viewHolder)

        val ref = FirebaseStorage.getInstance().getReference("/images/instrument_ads/")
        ref.child(ad.photoId).getBytes(4*1024*1024)
            .addOnSuccessListener { bytes ->
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0 ,bytes.size)
                viewHolder.itemView.adPhotoMyAdsInstr.setImageBitmap(bitmap)
                viewHolder.itemView.adPhotoFrameMyAdsInstr.alpha = 1f
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

        view.homeButtonMyAdsInstr.setOnClickListener {
            val intentHomepage = Intent(viewHolder.itemView.context, HomepageActivity::class.java)
            intentHomepage.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(viewHolder.itemView.context, intentHomepage, null)
        }

        viewHolder.itemView.editButtonMyAdsInstr.setOnClickListener {
            val fragment = EditAdInstrumentFragment()
            val args = Bundle()
            args.putString("aid", ad.aid)
            args.putString("brand", ad.brand)
            args.putString("model", ad.model)
            args.putDouble("price", ad.price)
            args.putInt("category", ad.category)
            args.putInt("condition", ad.condition)
            args.putString("photoId", ad.photoId)
            args.putString("date", ad.date)
            fragment.arguments = args

            val context = viewHolder.itemView.context as AppCompatActivity
            context.supportFragmentManager.beginTransaction().replace(R.id.fragment, fragment).addToBackStack(null).commit()
        }

        viewHolder.itemView.deleteButtonMyAdsInstr.setOnClickListener {
            val alertDialog = AlertDialogInflater.inflateConfirmDeleteDialog(view.context, AlertDialogInflater.BLUE)

            alertDialog.confirmButtonConfirmDeletePopup.setOnClickListener {
                DBManager.deleteAdOnDatabase(ad.aid, ad.photoId)
                alertDialog.dismiss()
                val context = viewHolder.itemView.context as AppCompatActivity
                val fragment = MyAdsInstrumentFragment()
                val args = Bundle()
                args.putBoolean("imageLoaded", true)
                fragment.arguments = args
                context.supportFragmentManager.popBackStack()
                context.supportFragmentManager.beginTransaction().replace(R.id.fragment, fragment).commit()
            }
            alertDialog.cancelButtonConfirmDeletePopup.setOnClickListener {
                alertDialog.dismiss()
            }
        }
    }
}