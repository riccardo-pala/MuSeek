package com.riky.museek.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.riky.museek.R
import com.riky.museek.activities.HomepageActivity
import com.riky.museek.classes.AdBandBand
import com.riky.museek.classes.AlertDialogInflater
import com.riky.museek.classes.DBManager
import kotlinx.android.synthetic.main.fragment_edit_band_ad_band.*
import kotlinx.android.synthetic.main.fragment_edit_band_ad_band.view.*

class EditBandAdBandFragment : Fragment() {

    private var aid : String? = null
    private var alertDialog : AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_edit_band_ad_band, container, false)

        if (context != null) DBManager.verifyLoggedUser(context!!)

        try {
            requireArguments()
        }
        catch (e : IllegalStateException) {
            Toast.makeText(activity, "Errore durante il caricamento dell'annuncio. Riprova.", Toast.LENGTH_LONG).show()
            fragmentManager!!.popBackStack()
            fragmentManager!!.beginTransaction().replace(R.id.fragment, MyBandAdsBandFragment()).commit()
        }

        setListeners(view)

        alertDialog = AlertDialogInflater.inflateLoadingDialog(context!!, AlertDialogInflater.RED)

        aid = arguments!!.getString("aid", "")

        view.regionSpinnerEditBandAdBand.setSelection(arguments!!.getInt("region", 0))
        view.musicianSpinnerEditBandAdBand.setSelection(arguments!!.getInt("musician", 0))
        view.descriptionEditTextEditBandAdBand.setText(arguments!!.getString("description", ""))

        alertDialog!!.dismiss()

        view.updateButtonEditBandAdBand.setOnClickListener {

            hideKeyboard(view)

            view.regionSpinnerEditBandAdBand.setBackgroundResource(R.drawable.shadow_spinner)
            view.musicianSpinnerEditBandAdBand.setBackgroundResource(R.drawable.shadow_spinner)
            view.descriptionEditTextEditBandAdBand.setBackgroundResource(R.drawable.shadow_edit_text_grey_light)

            performUpdateAd()
        }

        return view
    }

    private fun performUpdateAd() {

        val uid = FirebaseAuth.getInstance().uid

        DBManager.verifyLoggedUser(context!!)

        val region = regionSpinnerEditBandAdBand.selectedItemPosition
        val musician = musicianSpinnerEditBandAdBand.selectedItemPosition
        val description = descriptionEditTextEditBandAdBand.text.toString().trim()

        var isEmptyFields = false

        if (region == 0) {
            regionSpinnerEditBandAdBand.setBackgroundResource(R.drawable.shadow_spinner_error)
            isEmptyFields = true
        }
        if (musician == 0) {
            musicianSpinnerEditBandAdBand.setBackgroundResource(R.drawable.shadow_spinner_error)
            isEmptyFields = true
        }
        if (description.isEmpty() || description.length > 150) {
            descriptionEditTextEditBandAdBand.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
            isEmptyFields = true
        }

        if (isEmptyFields) {
            Toast.makeText(context, "Si prega di compilare correttamente tutti i campi del form.", Toast.LENGTH_LONG).show()
            return
        }

        val ad = AdBandBand(aid!!, region, musician, description,  uid!!)

        val ref = DBManager.database.getReference("/band_band_ads/${ad.aid}")

        ref.setValue(ad)
            .addOnSuccessListener {
                Log.d(DBManager::class.java.name, "Ad successfully saved on DB")
                Toast.makeText(context, "Annuncio aggiornato con successo!", Toast.LENGTH_LONG).show()
                val fragment = MyBandAdsBandFragment()
                val args = Bundle()
                args.putBoolean("loaded", true)
                fragment.arguments = args
                fragmentManager!!.popBackStack()
                fragmentManager!!.beginTransaction().replace(R.id.fragment, fragment).commit()
            }
            .addOnFailureListener{
                Log.d(DBManager::class.java.name, "Error on Database: ${it.message}")
                Toast.makeText(context, "Errore durante l'aggiornamento' dell'annuncio. Riprova", Toast.LENGTH_LONG).show()
            }
    }

    private fun setListeners(view: View) {

        view.homeButtonEditBandAdBand.setOnClickListener {
            hideKeyboard(view)
            val intentHomepage = Intent(activity, HomepageActivity::class.java)
            intentHomepage.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentHomepage)
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