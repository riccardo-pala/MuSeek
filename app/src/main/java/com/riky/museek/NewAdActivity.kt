package com.riky.museek

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_new_ad.*
import java.util.*

class NewAdActivity : AppCompatActivity() {

    var pickedPhotoUri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_ad)

        photoPickerButtonNewAd.setOnClickListener {
            var intentPicker = Intent(Intent.ACTION_PICK)
            intentPicker.type = "image/*"
            startActivityForResult(intentPicker, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==0 && resultCode== Activity.RESULT_OK && data != null) {
            Log.d(NewAdActivity::class.java.name, "Photo was picked")
            pickedPhotoUri = data.data
            val bitmapPhoto = MediaStore.Images.Media.getBitmap(contentResolver, pickedPhotoUri)
            val bitmapPhotoDrawable = BitmapDrawable(bitmapPhoto)
            photoPickerButtonNewAd.setText("")
            photoPickerButtonNewAd.setBackgroundDrawable(bitmapPhotoDrawable)
        }

    }

    private fun uploadPickedPhotoOnFirebaseStorage() : Boolean {

        if (pickedPhotoUri == null) return true

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        var isSuccess = true

        ref.putFile(pickedPhotoUri!!)
            .addOnSuccessListener {
                Log.d(NewAdActivity::class.java.name, "Image succesfully loaded on Firebase Storage")
            }
            .addOnFailureListener {
                Log.d(NewAdActivity::class.java.name, "Error while uploading file on Firebase: ${it.message}")
                Toast.makeText(this, "Errore durante il caricamento dell'immagine. Riprova.", Toast.LENGTH_LONG)
                isSuccess = false
            }

        return isSuccess
    }
}