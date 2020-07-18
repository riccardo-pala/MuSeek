package com.riky.museek.classes

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.riky.museek.R
import com.riky.museek.fragments.EditMemberAdBandFragment
import com.riky.museek.fragments.MyMemberAdsBandFragment
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.ad_card_my_member_ads_band.view.*
import kotlinx.android.synthetic.main.confirm_delete_popup_blue.*

class AdItemMyMemberAdsBand(private val ad: AdMemberBand, val view: View, val alertDialog: AlertDialog, private val stopLoading: Boolean): Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.ad_card_my_member_ads_band
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.bandNameTextViewMyMemberAdsBand.text = ad.bandName
        viewHolder.itemView.regionTextViewMyMemberAdsBand.text = DBManager.getRegionById(ad.region)
        viewHolder.itemView.musicianTextViewMyMemberAdsBand.text = DBManager.getMusicianById(ad.musician)
        viewHolder.itemView.descriptionTextViewMyMemberAdsBand.text = ad.description

        setListeners(viewHolder)

        if (stopLoading) {
            alertDialog.dismiss()
        }
    }

    private fun setListeners(viewHolder: ViewHolder) {


        viewHolder.itemView.editButtonMyMemberAdsBand.setOnClickListener {
            val fragment = EditMemberAdBandFragment()
            val args = Bundle()
            args.putString("aid", ad.aid)
            args.putString("bandName", ad.bandName)
            args.putInt("region", ad.region)
            args.putInt("musician", ad.musician)
            args.putString("description", ad.description)
            fragment.arguments = args

            val context = viewHolder.itemView.context as AppCompatActivity
            context.supportFragmentManager.beginTransaction().replace(R.id.fragment, fragment).addToBackStack(null).commit()
        }

        viewHolder.itemView.deleteButtonMyMemberAdsBand.setOnClickListener {
            val alertDialog = AlertDialogInflater.inflateConfirmDeleteDialog(view.context, AlertDialogInflater.RED)

            alertDialog.confirmButtonConfirmDeletePopup.setOnClickListener {
                DBManager.deleteMemberAdOnDatabase(ad.aid)
                alertDialog.dismiss()
                val context = viewHolder.itemView.context as AppCompatActivity
                val fragment = MyMemberAdsBandFragment()
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