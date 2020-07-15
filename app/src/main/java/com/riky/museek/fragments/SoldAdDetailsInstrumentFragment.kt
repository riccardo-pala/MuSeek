package com.riky.museek.fragments

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.android.synthetic.main.confirm_notify_popup_blue.*
import kotlinx.android.synthetic.main.confirm_purchase_popup_blue.*
import kotlinx.android.synthetic.main.fragment_ad_details_instrument.*
import kotlinx.android.synthetic.main.fragment_ad_details_instrument.view.*
import kotlinx.android.synthetic.main.fragment_sold_ad_details_instrument.*
import kotlinx.android.synthetic.main.fragment_sold_ad_details_instrument.view.*
import java.text.NumberFormat
import java.util.*

class SoldAdDetailsInstrumentFragment : Fragment() {

    private var aid : String? = ""
    private var alertDialog : AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_sold_ad_details_instrument, container, false)

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

        if(context != null) DBManager.verifyLoggedUser(context!!)

        val ref = FirebaseDatabase.getInstance().getReference("/instrument_purchased_ads/$aid")

        val formatter = NumberFormat.getCurrencyInstance()
        formatter.maximumFractionDigits = 2
        formatter.currency = Currency.getInstance("EUR")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(ad: DataSnapshot) {
                if (ad.exists()) {

                    val buyeruid = ad.child("buyeruid").value as String

                    brandTextViewSoldAdDetailsInstr.text = ad.child("brand").value as String
                    modelTextViewSoldAdDetailsInstr.text = ad.child("model").value as String
                    categoryTextViewSoldAdDetailsInstr.text = DBManager.getCategoryById(ad.child("category").value.toString().toInt())
                    dateTextViewSoldAdDetailsInstr.text = ad.child("date").value.toString().substring(0, 10)
                    priceTextViewSoldAdDetailsInstr.text = formatter.format(ad.child("price").value.toString().toDouble())

                    if (ad.child("send").value.toString().toBoolean()) {
                        sendButtonSoldAdDetailsInstr.setBackgroundResource(R.drawable.shadow_button_grey_light)
                        sendButtonSoldAdDetailsInstr.text = "Già Inviato"
                    }

                    val photoId = ad.child("photoId").value as String
                    if (photoId.isNotEmpty()) {
                        val ref1 = FirebaseStorage.getInstance()
                            .getReference("/images/instrument_ads/")
                        ref1.child(photoId).getBytes(4 * 1024 * 1024)
                            .addOnSuccessListener { bytes ->
                                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                adPhotoSoldAdDetailsInstr.setImageBitmap(bitmap)
                                alertDialog!!.dismiss()
                            }
                            .addOnFailureListener {
                                alertDialog!!.dismiss()
                            }
                    }
                    else {
                        alertDialog!!.dismiss()
                    }

                    val ref2 = DBManager.database.getReference("/users/$buyeruid")
                    ref2.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(user: DataSnapshot) {
                            if (user.exists()) {
                                val name = "${user.child("firstname").value.toString()} ${user.child("lastname").value.toString()}"
                                userTextViewSoldAdDetailsInstr.text = name
                            }
                            ref2.removeEventListener(this)
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.d(DBManager::class.java.name, "ERROR on Database: ${databaseError.message}")
                            ref2.removeEventListener(this)
                        }
                    })

                    val ref3 = DBManager.database.getReference("/instrument_users/$buyeruid")
                    ref3.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(user: DataSnapshot) {

                            if (user.exists()) {
                                val nation = user.child("nation").value.toString()
                                val city = user.child("city").value.toString()
                                val street = user.child("street").value.toString()
                                val civic = user.child("civic").value.toString()
                                val inner = user.child("inner").value.toString()
                                val cap = user.child("cap").value.toString()

                                val address = "Via $street $civic" + if (inner != "") {"/$inner"} else {""} + ",\n$city $cap, $nation"

                                addressTextViewSoldAdDetailsInstr.text = address
                            }
                            ref3.removeEventListener(this)
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.d(DBManager::class.java.name, "ERROR on Database: ${databaseError.message}")
                            ref3.removeEventListener(this)
                        }
                    })
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

        view.homeButtonSoldAdDetailsInstr.setOnClickListener {
            val intentHomepage = Intent(activity, HomepageActivity::class.java)
            intentHomepage.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentHomepage)
        }

        view.sendButtonSoldAdDetailsInstr.setOnClickListener {
            if (context != null) {
                DBManager.verifyLoggedUser(context!!)

                var alertDialog = AlertDialogInflater.inflateConfirmNotifyDialog(context!!, AlertDialogInflater.BLUE)

                alertDialog.confirmButtonConfirmNotifyPopup.setOnClickListener {
                    alertDialog.dismiss()
                    alertDialog = AlertDialogInflater.inflateLoadingDialog(context!!, AlertDialogInflater.BLUE)
                    DBManager.performNotify(context!!, alertDialog, aid, view)
                }
                alertDialog.cancelButtonConfirmNotifyPopup.setOnClickListener {
                    alertDialog.dismiss()
                }
            }
        }
    }
}