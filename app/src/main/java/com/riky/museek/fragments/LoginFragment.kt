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
import com.riky.museek.activities.HomepageActivity
import com.riky.museek.R
import com.riky.museek.classes.AlertDialogInflater
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*

class LoginFragment : Fragment() {

    var alertDialog : AlertDialog ?= null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_login, container, false)

        view.logButtonLog.setOnClickListener {
            hideKeyboard(view)
            alertDialog = AlertDialogInflater.inflateLoadingDialog(context!!, AlertDialogInflater.GREY)
            performLogin()
        }

        return view
    }

    private fun performLogin() {

        val email = emailEditTextLog.text.toString().trim()
        val password = passwordEditTextLog.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            alertDialog!!.dismiss()
            Toast.makeText(activity, "Per favore, compila tutti i campi del form!", Toast.LENGTH_LONG).show()
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                alertDialog!!.dismiss()
                Toast.makeText(activity, "Benvenuto!", Toast.LENGTH_LONG).show()
                val intentHomepage = Intent(activity, HomepageActivity::class.java)
                intentHomepage.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intentHomepage)
            }
            .addOnFailureListener {
                alertDialog!!.dismiss()
                Toast.makeText(activity, "Errore in fase di Login: ${it.message}", Toast.LENGTH_LONG).show()
                Log.d(LoginFragment::class.java.name, "Error on Login: ${it.message}")
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