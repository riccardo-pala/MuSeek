package com.riky.museek.classes

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.riky.museek.R
import com.riky.museek.activities.MainActivity
import com.riky.museek.fragments.*
import kotlinx.android.synthetic.main.fragment_edit_password.view.*
import kotlinx.android.synthetic.main.loading_popup_blue.*
import kotlinx.android.synthetic.main.password_popup_blue.*
import java.time.LocalDateTime

class DBManager {

    companion object {

        val storage = FirebaseStorage.getInstance()
        val database = FirebaseDatabase.getInstance()


        /*----------------------- VERIFY -----------------------------*/

        fun verifyLoggedUser(context: Context) {
            val uid = FirebaseAuth.getInstance().uid ?: ""

            if (uid == null) {
                FirebaseAuth.getInstance().signOut()
                val intentMain = Intent(context, MainActivity::class.java)
                intentMain.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(context, intentMain, null)
            }
        }

        fun verifyAdUser(aid: String, uid: String, context: Context) {

            val ref = database.getReference("/instrument_ads/$aid")

            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (!dataSnapshot.exists() || dataSnapshot.child("uid").value.toString() != uid) {
                        Toast.makeText(context, "Errore durante il caricamento dell'annuncio. Riprova.", Toast.LENGTH_LONG).show()
                        val activity = context as AppCompatActivity
                        activity.supportFragmentManager.popBackStackImmediate()
                        activity.supportFragmentManager.beginTransaction().replace(R.id.fragment, MyAdsInstrumentFragment()).commit()
                        ref.removeEventListener(this)
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d(DBManager::class.java.name, "ERROR on Database: ${databaseError.message}")
                    Toast.makeText(context, "Errore durante il caricamento dell'annuncio. Riprova.", Toast.LENGTH_LONG).show()
                    val activity = context as AppCompatActivity
                    activity.supportFragmentManager.popBackStackImmediate()
                    activity.supportFragmentManager.beginTransaction().replace(R.id.fragment, MyAdsInstrumentFragment()).commit()
                    ref.removeEventListener(this)
                }
            })
        }

        fun verifyInstrumentUser(context: Context, alertDialog: AlertDialog, aid: String?) {

            val uid = FirebaseAuth.getInstance().uid ?: ""

            val ref = database.getReference("/instrument_users/$uid")

            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        alertDialog.dismiss()
                        val activity = context as AppCompatActivity
                        if (aid != null) {
                            performTransaction(aid, context)
                            activity.supportFragmentManager.beginTransaction().replace(R.id.fragment, InstrumentFragment()).addToBackStack(null).commit()
                        }
                        else {
                            activity.supportFragmentManager.beginTransaction().replace(R.id.fragment, NewAdInstrumentFragment()).addToBackStack(null).commit()
                        }
                    }
                    else {
                        alertDialog.dismiss()
                        Toast.makeText(context, "E' necessario completare il profilo.", Toast.LENGTH_LONG).show()
                        val activity = context as AppCompatActivity
                        activity.supportFragmentManager.beginTransaction().replace(R.id.fragment, EditAddressInstrumentFragment()).addToBackStack(null).commit()
                    }
                    ref.removeEventListener(this)
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d(DBManager::class.java.name, "ERROR on Database: ${databaseError.message}")
                    alertDialog.dismiss()
                    Toast.makeText(context, "Si è verificato un errore durante l'autenticazione. Riprova.", Toast.LENGTH_LONG).show()
                    ref.removeEventListener(this)
                }
            })
        }

        /*----------------------- UPDATE -----------------------------*/

        fun updateUserEmail(email:String, password: String, newEmail: String, alertDialog: AlertDialog, context: Context, view: View) {

            val credential = EmailAuthProvider.getCredential(email, password)
            val currentUser = FirebaseAuth.getInstance().currentUser

            alertDialog.dismiss()

            val alertDialog2 = AlertDialogInflater.inflateLoadingDialog(context, AlertDialogInflater.GREY)

            currentUser!!.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        //Log.d(DBManager::class.java.name, "User re-authenticated.")
                        currentUser.updateEmail(newEmail)
                            .addOnCompleteListener { task2 ->
                                if (task2.isSuccessful) {
                                    //Log.d(DBManager::class.java.name, "User email address updated.")
                                    val uid = FirebaseAuth.getInstance().uid ?: ""
                                    val ref = database.getReference("/users/$uid")
                                    ref.child("email").setValue(newEmail)
                                        .addOnSuccessListener {
                                            //Log.d(DBManager::class.java.name, "Email successfully updated on DB")
                                            Toast.makeText(context, "L'email è stata aggiornata con successo!", Toast.LENGTH_LONG).show()
                                            val newCredential = EmailAuthProvider.getCredential(newEmail, password)
                                            currentUser.reauthenticate(newCredential)
                                            alertDialog2.dismiss()
                                            val activity = context as AppCompatActivity
                                            activity.supportFragmentManager.popBackStack()
                                            activity.supportFragmentManager.beginTransaction().replace(R.id.fragment, MyProfileFragment()).commit()
                                        }
                                }
                            }
                            .addOnFailureListener {
                                alertDialog2.dismiss()
                                //Log.d(DBManager::class.java.name, "Problem updating email.")
                                Toast.makeText(context, "Errore: ${it.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                    else {
                        alertDialog2.dismiss()
                        alertDialog.passwordEditTextPasswordPopup.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
                        alertDialog.show()
                        Log.d(DBManager::class.java.name, "Wrong password.")
                        Toast.makeText(context, "Password errata!", Toast.LENGTH_LONG).show()
                    }
                }
        }

        fun updateUserPassword(password: String, newPassword: String, alertDialog: AlertDialog, context: Context, view: View) {

            val currentUser = FirebaseAuth.getInstance().currentUser
            val email = currentUser!!.email ?: ""
            val credential = EmailAuthProvider.getCredential(email, password)

            alertDialog.dismiss()

            val alertDialog2 = AlertDialogInflater.inflateLoadingDialog(context, AlertDialogInflater.GREY)

            currentUser.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        if (password == newPassword) {
                            alertDialog2.dismiss()
                            view.newPasswordEditTextEditPassword.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
                            view.newPasswordConfirmEditTextEditPassword.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
                            Toast.makeText(context, "Si prega di inserire una password diversa dalla precedente!", Toast.LENGTH_LONG).show()
                            return@addOnCompleteListener
                        }
                        //Log.d(DBManager::class.java.name, "User re-authenticated.")
                        currentUser.updatePassword(newPassword)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    //Log.d(DBManager::class.java.name, "User password updated.")
                                    Toast.makeText(context, "La password è stata aggiornata con successo!", Toast.LENGTH_LONG).show()
                                    val newCredential = EmailAuthProvider.getCredential(email, newPassword)
                                    currentUser.reauthenticate(newCredential)
                                    alertDialog2.dismiss()
                                    val activity = context as AppCompatActivity
                                    activity.supportFragmentManager.popBackStack()
                                    activity.supportFragmentManager.beginTransaction().replace(R.id.fragment, MyProfileFragment()).commit()
                                }
                            }
                            .addOnFailureListener {
                                alertDialog2.dismiss()
                                //Log.d(DBManager::class.java.name, "Problem updating password.")
                                Toast.makeText(context, "Errore: ${it.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                    else {
                        alertDialog2.dismiss()
                        alertDialog.passwordEditTextPasswordPopup.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
                        alertDialog.show()
                        Log.d(DBManager::class.java.name, "Wrong password.")
                        Toast.makeText(context, "Password errata!", Toast.LENGTH_LONG).show()
                    }
                }
        }

        fun updateUserAddressInstrument(user: UserInstrument, context: Context, alertDialog: AlertDialog) {

            val uid = FirebaseAuth.getInstance().uid ?: ""

            val ref = database.getReference("/instrument_users/$uid")

            ref.child("nation").setValue(user.nation)
            ref.child("city").setValue(user.city)
            ref.child("street").setValue(user.street)
            ref.child("civic").setValue(user.civic)
            ref.child("inner").setValue(user.inner)
            ref.child("cap").setValue(user.cap)

            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(userSnapshot: DataSnapshot) {
                    if (!userSnapshot.hasChild("adsNo")) {
                        ref.child("adsNo").setValue(0)
                    }
                    if (!userSnapshot.hasChild("reviewNo")) {
                        ref.child("reviewNo").setValue(0)
                    }
                    if (!userSnapshot.hasChild("reviewAverage")) {
                        ref.child("reviewAverage").setValue(0f)
                    }
                    alertDialog.dismiss()
                    Toast.makeText(context, "Profilo modificato con successo!.", Toast.LENGTH_LONG).show()
                    ref.removeEventListener(this)
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d(DBManager::class.java.name, "ERROR on Database: ${databaseError.message}")
                    Toast.makeText(context, "Si è verificato un errore durante la modifica del profilo! Riprova.", Toast.LENGTH_LONG).show()
                    alertDialog.dismiss()
                    ref.removeEventListener(this)
                }
            })
        }

        /*----------------------- SAVE/DELETE -----------------------------*/

        fun saveUserOnDatabase(email: String, firstname: String, lastname: String) {

            val uid = FirebaseAuth.getInstance().uid ?: ""

            val user = User(uid, email, firstname, lastname, "", "")
            val ref = database.getReference("/users/$uid")

            ref.setValue(user)
                .addOnSuccessListener {
                    Log.d(DBManager::class.java.name, "User successfully saved on DB")
                }
                .addOnFailureListener {
                    Log.d(DBManager::class.java.name, "Error on Database: ${it.message}")
                }
        }

        fun saveAdOnDatabase(ad: Ad, context: Context) {

            val ref = database.getReference("/instrument_ads/${ad.aid}")

            ref.setValue(ad)
                .addOnSuccessListener {
                    Log.d(DBManager::class.java.name, "Ad successfully saved on DB")
                    Toast.makeText(context, "Annuncio aggiunto con successo!", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener{
                    Log.d(DBManager::class.java.name, "Error on Database: ${it.message}")
                    Toast.makeText(context, "Errore durante la creazione dell'annuncio. Riprova", Toast.LENGTH_LONG).show()
                }
        }

        fun deleteAdOnDatabase(aid: String, photoId: String) {

            database.getReference("/instrument_ads/$aid").removeValue()
                .addOnSuccessListener {
                    Log.d(DBManager::class.java.name, "Ad successfully removed from DB")
                }
                .addOnFailureListener{
                    Log.d(DBManager::class.java.name, "Error on Database: ${it.message}")
                }

            storage.getReference("/images/instrument_ads/$photoId").delete()
                .addOnSuccessListener {
                    Log.d(DBManager::class.java.name, "Pic successfully removed from Storage")
                }
                .addOnFailureListener{
                    Log.d(DBManager::class.java.name, "Error on Database: ${it.message}")
                }


        }

        /*----------------------- UPLOAD -----------------------------*/

        fun uploadPickedPhotoOnStorage(pickedPhotoUri: Uri, photoPath: String, context: Context) {

            val ref = storage.getReference("/images/$photoPath")

            ref.putFile(pickedPhotoUri)
                .addOnSuccessListener {
                    Log.d(DBManager::class.java.name, "Image succesfully loaded on Firebase Storage")
                }
                .addOnFailureListener {
                    Log.d(DBManager::class.java.name, "Error while uploading file on Firebase: ${it.message}")
                    Toast.makeText(context, "Errore durante l'aggiornamento dell'annuncio. Riprova", Toast.LENGTH_LONG).show()
                }
        }

        /*----------------------- PERFORM -----------------------------*/

        fun performTransaction(aid: String, context: Context) {

            val buyeruid = FirebaseAuth.getInstance().uid

            if (buyeruid == null) {
                FirebaseAuth.getInstance().signOut()
                val intentMain = Intent(context, MainActivity::class.java)
                intentMain.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(context, intentMain, null)
            }

            val aidRef = database.getReference("/instrument_ads/$aid")

            aidRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val selleruid = dataSnapshot.child("uid").value as String
                        if (selleruid != buyeruid) {
                            val ad = PurchasedAd(
                                aid,
                                dataSnapshot.child("brand").value as String,
                                dataSnapshot.child("model").value as String,
                                dataSnapshot.child("price").value.toString().toDouble(),
                                dataSnapshot.child("category").value.toString().toInt(),
                                dataSnapshot.child("condition").value.toString().toInt(),
                                dataSnapshot.child("photoId").value as String,
                                selleruid,
                                buyeruid!!,
                                LocalDateTime.now().toString())
                            val ref = database.getReference("/instrument_purchased_ads/$aid")
                            ref.setValue(ad)
                                .addOnSuccessListener {
                                    Log.d(DBManager::class.java.name, "Ad successfully saved on DB")
                                }
                                .addOnFailureListener{
                                    Log.d(DBManager::class.java.name, "Error on Database: ${it.message}")
                                }
                            aidRef.removeValue()
                                .addOnSuccessListener {
                                    Log.d(DBManager::class.java.name, "Ad successfully removed from DB")
                                }
                                .addOnFailureListener{
                                    Log.d(DBManager::class.java.name, "Error on Database: ${it.message}")
                                }
                            Toast.makeText(context, "Acquisto Completato!", Toast.LENGTH_LONG).show()
                        }
                        else {
                            Toast.makeText(context, "Qualcosa è andato storto, riprova!", Toast.LENGTH_LONG).show()
                            Log.d(DBManager::class.java.name, "Annuncio di proprietà dell'acquirente!")
                        }
                    }
                    else {
                        Toast.makeText(context, "Qualcosa è andato storto, riprova!", Toast.LENGTH_LONG).show()
                        Log.d(DBManager::class.java.name, "Annuncio non esistente!")
                    }
                    aidRef.removeEventListener(this)
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d(ShowAdsInstrumentFragment::class.java.name, "ERROR on Database: ${databaseError.message}")
                    aidRef.removeEventListener(this)
                }
            })

        }

        /*----------------------- VARIE -----------------------------*/

        fun getCategoryStringByType(type: String) : Array<Int> {

            when (type) {
                "F" -> {
                    return arrayOf(6, 7, 9, 12)
                }
                "C" -> {
                    return arrayOf(1, 2, 3, 4, 13)
                }
                "T" -> {
                    return arrayOf(8, 10, 11)
                }
                "A" -> {
                    return arrayOf(5, 14, 15)
                }
            }
            return arrayOf(0)
        }

        fun getCategoryById(id: Int) : String {

            when(id) {
                1 -> return "Basso Elettrico"
                2 -> return "Chitarra Acustica"
                3 -> return "Chitarra Classica"
                4 -> return "Chitarra Elettrica"
                5 -> return "Contrabbasso"
                6 -> return "Flauto"
                7 -> return "Oboe"
                8 -> return "Pianoforte"
                9 -> return "Sassofono"
                10 -> return "Synth"
                11 -> return "Tastiera"
                12 -> return "Tromba"
                13 -> return "Ukulele"
                14 -> return "Violino"
                15 -> return "Violoncello"
            }
            return ""
        }
    }
}