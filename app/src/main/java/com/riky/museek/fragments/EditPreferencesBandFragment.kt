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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.riky.museek.R
import com.riky.museek.activities.HomepageActivity
import com.riky.museek.classes.AlertDialogInflater
import com.riky.museek.classes.DBManager
import com.riky.museek.classes.UserBand
import kotlinx.android.synthetic.main.fragment_edit_ad_instrument.*
import kotlinx.android.synthetic.main.fragment_edit_preferences_band.view.*

class EditPreferencesBandFragment : Fragment() {

    private var alertDialog : AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_edit_preferences_band, container, false)

        if (context != null) DBManager.verifyLoggedUser(context!!)

        DBManager.verifyLoggedUser(context!!)

        view.homeButtonEditPreferencesBand.setOnClickListener {
            hideKeyboard(view)
            val intentHomepage = Intent(activity, HomepageActivity::class.java)
            intentHomepage.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentHomepage)
        }

        alertDialog = AlertDialogInflater.inflateLoadingDialog(context!!, AlertDialogInflater.RED)

        fetchMyPreferencesFromDatabase(view)

        view.updateButtonEditPreferencesBand.setOnClickListener {

            hideKeyboard(view)

            DBManager.verifyLoggedUser(context!!)

            view.regionSpinnerEditPreferencesBand.setBackgroundResource(R.drawable.shadow_spinner)
            view.musicianSpinnerEditPreferencesBand.setBackgroundResource(R.drawable.shadow_spinner)
            view.descriptionEditTextEditPreferencesBand.setBackgroundResource(R.drawable.shadow_edit_text_grey_light)

            alertDialog!!.show()

            if (context != null) DBManager.verifyLoggedUser(context!!)

            val region = view.regionSpinnerEditPreferencesBand.selectedItemPosition
            val musician = view.musicianSpinnerEditPreferencesBand.selectedItemPosition
            val description = view.descriptionEditTextEditPreferencesBand.text.toString().trim()

            var isEmptyFields = false

            if (region == 0) {
                view.regionSpinnerEditPreferencesBand.setBackgroundResource(R.drawable.shadow_spinner_error)
                isEmptyFields = true
            }
            if (musician == 0) {
                view.musicianSpinnerEditPreferencesBand.setBackgroundResource(R.drawable.shadow_spinner_error)
                isEmptyFields = true
            }
            if (description.isEmpty()) {
                view.descriptionEditTextEditPreferencesBand.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
                isEmptyFields = true
            }
            if (isEmptyFields) {
                Toast.makeText(activity, "Si prega di compilare correttamente tutti i campi del form!", Toast.LENGTH_LONG).show()
                alertDialog!!.dismiss()
                return@setOnClickListener
            }

            val user = UserBand(region, musician, description)
            DBManager.updateUserPreferencesBand(user, context!!, alertDialog!!)
            fragmentManager!!.popBackStack()
            fragmentManager!!.beginTransaction().replace(R.id.fragment, MyProfileBandFragment()).commit()

            return@setOnClickListener
        }

        return view
    }

    private fun fetchMyPreferencesFromDatabase(view: View) {

        val uid = FirebaseAuth.getInstance().uid

        val ref = FirebaseDatabase.getInstance().getReference("/band_users/$uid")

        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(user: DataSnapshot) {
                if (user.exists()) {
                    view.regionSpinnerEditPreferencesBand.setSelection(user.child("region").value.toString().toInt())
                    view.musicianSpinnerEditPreferencesBand.setSelection(user.child("musician").value.toString().toInt())
                    view.descriptionEditTextEditPreferencesBand.setText(user.child("description").value.toString())
                }
                alertDialog!!.dismiss()
                ref.removeEventListener(this)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(EditPreferencesBandFragment::class.java.name, "ERROR on Database: ${databaseError.message}")
                alertDialog!!.dismiss()
                ref.removeEventListener(this)
            }
        })
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