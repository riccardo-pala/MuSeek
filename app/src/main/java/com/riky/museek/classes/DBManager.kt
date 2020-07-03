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

        fun getEmailByUid(uid: String) : String {

            val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

            var email = ""

            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        email = dataSnapshot.child("email").value as String
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d(MyAdsInstrumentFragment::class.java.name, "ERROR on Database: ${databaseError.message}")
                }
            })

            return email
        }

        fun saveUserOnDatabase(email: String, firstname: String, lastname: String): Boolean {

            var isSuccess = true

            val uid = FirebaseAuth.getInstance().uid ?: ""

            val user = User(uid, email, firstname, lastname)
            val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

            ref.setValue(user)
                .addOnSuccessListener {
                    Log.d(RegistrationFragment::class.java.name, "User successfully saved on DB")
                }
                .addOnFailureListener {
                    Log.d(RegistrationFragment::class.java.name, "Error on Database: ${it.message}")
                    isSuccess = false
                }
            return isSuccess
        }

        fun saveAdOnDatabase(ad: Ad) : Boolean {

            var isSuccess = true

            val aid = "instr-ad-" + UUID.randomUUID()

            val ref = FirebaseDatabase.getInstance().getReference("/instrument_ads/$aid")

            ref.setValue(ad)
                .addOnSuccessListener {
                    Log.d(NewAdInstrumentFragment::class.java.name, "Ad successfully saved on DB")
                }
                .addOnFailureListener{
                    Log.d(NewAdInstrumentFragment::class.java.name, "Error on Database: ${it.message}")
                    isSuccess = false
                }

            return isSuccess
        }

        fun uploadPickedPhotoOnStorage(pickedPhotoUri: Uri, photoId: String) : Boolean {

            var isSuccess = true

            val ref = FirebaseStorage.getInstance().getReference("/images/$photoId")

            ref.putFile(pickedPhotoUri)
                .addOnSuccessListener {
                    Log.d(NewAdInstrumentFragment::class.java.name, "Image succesfully loaded on Firebase Storage")
                }
                .addOnFailureListener {
                    Log.d(NewAdInstrumentFragment::class.java.name, "Error while uploading file on Firebase: ${it.message}")
                    isSuccess = false
                }

            return isSuccess
        }
    }
}