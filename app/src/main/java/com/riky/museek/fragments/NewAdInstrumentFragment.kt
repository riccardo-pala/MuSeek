package com.riky.museek.fragments

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
import com.riky.museek.classes.Ad
import com.riky.museek.activities.MainActivity
import com.riky.museek.R
import com.riky.museek.activities.HomepageActivity
import com.riky.museek.classes.DBManager
import kotlinx.android.synthetic.main.fragment_my_ads_instrument.view.*
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

        view.homeButtonNewAdInstr.setOnClickListener {
            val intentHomepage = Intent(activity, HomepageActivity::class.java)
            intentHomepage.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentHomepage)
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

    private fun performNewAd() {

        val brand = brandEditTextNewAdInstr.text.toString()
        Log.d(NewAdInstrumentFragment::class.java.name, "Brand: $brand")
        val model = modelEditTextNewAdInstr.text.toString()
        Log.d(NewAdInstrumentFragment::class.java.name, "Model: $model")
        val price = priceEditTextNewAdInstr.text.toString()
        Log.d(NewAdInstrumentFragment::class.java.name, "Price: $price")
        val category = categorySpinnerNewAdInstr.selectedItemPosition
        Log.d(NewAdInstrumentFragment::class.java.name, "Category: $category")

        if (brand.isEmpty() || model.isEmpty() || price.isEmpty() || category == 0) {
            Log.d(NewAdInstrumentFragment::class.java.name, "Some empty fields.")
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

        if (context != null) DBManager.verifyLoggedUser(context!!)

        val uid = FirebaseAuth.getInstance().uid

        if (pickedPhotoUri == null)
            Toast.makeText(activity, "Si prega di caricare una foto.", Toast.LENGTH_LONG).show()

        photoId = "photo-" + UUID.randomUUID().toString()

        if(!DBManager.uploadPickedPhotoOnStorage(pickedPhotoUri!!, photoId!!)) {
            Toast.makeText(activity, "Errore durante la creazione dell'annuncio. Riprova", Toast.LENGTH_LONG).show()
            return
        }

        val date = LocalDateTime.now().toString()

        val aid = "instr-ad-" + UUID.randomUUID()

        val ad = Ad(aid, brand, model, priceFloat, category, photoId!!, uid!!, date)

        if(DBManager.saveAdOnDatabase(ad))
            Toast.makeText(activity, "Annuncio aggiunto con successo!", Toast.LENGTH_LONG).show()
        else {
            Toast.makeText(activity, "Errore durante la creazione dell'annuncio. Riprova", Toast.LENGTH_LONG).show()
            return
        }

        fragmentManager!!.beginTransaction().replace(R.id.fragment, InstrumentFragment()).commit()
    }
}