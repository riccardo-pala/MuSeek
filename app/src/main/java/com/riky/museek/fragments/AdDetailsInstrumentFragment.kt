package com.riky.museek.fragments

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.riky.museek.R
import com.riky.museek.activities.HomepageActivity
import com.riky.museek.classes.AlertDialogInflater
import com.riky.museek.classes.DBManager
import kotlinx.android.synthetic.main.confirm_delete_popup_blue.*
import kotlinx.android.synthetic.main.confirm_purchase_popup_blue.*
import kotlinx.android.synthetic.main.fragment_ad_details_instrument.*
import kotlinx.android.synthetic.main.fragment_ad_details_instrument.view.*
import kotlinx.android.synthetic.main.loading_popup_blue.view.*
import java.text.NumberFormat
import java.util.*

class AdDetailsInstrumentFragment : Fragment() {

    private var aid : String? = ""
    private var alertDialog : AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_ad_details_instrument, container, false)

        if (context != null) DBManager.verifyLoggedUser(context!!)

        try {
            requireArguments()
        }
        catch (e : IllegalStateException) {
            Toast.makeText(activity, "Errore durante il caricamento dell'annuncio. Riprova.", Toast.LENGTH_LONG).show()
            return view
        }

        setListeners(view)

        alertDialog = AlertDialogInflater.inflateLoadingDialog(context!!, AlertDialogInflater.BLUE)

        aid = arguments!!.getString("aid", "")

        fetchSingleAdFromDatabase()

        return view
    }

    private fun fetchSingleAdFromDatabase() {

        val uid = FirebaseAuth.getInstance().uid

        val ref = FirebaseDatabase.getInstance().getReference("/instrument_ads/$aid")

        val formatter = NumberFormat.getCurrencyInstance()
        formatter.maximumFractionDigits = 2
        formatter.currency = Currency.getInstance("EUR")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val aduid = dataSnapshot.child("uid").value as String
                    if (aduid != uid) {
                        brandTextViewAdDetailsInstr.text = dataSnapshot.child("brand").value as String
                        modelTextViewAdDetailsInstr.text = dataSnapshot.child("model").value as String
                        categoryTextViewAdDetailsInstr.text = DBManager.getCategoryById(dataSnapshot.child("category").value.toString().toInt())
                        dateTextViewAdDetailsInstr.text = dataSnapshot.child("date").value.toString().substring(0, 10)
                        priceTextViewAdDetailsInstr.text = formatter.format(dataSnapshot.child("price").value.toString().toDouble())

                        val photoId = dataSnapshot.child("photoId").value as String
                        if (photoId.isNotEmpty()) {
                            val ref1 = FirebaseStorage.getInstance()
                                .getReference("/images/instrument_ads/")
                            ref1.child(photoId).getBytes(4 * 1024 * 1024)
                                .addOnSuccessListener { bytes ->
                                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    adPhotoAdDetailsInstr.setImageBitmap(bitmap)
                                    alertDialog!!.dismiss()
                                }
                                .addOnFailureListener {
                                    alertDialog!!.dismiss()
                                }
                        }
                        else {
                            alertDialog!!.dismiss()
                        }

                        val ref2 = DBManager.database.getReference("/users/$aduid")
                        ref2.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    val name = "${dataSnapshot.child("firstname").value.toString()} ${dataSnapshot.child("lastname").value.toString()}"
                                    userTextViewAdDetailsInstr.text = name
                                }
                                ref2.removeEventListener(this)
                            }
                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.d(DBManager::class.java.name, "ERROR on Database: ${databaseError.message}")
                                ref2.removeEventListener(this)
                            }
                        })
                    }
                    else {
                        alertDialog!!.dismiss()
                        Toast.makeText(activity, "Errore durante il caricamento dell'annuncio. Riprova.", Toast.LENGTH_LONG).show()
                    }
                }
                else {
                    alertDialog!!.dismiss()
                    Toast.makeText(activity, "Errore durante il caricamento dell'annuncio. Riprova.", Toast.LENGTH_LONG).show()
                }
                ref.removeEventListener(this)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(ShowAdsInstrumentFragment::class.java.name, "ERROR on Database: ${databaseError.message}")
                alertDialog!!.dismiss()
                Toast.makeText(activity, "Errore durante il caricamento dell'annuncio. Riprova.", Toast.LENGTH_LONG).show()
                ref.removeEventListener(this)
            }
        })
    }

    private fun setListeners(view: View) {

        view.homeButtonAdDetailsInstr.setOnClickListener {
            val intentHomepage = Intent(activity, HomepageActivity::class.java)
            intentHomepage.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentHomepage)
        }

        view.purchaseButtonAdDetailsInstr.setOnClickListener {
            if (context != null) {
                DBManager.verifyLoggedUser(context!!)

                var alertDialog = AlertDialogInflater.inflateConfirmPurchaseDialog(context!!, AlertDialogInflater.BLUE)

                alertDialog.confirmButtonConfirmPurchasePopup.setOnClickListener {
                    alertDialog.dismiss()
                    alertDialog = AlertDialogInflater.inflateLoadingDialog(context!!, AlertDialogInflater.BLUE)
                    DBManager.verifyInstrumentUser(context!!, alertDialog, aid)
                }
                alertDialog.cancelButtonConfirmPurchasePopup.setOnClickListener {
                    alertDialog.dismiss()
                }
            }
        }
    }
}