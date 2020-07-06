package com.riky.museek.classes

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.riky.museek.fragments.MyAdsInstrumentFragment
import com.riky.museek.fragments.NewAdInstrumentFragment
import com.riky.museek.fragments.RegistrationFragment
import java.util.*

class DBManager (){

    companion object {

        val storage = FirebaseStorage.getInstance()
        val database = FirebaseDatabase.getInstance()

        fun getNameByUid(uid: String) : String {

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

        fun getCategoryStringByType(type: String) : String {
            when (type) {
                "F" -> return "FlautoOboeSassofonoTromba"
                "C" -> return "Basso ElettricoChitarra AcusticaChitarra ClassicaChitarra ElettricaUkulele"
                "T" -> return "PianoforteSynthTastiera"
                "A" -> return "ContrabbassoViolinoVioloncello"
            }
            return ""
        }
    }
}