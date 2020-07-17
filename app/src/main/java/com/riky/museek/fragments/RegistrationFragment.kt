package com.riky.museek.fragments

import android.content.Context
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
import com.riky.museek.classes.DBManager
import kotlinx.android.synthetic.main.fragment_registration.*
import kotlinx.android.synthetic.main.fragment_registration.view.*

class RegistrationFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_registration, container, false)

        view.regButtonReg.setOnClickListener {
            hideKeyboard(view)
            performRegistration()
        }

        return view
    }

    private fun performRegistration() {

        val firstname = firstnameEditTextReg.text.toString().trim()
        val lastname = lastnameEditTextReg.text.toString().trim()
        val phone = phoneEditTextReg.text.toString().trim()
        val email = emailEditTextReg.text.toString().trim()
        val password = passwordEditTextReg.text.toString().trim()
        val passwordconfirm = passwordconfirmEditTextReg.text.toString().trim()

        if (firstname.isEmpty() || lastname.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || passwordconfirm.isEmpty()) {
            Toast.makeText(activity, "Per favore, compila tutti i campi del form!", Toast.LENGTH_LONG).show()
            return
        }

        if (password!=passwordconfirm) {
            Toast.makeText(activity, "Errore! Le password non combaciano.", Toast.LENGTH_LONG).show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                Toast.makeText(activity, "Registrazione completata con successo!", Toast.LENGTH_LONG).show()
                DBManager.saveUserOnDatabase(email, firstname, lastname, phone)
                fragmentManager!!.beginTransaction().replace(R.id.fragment, LoginFragment()).commit()
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Errore in fase di Registrazione: ${it.message}", Toast.LENGTH_LONG).show()
                Log.d(RegistrationFragment::class.java.name, "Error on Registration: ${it.message}")
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