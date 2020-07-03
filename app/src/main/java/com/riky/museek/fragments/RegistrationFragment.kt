package com.riky.museek.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.riky.museek.R
import com.riky.museek.classes.DBManager
import com.riky.museek.classes.User
import kotlinx.android.synthetic.main.fragment_registration.*
import kotlinx.android.synthetic.main.fragment_registration.view.*

class RegistrationFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_registration, container, false)

        view.regButtonReg.setOnClickListener {
            performRegistration()
        }

        return view
    }

    private fun performRegistration() {

        val firstname = firstnameEditTextReg.text.toString()
        val lastname = lastnameEditTextReg.text.toString()
        val email = emailEditTextReg.text.toString()
        val password = passwordEditTextReg.text.toString()
        val passwordconfirm = passwordconfirmEditTextReg.text.toString()

        if (firstname.isEmpty() || lastname.isEmpty() || email.isEmpty() || password.isEmpty() || passwordconfirm.isEmpty()) {
            Toast.makeText(activity, "Per favore, compila tutti i campi del form!", Toast.LENGTH_LONG).show()
            return
        }

        if (password!=passwordconfirm) {
            Toast.makeText(activity, "Errore! Le password non combaciano.", Toast.LENGTH_LONG).show()
            return
        }

        Log.d(RegistrationFragment::class.java.name, "Firstname: $firstname")
        Log.d(RegistrationFragment::class.java.name, "Lastname: $lastname")
        Log.d(RegistrationFragment::class.java.name, "Email: $email")
        Log.d(RegistrationFragment::class.java.name, "Password: $password")
        Log.d(RegistrationFragment::class.java.name, "Password Confirm: $passwordconfirm")

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                Toast.makeText(activity, "Registrazione completata con successo!", Toast.LENGTH_LONG).show()
                DBManager.saveUserOnDatabase(email, firstname, lastname)
                fragmentManager!!.beginTransaction().replace(
                    R.id.fragment,
                    LoginFragment()
                ).addToBackStack(null).commit()
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Errore in fase di Registrazione: ${it.message}", Toast.LENGTH_LONG).show()
                Log.d(RegistrationFragment::class.java.name, "Error on Registration: ${it.message}")
            }
    }
}