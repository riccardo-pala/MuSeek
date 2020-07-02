package com.riky.museek

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_new_ad.*
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.util.*

class NewAdActivity : AppCompatActivity() {

    var pickedPhotoUri : Uri? = null
    var photoId : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_ad)

        val uid = FirebaseAuth.getInstance().uid

        if (uid == null) {
            FirebaseAuth.getInstance().signOut()
            val intentMain = Intent(this, IndexActivity::class.java)
            intentMain.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentMain)
        }

        photoPickerButtonNewAd.setOnClickListener {
            val intentPicker = Intent(Intent.ACTION_PICK)
            intentPicker.type = "image/*"
            startActivityForResult(intentPicker, 0)
        }

        submitButtonNewAd.setOnClickListener {
            performNewAd()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d(NewAdActivity::class.java.name, "Photo was picked")
            pickedPhotoUri = data.data
            val bitmapPhoto = MediaStore.Images.Media.getBitmap(contentResolver, pickedPhotoUri)
            val bitmapPhotoDrawable = BitmapDrawable(bitmapPhoto)
            photoPickerButtonNewAd.text = ""
            photoPickerButtonNewAd.background = bitmapPhotoDrawable
        }
    }

    private fun uploadPickedPhotoOnStorage() : Boolean {

        if (pickedPhotoUri == null) {
            Toast.makeText(this, "Si prega di caricare una foto.", Toast.LENGTH_LONG).show()
            return false
        }

        var isSuccess = true
        photoId = "photo-" + UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$photoId")

        ref.putFile(pickedPhotoUri!!)
            .addOnSuccessListener {
                Log.d(NewAdActivity::class.java.name, "Image succesfully loaded on Firebase Storage")
            }
            .addOnFailureListener {
                Log.d(NewAdActivity::class.java.name, "Error while uploading file on Firebase: ${it.message}")
                Toast.makeText(this, "Errore durante il caricamento dell'immagine. Riprova.", Toast.LENGTH_LONG).show()
                isSuccess = false
            }

        return isSuccess
    }

    private fun performNewAd() {

        val brand = brandEditTextNewAd.text.toString()
        Log.d(NewAdActivity::class.java.name, "Brand: $brand")
        val model = modelEditTextNewAd.text.toString()
        Log.d(NewAdActivity::class.java.name, "Model: $model")
        val price = priceEditTextNewAd.text.toString()
        Log.d(NewAdActivity::class.java.name, "Price: $price")
        val category = categorySpinnerNewAd.selectedItem.toString()
        Log.d(NewAdActivity::class.java.name, "Category: $category")
        Log.d(NewAdActivity::class.java.name, "Category index: ${categorySpinnerNewAd.selectedItemPosition}")

        if (brand.isEmpty() || model.isEmpty() || price.isEmpty() || categorySpinnerNewAd.selectedItemPosition == 0) {
            Toast.makeText(this, "Si prega di compilare tutti i campi del form.", Toast.LENGTH_LONG).show()
            return
        }

        val priceFloat = price.toFloatOrNull()

        if (priceFloat == null) {
            Log.d(NewAdActivity::class.java.name, "Price not valid - ERR:1")
            Toast.makeText(this, "Il prezzo inserito è in un formato non valido.", Toast.LENGTH_LONG).show()
            return
        }
        /*if ((priceFloat%0.01f) != 0f) {
            Log.d(NewAdActivity::class.java.name, "Price not valid - ERR:2 - ${priceFloat%0.10f}")
            Toast.makeText(this, "Si prega di inserire un prezzo con massimo 2 cifre decimali.", Toast.LENGTH_LONG)
            return
        }*/
        if (priceFloat > 100000) {
            Log.d(NewAdActivity::class.java.name, "Price not valid - ERR:3")
            Toast.makeText(this, "Il prezzo massimo è di 100.000€", Toast.LENGTH_LONG).show()
            return
        }

        val uid = FirebaseAuth.getInstance().uid

        if (uid == null) {
            Log.d(NewAdActivity::class.java.name, "User not logged")
            FirebaseAuth.getInstance().signOut()
            val intentMain = Intent(this, IndexActivity::class.java)
            intentMain.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentMain)
        }

        if(!uploadPickedPhotoOnStorage()) return

        val date = LocalDateTime.now().toString()

        val ad = Ad(brand, model, priceFloat, category, photoId!!, uid!!, date)

        saveAdOnDatabase(ad)

        val intentInstrument = Intent(this, InstrumentActivity::class.java)
        intentInstrument.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intentInstrument)
    }

    private fun saveAdOnDatabase(ad: Ad) {

        val aid = "instr-ad-" + UUID.randomUUID()

        val ref = FirebaseDatabase.getInstance().getReference("/instrument_ads/$aid")

        ref.setValue(ad)
            .addOnSuccessListener {
                Log.d(RegistrationActivity::class.java.name, "Ad successfully saved on DB")
                Toast.makeText(this, "Annuncio aggiunto con successo!", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener{
                Toast.makeText(this, "Errore durante l'aggiunta dell'annuncio: ${it.message}", Toast.LENGTH_LONG).show()
            }

        return
    }
}