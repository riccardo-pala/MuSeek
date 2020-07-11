package com.riky.museek.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.riky.museek.R
import com.riky.museek.activities.HomepageActivity
import com.riky.museek.activities.MainActivity
import com.riky.museek.classes.Ad
import com.riky.museek.classes.DBManager
import kotlinx.android.synthetic.main.fragment_edit_ad_instrument.*
import kotlinx.android.synthetic.main.fragment_edit_ad_instrument.view.*
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_edit_profile.view.*
import java.time.LocalDateTime
import java.util.*

class EditAdInstrumentFragment : Fragment() {

    var pickedPhotoUri : Uri? = null
    var photoId : String? = null
    var aid : String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_edit_ad_instrument, container, false)

        if (context != null) DBManager.verifyLoggedUser(context!!)

        try {
            requireArguments()
        }
        catch (e : IllegalStateException) {
            Toast.makeText(activity, "Errore durante il caricamento dell'annuncio. Riprova.", Toast.LENGTH_LONG).show()
            fragmentManager!!.popBackStack()
            fragmentManager!!.beginTransaction().replace(R.id.fragment, MyAdsInstrumentFragment()).commit()
        }

        val animation = RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        animation.interpolator = LinearInterpolator()
        animation.repeatCount = Animation.INFINITE
        animation.duration = 700

        view.loadingImageViewEditAdInstr.startAnimation(animation);

        view.homeButtonEditAdInstr.setOnClickListener {
            val intentHomepage = Intent(activity, HomepageActivity::class.java)
            intentHomepage.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentHomepage)
        }

        view.photoPickerButtonEditAdInstr.setOnClickListener {
            val intentPicker = Intent(Intent.ACTION_PICK)
            intentPicker.type = "image/*"
            startActivityForResult(intentPicker, 0)
        }

        aid = arguments!!.getString("aid", "")

        if (!DBManager.verifyAdUser(aid!!, FirebaseAuth.getInstance().uid!!)) {
            Toast.makeText(activity, "Errore durante il caricamento dell'annuncio. Riprova.", Toast.LENGTH_LONG).show()
            fragmentManager!!.popBackStack()
            fragmentManager!!.beginTransaction().replace(R.id.fragment, MyAdsInstrumentFragment()).commit()
        }

        view.brandEditTextEditAdInstr.setText(arguments!!.getString("brand", "Marca"))
        view.modelEditTextEditAdInstr.setText(arguments!!.getString("model", "Modello"))
        view.priceEditTextEditAdInstr.setText(arguments!!.getFloat("price", 0f).toString())
        view.categorySpinnerEditAdInstr.setSelection(arguments!!.getInt("category", 0))
        photoId = arguments!!.getString("photoId", null)
        val ref = FirebaseStorage.getInstance().getReference("/images/instrument_ads/")

        if (photoId != null)
            ref.child(photoId!!).getBytes(4*1024*1024)
                .addOnSuccessListener { bytes ->
                    view.photoPickerButtonEditAdInstr.alpha = 0f
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0 ,bytes.size)
                    view.imageViewEditAdInstr.setImageBitmap(bitmap)
                    view.loadingImageViewEditAdInstr.clearAnimation()
                    view.loadingLayoutEditAdInstr.visibility = View.GONE
                }

        view.updateButtonEditAdInstr.setOnClickListener {
            performUpdateAd()
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d(EditAdInstrumentFragment::class.java.name, "Photo was picked")
            pickedPhotoUri = data.data
            photoPickerButtonEditAdInstr.alpha = 0f
            imageViewEditAdInstr.setImageURI(pickedPhotoUri)
        }
    }

    private fun performUpdateAd() {

        val uid = FirebaseAuth.getInstance().uid

        if (context != null) DBManager.verifyLoggedUser(context!!)

        val brand = brandEditTextEditAdInstr.text.toString()
        //Log.d(EditAdInstrumentFragment::class.java.name, "Brand: $brand")
        val model = modelEditTextEditAdInstr.text.toString()
        //Log.d(EditAdInstrumentFragment::class.java.name, "Model: $model")
        val price = priceEditTextEditAdInstr.text.toString()
        //Log.d(EditAdInstrumentFragment::class.java.name, "Price: $price")
        val category = categorySpinnerEditAdInstr.selectedItemPosition
        //Log.d(EditAdInstrumentFragment::class.java.name, "Category: $category")

        if (brand.isEmpty() || model.isEmpty() || price.isEmpty() || categorySpinnerEditAdInstr.selectedItemPosition == 0) {
            Toast.makeText(activity, "Si prega di compilare tutti i campi del form.", Toast.LENGTH_LONG).show()
            return
        }

        val priceFloat = price.toFloatOrNull()

        if (priceFloat == null) {
            Log.d(EditAdInstrumentFragment::class.java.name, "Price not valid - ERR:1")
            Toast.makeText(activity, "Il prezzo inserito è in un formato non valido.", Toast.LENGTH_LONG).show()
            return
        }
        if (((priceFloat*100.0f)%1.0f) != 0.0f) {
            Log.d(EditAdInstrumentFragment::class.java.name, "Price not valid - ERR:2 - ${(priceFloat*100f)%1f}")
            Toast.makeText(activity, "Si prega di inserire un prezzo con massimo 2 cifre decimali.", Toast.LENGTH_LONG).show()
            return
        }
        if (priceFloat > 100000) {
            Log.d(EditAdInstrumentFragment::class.java.name, "Price not valid - ERR:3")
            Toast.makeText(activity, "Il prezzo massimo è di 100.000€", Toast.LENGTH_LONG).show()
            return
        }

        if (photoId == null) photoId = "photo-" + UUID.randomUUID().toString()

        if (pickedPhotoUri != null) {
            if(!DBManager.uploadPickedPhotoOnStorage(pickedPhotoUri!!, "instrument_ads/$photoId")) {
                Toast.makeText(activity, "Errore durante l'aggiornamento dell'annuncio. Riprova", Toast.LENGTH_LONG).show()
                return
            }
        }

        val date = arguments!!.getString("date", LocalDateTime.now().toString())

        val ad = Ad(aid!!, brand, model, priceFloat, category, photoId!!, uid!!, date)

        if(DBManager.saveAdOnDatabase(ad))
            Toast.makeText(activity, "Annuncio aggiornato con successo!", Toast.LENGTH_LONG).show()
        else {
            Toast.makeText(activity, "Errore durante l'aggiornamento dell'annuncio. Riprova", Toast.LENGTH_LONG).show()
            return
        }

        fragmentManager!!.popBackStack()
        fragmentManager!!.beginTransaction().replace(R.id.fragment, MyAdsInstrumentFragment()).commit()
    }
}