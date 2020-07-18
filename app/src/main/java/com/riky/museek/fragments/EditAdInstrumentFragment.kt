package com.riky.museek.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.riky.museek.R
import com.riky.museek.activities.HomepageActivity
import com.riky.museek.classes.AdInstrument
import com.riky.museek.classes.AlertDialogInflater
import com.riky.museek.classes.DBManager
import kotlinx.android.synthetic.main.fragment_edit_ad_instrument.*
import kotlinx.android.synthetic.main.fragment_edit_ad_instrument.view.*
import java.time.LocalDateTime
import java.util.*
import kotlin.math.roundToInt

class EditAdInstrumentFragment : Fragment() {

    private var pickedPhotoUri : Uri? = null
    private var photoId : String? = null
    private var aid : String? = null
    private var alertDialog : AlertDialog? = null

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

        setListeners(view)

        alertDialog = AlertDialogInflater.inflateLoadingDialog(context!!, AlertDialogInflater.BLUE)

        aid = arguments!!.getString("aid", "")

        DBManager.verifyAdUser(aid!!, FirebaseAuth.getInstance().uid!!, context!!)

        view.brandEditTextEditAdInstr.setText(arguments!!.getString("brand", "Marca"))
        view.modelEditTextEditAdInstr.setText(arguments!!.getString("model", "Modello"))
        view.priceEditTextEditAdInstr.setText(arguments!!.getDouble("price", 0.0).toString())
        view.categorySpinnerEditAdInstr.setSelection(arguments!!.getInt("category", 0))
        val condition = arguments!!.getInt("condition", -1)
        Log.d(EditAdInstrumentFragment::class.java.name, "Condition: $condition")
        val conditionId =
            when(condition) {
                1 -> view.newRadioButtonEditAdInstr.id
                2 -> view.usedAsNewRadioButtonEditAdInstr.id
                3 -> view.usedGoodRadioButtonEditAdInstr.id
                4 -> view.usedDiscreetRadioButtonEditAdInstr.id
                else -> -1
            }
        if (conditionId != -1) view.conditionRadioGroupEditAdInstr.check(conditionId)
        else view.conditionRadioGroupEditAdInstr.clearCheck()

        photoId = arguments!!.getString("photoId", null)
        val ref = FirebaseStorage.getInstance().getReference("/images/instrument_ads/")

        if (photoId != null && photoId!!.isNotEmpty())
            ref.child(photoId!!).getBytes(4*1024*1024)
                .addOnSuccessListener { bytes ->
                    view.photoPickerButtonEditAdInstr.alpha = 0f
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0 ,bytes.size)
                    view.imageViewEditAdInstr.setImageBitmap(bitmap)
                    alertDialog!!.dismiss()
                }
                .addOnFailureListener {
                    alertDialog!!.dismiss()
                }
        else {
            alertDialog!!.dismiss()
        }

        view.updateButtonEditAdInstr.setOnClickListener {

            hideKeyboard(view)

            view.brandEditTextEditAdInstr.setBackgroundResource(R.drawable.shadow_edit_text_grey_light)
            view.modelEditTextEditAdInstr.setBackgroundResource(R.drawable.shadow_edit_text_grey_light)
            view.priceEditTextEditAdInstr.setBackgroundResource(R.drawable.shadow_edit_text_grey_light)
            view.categorySpinnerEditAdInstr.setBackgroundResource(R.drawable.shadow_spinner)
            view.conditionTextViewEditAdInstr.setShadowLayer(3f, 0f, 0f, Color.WHITE)

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

        val brand = brandEditTextEditAdInstr.text.toString().trim()
        val model = modelEditTextEditAdInstr.text.toString().trim()
        val price = priceEditTextEditAdInstr.text.toString().trim()
        val category = categorySpinnerEditAdInstr.selectedItemPosition
        val conditionId = conditionRadioGroupEditAdInstr.checkedRadioButtonId

        var isEmptyFields = false

        if (brand.isEmpty()) {
            brandEditTextEditAdInstr.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
            isEmptyFields = true
        }
        if (model.isEmpty()) {
            modelEditTextEditAdInstr.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
            isEmptyFields = true
        }
        if (price.isEmpty()) {
            priceEditTextEditAdInstr.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
            isEmptyFields = true
        }
        if (category == 0) {
            categorySpinnerEditAdInstr.setBackgroundResource(R.drawable.shadow_spinner_error)
            isEmptyFields = true
        }
        if (conditionId == -1) {
            conditionTextViewEditAdInstr.setShadowLayer(10f, 0f, 0f, Color.RED)
            isEmptyFields = true
        }

        if (isEmptyFields) {
            Toast.makeText(activity, "Si prega di compilare tutti i campi del form.", Toast.LENGTH_LONG).show()
            return
        }

        val priceDouble = (price.toDouble() * 100).roundToInt() / 100.0

        if (priceDouble < 0) {
            priceEditTextEditAdInstr.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
            Log.d(EditAdInstrumentFragment::class.java.name, "Price not valid - ERR:1")
            Toast.makeText(activity, "Il prezzo inserito è in un formato non valido.", Toast.LENGTH_LONG).show()
            return
        }
        if (priceDouble > 100000) {
            priceEditTextEditAdInstr.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
            Log.d(EditAdInstrumentFragment::class.java.name, "Price not valid - ERR:3")
            Toast.makeText(activity, "Il prezzo massimo è di 100.000€", Toast.LENGTH_LONG).show()
            return
        }

        val condition =
            when(conditionId) {
                newRadioButtonEditAdInstr.id -> 1
                usedAsNewRadioButtonEditAdInstr.id -> 2
                usedGoodRadioButtonEditAdInstr.id -> 3
                usedDiscreetRadioButtonEditAdInstr.id -> 4
                else -> 4
            }

        if (photoId == null) photoId = "photo-" + UUID.randomUUID().toString()

        if (pickedPhotoUri != null) {
            DBManager.uploadPickedPhotoOnStorage(pickedPhotoUri!!, "instrument_ads/$photoId", context!!)
        }

        val date = arguments!!.getString("date", LocalDateTime.now().toString())

        val ad = AdInstrument(aid!!, brand, model, priceDouble, category, condition, photoId!!, uid!!, date)

        DBManager.saveAdOnDatabase(ad, context!!)

        val fragment = MyAdsInstrumentFragment()
        val args = Bundle()
        args.putBoolean("imageLoaded", true)
        fragment.arguments = args
        fragmentManager!!.popBackStack()
        fragmentManager!!.beginTransaction().replace(R.id.fragment, fragment).commit()
    }

    private fun setListeners(view: View) {

        view.homeButtonEditAdInstr.setOnClickListener {
            hideKeyboard(view)
            val intentHomepage = Intent(activity, HomepageActivity::class.java)
            intentHomepage.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentHomepage)
        }

        view.photoPickerButtonEditAdInstr.setOnClickListener {
            hideKeyboard(view)
            val intentPicker = Intent(Intent.ACTION_PICK)
            intentPicker.type = "image/*"
            startActivityForResult(intentPicker, 0)
        }
    }

    private fun hideKeyboard(view: View) {
        try {
            val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}