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
import kotlinx.android.synthetic.main.fragment_edit_email.view.*
import kotlinx.android.synthetic.main.password_popup_blue.*

class EditEmailFragment : Fragment () {

    private var alertDialog : AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_edit_email, container, false)

        if (context != null) DBManager.verifyLoggedUser(context!!)

        view.homeButtonEditEmail.setOnClickListener {
            hideKeyboard(view)
            val intentHomepage = Intent(activity, HomepageActivity::class.java)
            intentHomepage.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentHomepage)
        }

        view.updateButtonEditEmail.setOnClickListener {

            hideKeyboard(view)

            view.oldEmailEditTextEditEmail.setBackgroundResource(R.drawable.shadow_edit_text_grey_light)
            view.newEmailEditTextEditEmail.setBackgroundResource(R.drawable.shadow_edit_text_grey_light)
            view.newEmailConfirmEditTextEditEmail.setBackgroundResource(R.drawable.shadow_edit_text_grey_light)

            if (context != null) DBManager.verifyLoggedUser(context!!)

            val oldEmail = view.oldEmailEditTextEditEmail.text.toString().trim()
            val newEmail = view.newEmailEditTextEditEmail.text.toString().trim()
            val newEmailConfirm = view.newEmailConfirmEditTextEditEmail.text.toString().trim()

            if (oldEmail.isEmpty() || newEmail.isEmpty() || newEmailConfirm.isEmpty()) {
                Toast.makeText(activity, "Si prega di compilare tutti i campi del form!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (oldEmail != FirebaseAuth.getInstance().currentUser!!.email) {
                view.oldEmailEditTextEditEmail.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
                Toast.makeText(activity, "Email errata!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (newEmail != newEmailConfirm) {
                view.newEmailEditTextEditEmail.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
                view.newEmailConfirmEditTextEditEmail.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
                Toast.makeText(activity, "Le email non corrispondono!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (oldEmail == newEmail) {
                view.newEmailEditTextEditEmail.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
                view.newEmailConfirmEditTextEditEmail.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
                Toast.makeText(context, "Si prega di inserire un'email diversa dalla precedente!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            alertDialog = AlertDialogInflater.inflatePasswordDialog(context!!, AlertDialogInflater.GREY)

            alertDialog!!.submitButtonPasswordPopup.setOnClickListener {
                hideKeyboard(view)
                alertDialog!!.passwordEditTextPasswordPopup.setBackgroundResource(R.drawable.shadow_edit_text_grey_light)
                val password = alertDialog!!.passwordEditTextPasswordPopup.text.toString().trim()
                DBManager.updateUserEmail(oldEmail, password, newEmail, alertDialog!!, context!!, view)
            }
        }

        return view
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