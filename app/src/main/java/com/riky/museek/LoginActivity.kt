package com.riky.museek

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        logButtonLog.setOnClickListener {
            performLogin()
        }
    }

    private fun performLogin() {

        var email = emailEditTextLog.text.toString()
        var password = passwordEditTextLog.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Per favore, compila tutti i campi del form!", Toast.LENGTH_LONG).show()
            return
        }

        Log.d(LoginActivity::class.java.name, "Email: $email")
        Log.d(LoginActivity::class.java.name, "Password: $password")

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                Toast.makeText(this, "Benvenuto!", Toast.LENGTH_LONG).show()
                var intentHomepage = Intent(this, HomepageActivity::class.java)
                intentHomepage.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intentHomepage)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Errore in fase di Login: ${it.message}", Toast.LENGTH_LONG).show()
                Log.d(LoginActivity::class.java.name, "Error on Login: ${it.message}")
            }
    }
}