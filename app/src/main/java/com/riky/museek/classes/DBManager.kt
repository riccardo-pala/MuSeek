package com.riky.museek.classes

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
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
import kotlinx.android.synthetic.main.fragment_sold_ad_details_instrument.view.*
import kotlinx.android.synthetic.main.password_popup_blue.*
import java.time.LocalDateTime

class DBManager {

    companion object {

        val storage = FirebaseStorage.getInstance()
        val database = FirebaseDatabase.getInstance()


        /*----------------------- VERIFY -----------------------------*/

        fun verifyLoggedUser(context: Context) {
            val uid = FirebaseAuth.getInstance().uid

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
                            performInstrumentTransaction(aid, context)
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
                    if (!userSnapshot.hasChild("soldAdsNo")) {
                        ref.child("soldAdsNo").setValue(0)
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

        fun saveUserOnDatabase(email: String, firstname: String, lastname: String, phone: String) {

            val uid = FirebaseAuth.getInstance().uid ?: ""

            val user = User(uid, email, firstname, lastname, "", phone)
            val ref = database.getReference("/users/$uid")

            ref.setValue(user)
                .addOnSuccessListener {
                    Log.d(DBManager::class.java.name, "User successfully saved on DB")
                }
                .addOnFailureListener {
                    Log.d(DBManager::class.java.name, "Error on Database: ${it.message}")
                }
        }

        fun saveAdOnDatabase(ad: AdInstrument, context: Context) {

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

        fun deleteMemberAdOnDatabase(aid: String) {

            database.getReference("/band_member_ads/$aid").removeValue()
                .addOnSuccessListener {
                    Log.d(DBManager::class.java.name, "Ad successfully removed from DB")
                }
                .addOnFailureListener{
                    Log.d(DBManager::class.java.name, "Error on Database: ${it.message}")
                }
        }

        fun deleteBandAdOnDatabase(aid: String) {

            database.getReference("/band_band_ads/$aid").removeValue()
                .addOnSuccessListener {
                    Log.d(DBManager::class.java.name, "Ad successfully removed from DB")
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

        fun performInstrumentTransaction(aid: String, context: Context) {

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
                            val ad = PurchasedAdInstrument(
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
                            val ref1 = database.getReference("/instrument_purchased_ads/$aid")
                            ref1.setValue(ad)
                                .addOnSuccessListener {
                                    Log.d(DBManager::class.java.name, "Ad successfully saved on DB")
                                }
                                .addOnFailureListener{
                                    Log.d(DBManager::class.java.name, "Error on Database: ${it.message}")
                                }
                            ref1.child("send").setValue(false)
                            ref1.child("sendNotified").setValue(false)
                            ref1.child("soldNotified").setValue(false)
                            ref1.child("isReviewed").setValue(false)

                            val ref2 = database.getReference("/instrument_users/$selleruid")

                            ref2.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        val soldAdsNo = dataSnapshot.child("soldAdsNo").value.toString().toInt() + 1
                                        ref2.child("soldAdsNo").setValue(soldAdsNo)
                                        ref2.removeEventListener(this)
                                    }
                                }
                                override fun onCancelled(databaseError: DatabaseError) {
                                    Log.d(ShowAdsInstrumentFragment::class.java.name, "ERROR on Database: ${databaseError.message}")
                                    ref2.removeEventListener(this)
                                }
                            })

                            aidRef.removeValue()
                                .addOnSuccessListener {
                                    Log.d(DBManager::class.java.name, "Ad successfully removed from DB")
                                }
                                .addOnFailureListener{
                                    Log.d(DBManager::class.java.name, "Error on Database: ${it.message}")
                                }
                            Toast.makeText(context, "Acquisto Completato!", Toast.LENGTH_LONG).show()
                            val activity = context as AppCompatActivity
                            activity.supportFragmentManager.popBackStack()
                            activity.supportFragmentManager.popBackStack()
                            activity.supportFragmentManager.beginTransaction().replace(R.id.fragment, InstrumentFragment()).addToBackStack(null).commit()
                        }
                        else {
                            Toast.makeText(context, "Qualcosa è andato storto, riprova!", Toast.LENGTH_LONG).show()
                            Log.d(DBManager::class.java.name, "Annuncio di proprietà dell'acquirente!")
                            val activity = context as AppCompatActivity
                            activity.supportFragmentManager.popBackStack()
                            activity.supportFragmentManager.popBackStack()
                            activity.supportFragmentManager.beginTransaction().replace(R.id.fragment, InstrumentFragment()).addToBackStack(null).commit()
                        }
                    }
                    else {
                        Toast.makeText(context, "Qualcosa è andato storto, riprova!", Toast.LENGTH_LONG).show()
                        Log.d(DBManager::class.java.name, "Annuncio non esistente!")
                        val activity = context as AppCompatActivity
                        activity.supportFragmentManager.popBackStack()
                        activity.supportFragmentManager.popBackStack()
                        activity.supportFragmentManager.beginTransaction().replace(R.id.fragment, InstrumentFragment()).addToBackStack(null).commit()
                    }
                    aidRef.removeEventListener(this)
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d(ShowAdsInstrumentFragment::class.java.name, "ERROR on Database: ${databaseError.message}")
                    val activity = context as AppCompatActivity
                    activity.supportFragmentManager.popBackStack()
                    activity.supportFragmentManager.popBackStack()
                    activity.supportFragmentManager.beginTransaction().replace(R.id.fragment, InstrumentFragment()).addToBackStack(null).commit()
                    aidRef.removeEventListener(this)
                }
            })

        }

        fun performNotify(context: Context, alertDialog: AlertDialog, aid: String?, view: View) {

            val selleruid = FirebaseAuth.getInstance().uid

            if (selleruid == null) {
                FirebaseAuth.getInstance().signOut()
                val intentMain = Intent(context, MainActivity::class.java)
                intentMain.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(context, intentMain, null)
            }

            val aidRef = database.getReference("/instrument_purchased_ads/$aid")

            aidRef.child("send").setValue(true)
                .addOnCompleteListener {
                    view.sendButtonSoldAdDetailsInstr.setBackgroundResource(R.drawable.shadow_button_grey_light)
                    view.sendButtonSoldAdDetailsInstr.text = "Già Inviato"
                    view.sendButtonSoldAdDetailsInstr.setOnClickListener(null)
                    Toast.makeText(context, "Acquirente Notificato!", Toast.LENGTH_LONG).show()
                    alertDialog.dismiss()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Qualcosa è andato storto, riprova!", Toast.LENGTH_LONG).show()
                    alertDialog.dismiss()
                }
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

        fun getRegionById(id: Int) : String {

            when(id) {
                1 -> return "Abruzzo"
                2 -> return "Basilicata"
                3 -> return "Calabria"
                4 -> return "Campania"
                5 -> return "Emilia-Romagna"
                6 -> return "Friuli-Venezia Giulia"
                7 -> return "Lazio"
                8 -> return "Liguria"
                9 -> return "Lombardia"
                10 -> return "Marche"
                11 -> return "Molise"
                12 -> return "Piemonte"
                13 -> return "Puglia"
                14 -> return "Sardegna"
                15 -> return "Sicilia"
                16 -> return "Toscana"
                17 -> return "Trentino-Alto Adige"
                18 -> return "Umbria"
                19 -> return "Valle d\'Aosta"
                20 -> return "Veneto"
            }
            return ""
        }

        fun getMusicianById(id: Int) : String {

            when(id) {
                1 -> return "Bassista"
                2 -> return "Batterista"
                3 -> return "Cantante"
                4 -> return "Chitarrista"
                5 -> return "Flautista"
                6 -> return "Sassofonista"
                7 -> return "Tastierista"
                8 -> return "Trombettista"
                9 -> return "Violinista"
            }
            return ""
        }
    }
}