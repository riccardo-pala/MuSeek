package com.riky.museek.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.riky.museek.R
import com.riky.museek.activities.HomepageActivity
import com.riky.museek.classes.AdInstrument
import com.riky.museek.classes.DBManager
import kotlinx.android.synthetic.main.fragment_new_ad_instrument.*
import kotlinx.android.synthetic.main.fragment_new_ad_instrument.view.*
import java.time.LocalDateTime
import java.util.*
import kotlin.math.roundToInt

class NewAdInstrumentFragment : Fragment() {

    private var pickedPhotoUri : Uri? = null
    var photoId : String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_new_ad_instrument, container, false)

        DBManager.verifyLoggedUser(context!!)

        view.homeButtonNewAdInstr.setOnClickListener {
            hideKeyboard(view)
            val intentHomepage = Intent(activity, HomepageActivity::class.java)
            intentHomepage.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentHomepage)
        }

        view.photoPickerButtonNewAdInstr.setOnClickListener {
            hideKeyboard(view)
            val intentPicker = Intent(Intent.ACTION_PICK)
            intentPicker.type = "image/*"
            startActivityForResult(intentPicker, 0)
        }

        view.submitButtonNewAdInstr.setOnClickListener {

            hideKeyboard(view)

            view.brandEditTextNewAdInstr.setBackgroundResource(R.drawable.shadow_edit_text_grey_light)
            view.modelEditTextNewAdInstr.setBackgroundResource(R.drawable.shadow_edit_text_grey_light)
            view.priceEditTextNewAdInstr.setBackgroundResource(R.drawable.shadow_edit_text_grey_light)
            view.categorySpinnerNewAdInstr.setBackgroundResource(R.drawable.shadow_spinner)
            view.conditionTextViewNewAdInstr.setShadowLayer(3f, 0f, 0f, Color.WHITE)
            view.photoPickerButtonNewAdInstr.setShadowLayer(3f, 2f, 2f, Color.RED)

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

        val brand = brandEditTextNewAdInstr.text.toString().trim()
        val model = modelEditTextNewAdInstr.text.toString().trim()
        val price = priceEditTextNewAdInstr.text.toString().trim()
        val category = categorySpinnerNewAdInstr.selectedItemPosition
        val conditionId = conditionRadioGroupNewAdInstr.checkedRadioButtonId

        var isEmptyFields = false

        if (brand.isEmpty()) {
            brandEditTextNewAdInstr.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
            isEmptyFields = true
        }
        if (model.isEmpty()) {
            modelEditTextNewAdInstr.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
            isEmptyFields = true
        }
        if (price.isEmpty()) {
            priceEditTextNewAdInstr.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
            isEmptyFields = true
        }
        if (category == 0) {
            categorySpinnerNewAdInstr.setBackgroundResource(R.drawable.shadow_spinner_error)
            isEmptyFields = true
        }
        if (conditionId == -1) {
            conditionTextViewNewAdInstr.setShadowLayer(10f, 0f, 0f, Color.RED)
            isEmptyFields = true
        }
        if (pickedPhotoUri == null) {
            photoPickerButtonNewAdInstr.setShadowLayer(10f, 0f, 0f, Color.RED)
            isEmptyFields = true
        }

        if (isEmptyFields) {
            Toast.makeText(activity, "Si prega di compilare tutti i campi del form.", Toast.LENGTH_LONG).show()
            return
        }

        val priceDouble = (price.toDouble() * 100).roundToInt() / 100.0

        if (priceDouble < 0) {
            priceEditTextNewAdInstr.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
            Log.d(NewAdInstrumentFragment::class.java.name, "Price not valid - ERR:1")
            Toast.makeText(activity, "Il prezzo inserito è in un formato non valido.", Toast.LENGTH_LONG).show()
            return
        }
        if (priceDouble > 100000) {
            priceEditTextNewAdInstr.setBackgroundResource(R.drawable.shadow_edit_text_grey_light_error)
            Log.d(NewAdInstrumentFragment::class.java.name, "Price not valid - ERR:3")
            Toast.makeText(activity, "Il prezzo massimo è di 100.000€", Toast.LENGTH_LONG).show()
            return
        }

        val condition =
            when(conditionId) {
                newRadioButtonNewAdInstr.id -> 1
                usedAsNewRadioButtonNewAdInstr.id -> 2
                usedGoodRadioButtonNewAdInstr.id -> 3
                usedDiscreetRadioButtonNewAdInstr.id -> 4
                else -> 4
            }

        if (context != null) DBManager.verifyLoggedUser(context!!)

        val uid = FirebaseAuth.getInstance().uid

        photoId = "photo-" + UUID.randomUUID().toString()

        DBManager.uploadPickedPhotoOnStorage(pickedPhotoUri!!, "instrument_ads/$photoId", context!!)

        val date = LocalDateTime.now().toString()

        val aid = "instr-ad-" + UUID.randomUUID()

        val ad = AdInstrument(aid, brand, model, priceDouble, category, condition, photoId!!, uid!!, date)

        DBManager.saveAdOnDatabase(ad, context!!)

        fragmentManager!!.popBackStack()
        fragmentManager!!.beginTransaction().replace(R.id.fragment, InstrumentFragment()).commit()
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