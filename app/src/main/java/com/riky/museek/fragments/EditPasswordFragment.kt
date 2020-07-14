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
import com.riky.museek.R
import com.riky.museek.activities.HomepageActivity
import com.riky.museek.classes.AlertDialogInflater
import com.riky.museek.classes.DBManager
import kotlinx.android.synthetic.main.fragment_edit_password.view.*
import kotlinx.android.synthetic.main.password_popup_blue.*

class EditPasswordFragment : Fragment() {

    private var alertDialog : AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_edit_password, container, false)

        if (context != null) DBManager.verifyLoggedUser(context!!)

        view.homeButtonEditPassword.setOnClickListener {
            hideKeyboard(view)
            val intentHomepage = Intent(activity, HomepageActivity::class.java)
            intentHomepage.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentHomepage)
        }

        view.updateButtonEditPassword.setOnClickListener {

            view.newPasswordEditTextEditPassword.setBackgroundResource(R.drawable.shadow_edit_text_grey_light)
            view.newPasswordConfirmEditTextEditPassword.setBackgroundResource(R.drawable.shadow_edit_text_grey_light)

            if (context != null) DBManager.verifyLoggedUser(context!!)

            val newPassword = view.newPasswordEditTextEditPassword.text.toString().trim()
            val newPasswordConfirm = view.newPasswordConfirmEditTextEditPassword.text.toString().trim()

            if (newPassword.isEmpty() || newPasswordConfirm.isEmpty()) {
                Toast.makeText(activity, "Si prega di compilare tutti i campi del form!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (newPassword != newPasswordConfirm) {
                view.newPasswordEditTextEditPassword.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
                view.newPasswordConfirmEditTextEditPassword.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
                Toast.makeText(activity, "Le password non corrispondono!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            alertDialog = AlertDialogInflater.inflatePasswordDialog(context!!, AlertDialogInflater.GREY)

            alertDialog!!.submitButtonPasswordPopup.setOnClickListener {
                alertDialog!!.passwordEditTextPasswordPopup.setBackgroundResource(R.drawable.shadow_edit_text_grey_light)
                val oldPassword = alertDialog!!.passwordEditTextPasswordPopup.text.toString().trim()
                DBManager.updateUserPassword(oldPassword, newPassword, alertDialog!!, context!!, view)
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