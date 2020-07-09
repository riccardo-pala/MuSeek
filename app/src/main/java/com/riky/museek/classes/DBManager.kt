package com.riky.museek.classes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.riky.museek.activities.MainActivity

class DBManager {

    companion object {

        val storage = FirebaseStorage.getInstance()
        val database = FirebaseDatabase.getInstance()

        fun verifyLoggedUser(context: Context) {
            val uid = FirebaseAuth.getInstance().uid

            if (uid == null) {
                FirebaseAuth.getInstance().signOut()
                val intentMain = Intent(context, MainActivity::class.java)
                intentMain.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(context, intentMain, null)
            }
        }

        fun getNameByUid(uid: String) : String { //TODO: NON FUNZIONA

            val ref = database.getReference("/users/")

            var name = ""

            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        name =
                            dataSnapshot.child(uid).child("firstname").value.toString() + " " +
                                    dataSnapshot.child(uid).child("lastname").value.toString()
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d(DBManager::class.java.name, "ERROR on Database: ${databaseError.message}")
                }
            })

            return name
        }

        fun saveUserOnDatabase(email: String, firstname: String, lastname: String): Boolean {

            var isSuccess = true

            val uid = FirebaseAuth.getInstance().uid ?: ""

            val user = User(uid, email, firstname, lastname)
            val ref = database.getReference("/users/$uid")

            ref.setValue(user)
                .addOnSuccessListener {
                    Log.d(DBManager::class.java.name, "User successfully saved on DB")
                }
                .addOnFailureListener {
                    Log.d(DBManager::class.java.name, "Error on Database: ${it.message}")
                    isSuccess = false
                }
            return isSuccess
        }

        fun saveAdOnDatabase(ad: Ad) : Boolean {

            var isSuccess = true

            val ref = database.getReference("/instrument_ads/${ad.aid}")

            ref.setValue(ad)
                .addOnSuccessListener {
                    Log.d(DBManager::class.java.name, "Ad successfully saved on DB")
                }
                .addOnFailureListener{
                    Log.d(DBManager::class.java.name, "Error on Database: ${it.message}")
                    isSuccess = false
                }

            return isSuccess
        }

        fun deleteAdOnDatabase(aid: String, photoId: String) {

            database.getReference("/instrument_ads/$aid").removeValue()
                .addOnSuccessListener {
                    Log.d(DBManager::class.java.name, "Ad successfully removed from DB")
                }
                .addOnFailureListener{
                    Log.d(DBManager::class.java.name, "Error on Database: ${it.message}")
                }

            Log.d(DBManager::class.java.name, "photoId: $photoId")

            storage.getReference("/images/$photoId").delete()
                .addOnSuccessListener {
                    Log.d(DBManager::class.java.name, "Pic successfully removed from Storage")
                }
                .addOnFailureListener{
                    Log.d(DBManager::class.java.name, "Error on Database: ${it.message}")
                }
        }

        fun uploadPickedPhotoOnStorage(pickedPhotoUri: Uri, photoId: String) : Boolean {

            var isSuccess = true

            val ref = storage.getReference("/images/$photoId")

            ref.putFile(pickedPhotoUri)
                .addOnSuccessListener {
                    Log.d(DBManager::class.java.name, "Image succesfully loaded on Firebase Storage")
                }
                .addOnFailureListener {
                    Log.d(DBManager::class.java.name, "Error while uploading file on Firebase: ${it.message}")
                    isSuccess = false
                }

            return isSuccess
        }

        fun verifyAdUser(aid: String, uid: String) : Boolean {

            var isSuccess = true

            val ref = database.getReference("/instrument_ads/$aid")

            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (!dataSnapshot.exists() || dataSnapshot.child("uid").value.toString() != uid)
                        isSuccess = false
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d(DBManager::class.java.name, "ERROR on Database: ${databaseError.message}")
                    isSuccess = false
                }
            })

            return isSuccess
        }

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

        fun getSpinnerElement(s: String) : Int {

            when(s) {
                "Basso Elettrico" -> return 1
                "Chitarra Acustica" -> return 2
                "Chitarra Classica" -> return 3
                "Chitarra Elettrica" -> return 4
                "Contrabbasso" -> return 5
                "Flauto" -> return 6
                "Oboe" -> return 7
                "Pianoforte" -> return 8
                "Sassofono" -> return 9
                "Synth" -> return 10
                "Tastiera" -> return 11
                "Tromba" -> return 12
                "Ukulele" -> return 13
                "Violino" -> return 14
                "Violoncello" -> return 15
            }
            return 0
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