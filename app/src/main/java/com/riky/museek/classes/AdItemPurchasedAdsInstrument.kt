package com.riky.museek.classes

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.riky.museek.R
import com.riky.museek.activities.HomepageActivity
import com.riky.museek.fragments.ReviewInstrumentFragment
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.ad_card_purchased_ads_instrument.view.*
import kotlinx.android.synthetic.main.fragment_purchased_ads_instrument.view.*
import kotlinx.android.synthetic.main.review_popup_blue.*
import java.text.NumberFormat
import java.util.*

class AdItemPurchasedAdsInstrument (private val ad: PurchasedAdInstrument, val view: View, var alertDialog: AlertDialog, private val stopLoading: Boolean): Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.ad_card_purchased_ads_instrument
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

                    val spanName = SpannableString(name)
                    spanName.setSpan(UnderlineSpan(), 0, spanName.length, 0)

                    viewHolder.itemView.sellerTextViewPurchasedAdsInstr.text = spanName

                    viewHolder.itemView.sellerTextViewPurchasedAdsInstr.setOnClickListener {

                        val fragment = ReviewInstrumentFragment()
                        val args = Bundle()
                        args.putString("uid", ad.selleruid)
                        fragment.arguments = args

                        val context = viewHolder.itemView.context as AppCompatActivity
                        context.supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment, fragment)
                            .addToBackStack(this.javaClass.name)
                            .commit()
                    }

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

        val ref = DBManager.database.getReference("/instrument_purchased_ads/${ad.aid}")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(pAd: DataSnapshot) {
                if (pAd.exists()) {
                    if (pAd.child("isReviewed").value.toString().toBoolean()) {
                        viewHolder.itemView.reviewButtonPurchasedAdsInstr.setBackgroundResource((R.drawable.shadow_button_grey_light))
                        viewHolder.itemView.reviewButtonPurchasedAdsInstr.text = "Già Recensito"
                        viewHolder.itemView.reviewButtonPurchasedAdsInstr.setOnClickListener(null)
                    }
                    else {
                        setReviewButton(viewHolder)
                    }
                }
                ref.removeEventListener(this)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(DBManager::class.java.name, "ERROR on Database: ${databaseError.message}")
                ref.removeEventListener(this)
            }
        })
    }

    private fun setReviewButton(viewHolder: ViewHolder) {

        viewHolder.itemView.reviewButtonPurchasedAdsInstr.setOnClickListener {
            alertDialog = AlertDialogInflater.inflateReviewDialog(view.context, AlertDialogInflater.BLUE)

            alertDialog.submitButtonReviewPopup.setOnClickListener setListener@ {

                alertDialog.reviewEditTextReviewPopup.setBackgroundResource(R.drawable.shadow_edit_text_grey_light)

                val reviewValue = alertDialog.reviewEditTextReviewPopup.text.toString().toDouble()

                if (reviewValue > 5.0 || reviewValue < 0.0) {
                    Toast.makeText(view.context, "Si prega di inserire un valore compreso tra 0 e 5", Toast.LENGTH_LONG).show()
                    alertDialog.reviewEditTextReviewPopup.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
                    return@setListener
                }

                alertDialog.dismiss()
                alertDialog = AlertDialogInflater.inflateLoadingDialog(view.context, AlertDialogInflater.BLUE)

                val ref1 = DBManager.database.getReference("/instrument_users/${ad.selleruid}")

                ref1.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val actualReviewAverage = dataSnapshot.child("reviewAverage").value.toString().toDouble()
                            val actualReviewNo = dataSnapshot.child("reviewNo").value.toString().toDouble()
                            ref1.child("reviewAverage").setValue(((actualReviewAverage*actualReviewNo) + reviewValue) / (actualReviewNo+1.0))
                            ref1.child("reviewNo").setValue(actualReviewNo+1)
                            val ref2 = DBManager.database.getReference("/instrument_purchased_ads/${ad.aid}")
                            ref2.child("isReviewed").setValue(true)
                            viewHolder.itemView.reviewButtonPurchasedAdsInstr.setBackgroundResource((R.drawable.shadow_button_grey_light))
                            viewHolder.itemView.reviewButtonPurchasedAdsInstr.text = "Già Recensito"
                            viewHolder.itemView.reviewButtonPurchasedAdsInstr.setOnClickListener(null)
                            Toast.makeText(view.context, "La recensione è stata inviata con successo!", Toast.LENGTH_LONG).show()
                        }
                        else {
                            Toast.makeText(view.context, "Si è verificato un errore durante l'invio della recensione.", Toast.LENGTH_LONG).show()
                        }
                        ref1.removeEventListener(this)
                        alertDialog.dismiss()
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.d(DBManager::class.java.name, "ERROR on Database: ${databaseError.message}")
                        Toast.makeText(view.context, "Si è verificato un errore durante l'invio della recensione.", Toast.LENGTH_LONG).show()
                        ref1.removeEventListener(this)
                        alertDialog.dismiss()
                    }
                })
            }
        }
    }
}