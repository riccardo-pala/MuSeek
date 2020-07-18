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
import kotlinx.android.synthetic.main.fragment_review_instrument.view.*
import java.text.NumberFormat

class ReviewInstrumentFragment : Fragment() {

    private var uid : String? = ""
    private var alertDialog : AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_review_instrument, container, false)

        if (context != null) DBManager.verifyLoggedUser(context!!)

        view.homeButtonReviewInstr.setOnClickListener {
            val intentHomepage = Intent(activity, HomepageActivity::class.java)
            intentHomepage.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentHomepage)
        }

        uid = arguments!!.getString("uid", FirebaseAuth.getInstance().uid)

        alertDialog = AlertDialogInflater.inflateLoadingDialog(context!!, AlertDialogInflater.BLUE)

        fetchReviewFromDatabase(view)

        return view
    }

    private fun fetchReviewFromDatabase(view: View) {

        val formatter = NumberFormat.getInstance()
        formatter.minimumFractionDigits = 1
        formatter.maximumFractionDigits = 1

        val ref1 = FirebaseDatabase.getInstance().getReference("/instrument_users/$uid")

        ref1.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(user: DataSnapshot) {
                if (user.exists()) {
                    view.soldAdsNoTextViewReviewInstr.text = user.child("soldAdsNo").value.toString()
                    view.reviewNoTextViewReviewInstr.text = user.child("reviewNo").value.toString()
                    view.reviewAverageTextViewReviewInstr.text = formatter.format(user.child("reviewAverage").value.toString().toDouble())
                }
                else {
                    Toast.makeText(activity, "Errore durante il caricamento del profilo. Riprova.", Toast.LENGTH_LONG).show()
                }
                ref1.removeEventListener(this)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(ShowAdsInstrumentFragment::class.java.name, "ERROR on Database: ${databaseError.message}")
                Toast.makeText(activity, "Errore durante il caricamento del profilo. Riprova.", Toast.LENGTH_LONG).show()
                ref1.removeEventListener(this)
            }
        })

        val ref2 = FirebaseDatabase.getInstance().getReference("/users/$uid")

        ref2.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(user: DataSnapshot) {
                if (user.exists()) {
                    val name = user.child("firstname").value.toString() + " " + user.child("lastname").value.toString()
                    val email = user.child("email").value.toString()
                    val phone = user.child("phone").value.toString()
                    view.reviewTextViewReviewInstr.text = name
                    view.nameTextViewReviewInstr.text = name
                    view.emailTextViewReviewInstr.text = email
                    view.phoneTextViewReviewInstr.text = phone

                    var photoId = user.child("photoId").value.toString()

                    if (photoId == "") {
                        photoId = "default.jpg"
                    }
                    val ref = FirebaseStorage.getInstance().getReference("/images/users/")
                    ref.child(photoId).getBytes(4 * 1024 * 1024)
                        .addOnSuccessListener { bytes ->
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            view.photoImageViewReviewInstr.setImageBitmap(bitmap)
                            alertDialog!!.dismiss()
                        }
                        .addOnFailureListener {
                            alertDialog!!.dismiss()
                        }
                }
                else {
                    Toast.makeText(activity, "Errore durante il caricamento del profilo. Riprova.", Toast.LENGTH_LONG).show()
                    alertDialog!!.dismiss()
                }
                ref2.removeEventListener(this)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(ShowAdsInstrumentFragment::class.java.name, "ERROR on Database: ${databaseError.message}")
                alertDialog!!.dismiss()
                Toast.makeText(activity, "Errore durante il caricamento del profilo. Riprova.", Toast.LENGTH_LONG).show()
                ref2.removeEventListener(this)
            }
        })
    }
}