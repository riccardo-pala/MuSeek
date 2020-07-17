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
import com.riky.museek.classes.AlertDialogInflater
import com.riky.museek.classes.DBManager
import com.riky.museek.classes.AdBandBand
import kotlinx.android.synthetic.main.fragment_new_band_ad_band.view.*
import java.util.*

class NewBandAdBandFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_new_band_ad_band, container, false)

        DBManager.verifyLoggedUser(context!!)

        view.homeButtonNewBandAdBand.setOnClickListener {
            hideKeyboard(view)
            val intentHomepage = Intent(activity, HomepageActivity::class.java)
            intentHomepage.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentHomepage)
        }

        view.updateButtonNewBandAdBand.setOnClickListener {

            hideKeyboard(view)

            DBManager.verifyLoggedUser(context!!)

            val uid = FirebaseAuth.getInstance().uid

            view.regionSpinnerNewBandAdBand.setBackgroundResource(R.drawable.shadow_spinner)
            view.musicianSpinnerNewBandAdBand.setBackgroundResource(R.drawable.shadow_spinner)
            view.descriptionEditTextNewBandAdBand.setBackgroundResource(R.drawable.shadow_edit_text_grey_light)

            val alertDialog = AlertDialogInflater.inflateLoadingDialog(context!!, AlertDialogInflater.RED)

            if (context != null) DBManager.verifyLoggedUser(context!!)

            val region = view.regionSpinnerNewBandAdBand.selectedItemPosition
            val musician = view.musicianSpinnerNewBandAdBand.selectedItemPosition
            val description = view.descriptionEditTextNewBandAdBand.text.toString().trim()

            var isEmptyFields = false

            if (region == 0) {
                view.regionSpinnerNewBandAdBand.setBackgroundResource(R.drawable.shadow_spinner_error)
                isEmptyFields = true
            }
            if (musician == 0) {
                view.musicianSpinnerNewBandAdBand.setBackgroundResource(R.drawable.shadow_spinner_error)
                isEmptyFields = true
            }
            if (description.isEmpty() || description.length > 150) {
                view.descriptionEditTextNewBandAdBand.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
                isEmptyFields = true
            }
            if (isEmptyFields) {
                Toast.makeText(activity, "Si prega di compilare correttamente tutti i campi del form!", Toast.LENGTH_LONG).show()
                alertDialog.dismiss()
                return@setOnClickListener
            }

            val aid = "band-band-ad-" + UUID.randomUUID()
            val ad = AdBandBand(aid, region, musician, description, uid!!)

            performNewAd(ad, context!!, alertDialog)

            fragmentManager!!.popBackStack()
            fragmentManager!!.beginTransaction().replace(R.id.fragment, BandFragment()).commit()

            return@setOnClickListener
        }

        return view
    }

    fun performNewAd(ad: AdBandBand, context: Context, alertDialog: AlertDialog) {

        val ref = DBManager.database.getReference("/band_band_ads/${ad.aid}")

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

    private fun hideKeyboard(view: View) {
        try {
            val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}