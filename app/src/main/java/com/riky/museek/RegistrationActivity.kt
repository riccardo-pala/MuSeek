package com.riky.museek

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_registration.*

class RegistrationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        regButtonReg.setOnClickListener {
            performRegistration()
        }
    }

    private fun performRegistration() {

        var firstname = firstnameEditTextReg.text.toString()
        var lastname = lastnameEditTextReg.text.toString()
        var email = emailEditTextReg.text.toString()
        var password = passwordEditTextReg.text.toString()
        var passwordconfirm = passwordconfirmEditTextReg.text.toString()

        if (firstname.isEmpty() || lastname.isEmpty() || email.isEmpty() || password.isEmpty() || passwordconfirm.isEmpty()) {
            Toast.makeText(this, "Per favore, compila tutti i campi del form!", Toast.LENGTH_LONG).show()
            return
        }

        if (password!=passwordconfirm) {
            Toast.makeText(this, "Errore! Le password non combaciano.", Toast.LENGTH_LONG).show()
            return
        }

        Log.d(RegistrationActivity::class.java.name, "Firstname: $firstname")
        Log.d(RegistrationActivity::class.java.name, "Lastname: $lastname")
        Log.d(RegistrationActivity::class.java.name, "Email: $email")
        Log.d(RegistrationActivity::class.java.name, "Password: $password")
        Log.d(RegistrationActivity::class.java.name, "Password Confirm: $passwordconfirm")

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                Toast.makeText(this, "Registrazione completata con successo!", Toast.LENGTH_LONG).show()
                saveUserOnDatabase(email, firstname, lastname)
                var intentLogin = Intent(this, LoginActivity::class.java)
                startActivity(intentLogin)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Errore in fase di Registrazione: ${it.message}", Toast.LENGTH_LONG).show()
                Log.d(RegistrationActivity::class.java.name, "Error on Registration: ${it.message}")
            }
    }

    private fun saveUserOnDatabase(email: String, firstname: String, lastname: String) {

        val uid = FirebaseAuth.getInstance().uid ?: ""

        val user = User(uid, email, firstname, lastname)
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d(RegistrationActivity::class.java.name, "User successfully saved on DB")
            }
            .addOnFailureListener{
                Toast.makeText(this, "Errore in fase di Registrazione: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}