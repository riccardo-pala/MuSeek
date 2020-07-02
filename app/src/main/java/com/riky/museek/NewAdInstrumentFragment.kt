package com.riky.museek

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_new_ad_instrument.*
import kotlinx.android.synthetic.main.fragment_new_ad_instrument.view.*
import java.time.LocalDateTime
import java.util.*

class NewAdInstrumentFragment : Fragment() {

    var pickedPhotoUri : Uri? = null
    var photoId : String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_new_ad_instrument, container, false)

        val uid = FirebaseAuth.getInstance().uid

        if (uid == null) {
            FirebaseAuth.getInstance().signOut()
            val intentMain = Intent(activity, MainActivity::class.java)
            intentMain.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentMain)
        }

        view.photoPickerButtonNewAdInstr.setOnClickListener {
            val intentPicker = Intent(Intent.ACTION_PICK)
            intentPicker.type = "image/*"
            startActivityForResult(intentPicker, 0)
        }

        view.submitButtonNewAdInstr.setOnClickListener {
            performNewAd()
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d(NewAdInstrumentFragment::class.java.name, "Photo was picked")
            pickedPhotoUri = data.data
            photoPickerButtonNewAdInstr.alpha = 0f
            imageViewNewAdInstr.setImageURI(pickedPhotoUri)
        }
    }

    private fun uploadPickedPhotoOnStorage() : Boolean {

        if (pickedPhotoUri == null) {
            Toast.makeText(activity, "Si prega di caricare una foto.", Toast.LENGTH_LONG).show()
            return false
        }

        var isSuccess = true
        photoId = "photo-" + UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$photoId")

        ref.putFile(pickedPhotoUri!!)
            .addOnSuccessListener {
                Log.d(NewAdInstrumentFragment::class.java.name, "Image succesfully loaded on Firebase Storage")
            }
            .addOnFailureListener {
                Log.d(NewAdInstrumentFragment::class.java.name, "Error while uploading file on Firebase: ${it.message}")
                Toast.makeText(activity, "Errore durante il caricamento dell'immagine. Riprova.", Toast.LENGTH_LONG).show()
                isSuccess = false
            }

        return isSuccess
    }

    private fun performNewAd() {

        val brand = brandEditTextNewAdInstr.text.toString()
        Log.d(NewAdInstrumentFragment::class.java.name, "Brand: $brand")
        val model = modelEditTextNewAdInstr.text.toString()
        Log.d(NewAdInstrumentFragment::class.java.name, "Model: $model")
        val price = priceEditTextNewAdInstr.text.toString()
        Log.d(NewAdInstrumentFragment::class.java.name, "Price: $price")
        val category = categorySpinnerNewAdInstr.selectedItem.toString()
        Log.d(NewAdInstrumentFragment::class.java.name, "Category: $category")
        Log.d(NewAdInstrumentFragment::class.java.name, "Category index: ${categorySpinnerNewAdInstr.selectedItemPosition}")

        if (brand.isEmpty() || model.isEmpty() || price.isEmpty() || categorySpinnerNewAdInstr.selectedItemPosition == 0) {
            Toast.makeText(activity, "Si prega di compilare tutti i campi del form.", Toast.LENGTH_LONG).show()
            return
        }

        val priceFloat = price.toFloatOrNull()

        if (priceFloat == null) {
            Log.d(NewAdInstrumentFragment::class.java.name, "Price not valid - ERR:1")
            Toast.makeText(activity, "Il prezzo inserito è in un formato non valido.", Toast.LENGTH_LONG).show()
            return
        }
        if (((priceFloat*100.0f)%1.0f) != 0.0f) {
            Log.d(NewAdInstrumentFragment::class.java.name, "Price not valid - ERR:2 - ${(priceFloat*100f)%1f}")
            Toast.makeText(activity, "Si prega di inserire un prezzo con massimo 2 cifre decimali.", Toast.LENGTH_LONG).show()
            return
        }
        if (priceFloat > 100000) {
            Log.d(NewAdInstrumentFragment::class.java.name, "Price not valid - ERR:3")
            Toast.makeText(activity, "Il prezzo massimo è di 100.000€", Toast.LENGTH_LONG).show()
            return
        }

        val uid = FirebaseAuth.getInstance().uid

        if (uid == null) {
            Log.d(NewAdInstrumentFragment::class.java.name, "User not logged")
            FirebaseAuth.getInstance().signOut()
            val intentMain = Intent(activity, MainActivity::class.java)
            intentMain.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentMain)
        }

        if(!uploadPickedPhotoOnStorage()) return

        val date = LocalDateTime.now().toString()

        val ad = Ad(brand, model, priceFloat, category, photoId!!, uid!!, date)

        saveAdOnDatabase(ad)

        fragmentManager!!.beginTransaction().replace(R.id.fragment, InstrumentFragment()).commit()
    }

    private fun saveAdOnDatabase(ad: Ad) {

        val aid = "instr-ad-" + UUID.randomUUID()

        val ref = FirebaseDatabase.getInstance().getReference("/instrument_ads/$aid")

        var isSuccess = true

        ref.setValue(ad)
            .addOnSuccessListener {
                Log.d(NewAdInstrumentFragment::class.java.name, "Ad successfully saved on DB")
            }
            .addOnFailureListener{
                Log.d(NewAdInstrumentFragment::class.java.name, "Error on Database: ${it.message}")
                isSuccess = false
            }

        if (isSuccess)
            Toast.makeText(activity, "Annuncio aggiunto con successo!", Toast.LENGTH_LONG).show()
        else
            Toast.makeText(activity, "Errore durante la creazione dell'annuncio. Riprova", Toast.LENGTH_LONG).show()

        return
    }
}