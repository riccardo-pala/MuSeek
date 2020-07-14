package com.riky.museek.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
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
import com.riky.museek.classes.UserInstrument
import kotlinx.android.synthetic.main.fragment_edit_address_instrument.view.*
import kotlinx.android.synthetic.main.loading_popup_blue.*

class EditAddressInstrumentFragment : Fragment() {

    private var alertDialog : AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_edit_address_instrument, container, false)

        if (context != null) DBManager.verifyLoggedUser(context!!)

        DBManager.verifyLoggedUser(context!!)

        view.homeButtonEditAddressInstr.setOnClickListener {
            hideKeyboard(view)
            val intentHomepage = Intent(activity, HomepageActivity::class.java)
            intentHomepage.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentHomepage)
        }

        alertDialog = AlertDialogInflater.inflateLoadingDialog(context!!, AlertDialogInflater.BLUE)

        fetchMyAddressFromDatabase(view)

        view.updateButtonEditAddressInstr.setOnClickListener {

            hideKeyboard(view)

            DBManager.verifyLoggedUser(context!!)

            view.nationEditTextEditAddressInstr.setBackgroundResource(R.drawable.shadow_edit_text_grey_light)
            view.cityEditTextEditAddressInstr.setBackgroundResource(R.drawable.shadow_edit_text_grey_light)
            view.streetEditTextEditAddressInstr.setBackgroundResource(R.drawable.shadow_edit_text_grey_light)
            view.civicEditTextEditAddressInstr.setBackgroundResource(R.drawable.shadow_edit_text_grey_light)
            view.innerEditTextEditAddressInstr.setBackgroundResource(R.drawable.shadow_edit_text_grey_light)
            view.capEditTextEditAddressInstr.setBackgroundResource(R.drawable.shadow_edit_text_grey_light)

            if (context != null) DBManager.verifyLoggedUser(context!!)

            val nation = view.nationEditTextEditAddressInstr.text.toString().trim()
            val city = view.cityEditTextEditAddressInstr.text.toString().trim()
            val street = view.streetEditTextEditAddressInstr.text.toString().trim()
            val civic = view.civicEditTextEditAddressInstr.text.toString().trim()
            val inner = view.innerEditTextEditAddressInstr.text.toString().trim()
            val cap = view.capEditTextEditAddressInstr.text.toString().trim()

            var isEmptyFields = false

            if (nation.isEmpty()) {
                view.nationEditTextEditAddressInstr.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
                isEmptyFields = true
            }
            if (city.isEmpty()) {
                view.cityEditTextEditAddressInstr.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
                isEmptyFields = true
            }
            if (street.isEmpty()) {
                view.streetEditTextEditAddressInstr.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
                isEmptyFields = true
            }
            if (civic.isEmpty()) {
                view.civicEditTextEditAddressInstr.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
                isEmptyFields = true
            }
            if (cap.isEmpty() || cap.length != 5) {
                view.capEditTextEditAddressInstr.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
                isEmptyFields = true
            }
            if (isEmptyFields) {
                Toast.makeText(activity, "Si prega di compilare correttamente tutti i campi obbligatori del form!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            alertDialog!!.show()

            val user = UserInstrument(nation, city, street, civic, inner, cap)
            DBManager.updateUserAddressInstrument(user, context!!, alertDialog!!)
            fragmentManager!!.popBackStack()
            fragmentManager!!.beginTransaction().replace(R.id.fragment, MyProfileInstrumentFragment()).commit()

            return@setOnClickListener
        }

        return view
    }

    private fun fetchMyAddressFromDatabase(view: View) {

        val uid = FirebaseAuth.getInstance().uid

        val ref = FirebaseDatabase.getInstance().getReference("/instrument_users/$uid")

        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(user: DataSnapshot) {
                if (user.exists()) {
                    view.nationEditTextEditAddressInstr.setText(user.child("nation").value as String)
                    view.cityEditTextEditAddressInstr.setText(user.child("city").value as String)
                    view.streetEditTextEditAddressInstr.setText(user.child("street").value as String)
                    view.civicEditTextEditAddressInstr.setText(user.child("civic").value as String)
                    view.innerEditTextEditAddressInstr.setText(user.child("inner").value as String)
                    view.capEditTextEditAddressInstr.setText(user.child("cap").value as String)
                    alertDialog!!.dismiss()

                }
                else
                    alertDialog!!.dismiss()
                ref.removeEventListener(this)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(EditProfileFragment::class.java.name, "ERROR on Database: ${databaseError.message}")
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