package com.riky.museek.classes

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.riky.museek.R
import com.riky.museek.fragments.EditBandAdBandFragment
import com.riky.museek.fragments.MyBandAdsBandFragment
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.ad_card_my_band_ads_band.view.*
import kotlinx.android.synthetic.main.confirm_delete_popup_blue.*

class AdItemMyBandAdsBand (private val ad: AdBandBand, val view: View, val alertDialog: AlertDialog, private val stopLoading: Boolean): Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.ad_card_my_band_ads_band
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.regionTextViewMyBandAdsBand.text = DBManager.getRegionById(ad.region)
        viewHolder.itemView.musicianTextViewMyBandAdsBand.text = DBManager.getMusicianById(ad.musician)
        viewHolder.itemView.descriptionTextViewMyBandAdsBand.text = ad.description

        setListeners(viewHolder)

        if (stopLoading) {
            alertDialog.dismiss()
        }
    }

    private fun setListeners(viewHolder: ViewHolder) {


        viewHolder.itemView.editButtonMyBandAdsBand.setOnClickListener {
            val fragment = EditBandAdBandFragment()
            val args = Bundle()
            args.putString("aid", ad.aid)
            args.putInt("region", ad.region)
            args.putInt("musician", ad.musician)
            args.putString("description", ad.description)
            fragment.arguments = args

            val context = viewHolder.itemView.context as AppCompatActivity
            context.supportFragmentManager.beginTransaction().replace(R.id.fragment, fragment).addToBackStack(null).commit()
        }

        viewHolder.itemView.deleteButtonMyBandAdsBand.setOnClickListener {
            val alertDialog = AlertDialogInflater.inflateConfirmDeleteDialog(view.context, AlertDialogInflater.RED)

            alertDialog.confirmButtonConfirmDeletePopup.setOnClickListener {
                DBManager.deleteBandAdOnDatabase(ad.aid)
                alertDialog.dismiss()
                val context = viewHolder.itemView.context as AppCompatActivity
                val fragment = MyBandAdsBandFragment()
                val args = Bundle()
                args.putBoolean("loaded", true)
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