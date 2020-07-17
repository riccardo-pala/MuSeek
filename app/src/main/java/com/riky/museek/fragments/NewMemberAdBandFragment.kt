package com.riky.museek.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.riky.museek.R
import com.riky.museek.activities.HomepageActivity
import com.riky.museek.classes.AdMemberBand
import com.riky.museek.classes.AlertDialogInflater
import com.riky.museek.classes.DBManager
import kotlinx.android.synthetic.main.fragment_new_member_ad_band.view.*
import java.util.*

class NewMemberAdBandFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_new_member_ad_band, container, false)

        DBManager.verifyLoggedUser(context!!)

        view.homeButtonNewMemberAdBand.setOnClickListener {
            hideKeyboard(view)
            val intentHomepage = Intent(activity, HomepageActivity::class.java)
            intentHomepage.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentHomepage)
        }

        view.updateButtonNewMemberAdBand.setOnClickListener {

            hideKeyboard(view)

            DBManager.verifyLoggedUser(context!!)

            val uid = FirebaseAuth.getInstance().uid

            view.bandNameEditTextNewMemberAdBand.setBackgroundResource(R.drawable.shadow_edit_text_grey_light)
            view.regionSpinnerNewMemberAdBand.setBackgroundResource(R.drawable.shadow_spinner)
            view.musicianSpinnerNewMemberAdBand.setBackgroundResource(R.drawable.shadow_spinner)
            view.descriptionEditTextNewMemberAdBand.setBackgroundResource(R.drawable.shadow_edit_text_grey_light)

            val alertDialog = AlertDialogInflater.inflateLoadingDialog(context!!, AlertDialogInflater.RED)

            if (context != null) DBManager.verifyLoggedUser(context!!)

            val bandName = view.bandNameEditTextNewMemberAdBand.text.toString().trim()
            val region = view.regionSpinnerNewMemberAdBand.selectedItemPosition
            val musician = view.musicianSpinnerNewMemberAdBand.selectedItemPosition
            val description = view.descriptionEditTextNewMemberAdBand.text.toString().trim()

            var isEmptyFields = false


            if (bandName.isEmpty()) {
                view.bandNameEditTextNewMemberAdBand.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
                isEmptyFields = true
            }
            if (region == 0) {
                view.regionSpinnerNewMemberAdBand.setBackgroundResource(R.drawable.shadow_spinner_error)
                isEmptyFields = true
            }
            if (musician == 0) {
                view.musicianSpinnerNewMemberAdBand.setBackgroundResource(R.drawable.shadow_spinner_error)
                isEmptyFields = true
            }
            if (description.isEmpty() || description.length > 80) {
                view.descriptionEditTextNewMemberAdBand.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
                isEmptyFields = true
            }
            if (isEmptyFields) {
                Toast.makeText(activity, "Si prega di compilare correttamente tutti i campi del form!", Toast.LENGTH_LONG).show()
                alertDialog.dismiss()
                return@setOnClickListener
            }

            val aid = "band-member-ad-" + UUID.randomUUID()
            val ad = AdMemberBand(aid, bandName, region, musician, description, uid!!)

            performNewAd(ad, context!!, alertDialog)

            fragmentManager!!.popBackStack()
            fragmentManager!!.beginTransaction().replace(R.id.fragment, BandFragment()).commit()

            return@setOnClickListener
        }

        return view
    }

    fun performNewAd(ad: AdMemberBand, context: Context, alertDialog: AlertDialog) {

        val ref = DBManager.database.getReference("/band_member_ads/${ad.aid}")

        ref.setValue(ad)
            .addOnSuccessListener {
                alertDialog.dismiss()
                Toast.makeText(context, "Annuncio inserito con successo!.", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                alertDialog.dismiss()
                Toast.makeText(context, "Si è verificato un errore: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    /*---------------------- DA USARE IN EDITMemberADBAND ------------------------

    private fun fetchMyMemberAdFromDatabase(view: View) {

        val uid = FirebaseAuth.getInstance().uid

        val ref = FirebaseDatabase.getInstance().getReference("/band_users/$uid")

        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(user: DataSnapshot) {
                if (user.exists()) {
                    view.regionSpinnerEditBandAdBand.setSelection(user.child("region").value.toString().toInt())
                    view.musicianSpinnerEditBandAdBandBand.setSelection(user.child("musician").value.toString().toInt())
                    view.descriptionEditTextEditBandAdBandBand.setText(user.child("description").value.toString())
                }
                alertDialog!!.dismiss()
                ref.removeEventListener(this)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(EditBandAdBandBandFragment::class.java.name, "ERROR on Database: ${databaseError.message}")
                alertDialog!!.dismiss()
                ref.removeEventListener(this)
            }
        })
    }

     */

    private fun hideKeyboard(view: View) {
        try {
            val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}