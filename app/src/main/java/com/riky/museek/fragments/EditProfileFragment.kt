package com.riky.museek.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.riky.museek.R
import com.riky.museek.activities.HomepageActivity
import com.riky.museek.classes.AlertDialogInflater
import com.riky.museek.classes.DBManager
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_edit_profile.view.*
import java.util.*

class EditProfileFragment : Fragment() {

    private var pickedPhotoUri : Uri? = null
    var photoId : String? = ""
    private var alertDialog : AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        if (context != null) DBManager.verifyLoggedUser(context!!)

        setListeners(view)

        alertDialog = AlertDialogInflater.inflateLoadingDialog(context!!, AlertDialogInflater.GREY)

        fetchMyProfileFromDatabase(view)

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d(EditProfileFragment::class.java.name, "Photo was picked")
            pickedPhotoUri = data.data
            photoPickerButtonEditProfile.alpha = 0f
            imageViewFrameEditProfile.alpha = 1f
            imageViewEditProfile.setImageURI(pickedPhotoUri)
        }
    }

    private fun fetchMyProfileFromDatabase(view: View) {

        val uid = FirebaseAuth.getInstance().uid

        val ref1 = FirebaseDatabase.getInstance().getReference("/users/$uid")

        ref1.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(user: DataSnapshot) {
                if (user.exists()) {
                    view.firstnameEditTextEditProfile.setText(user.child("firstname").value as String)
                    view.lastnameEditTextEditProfile.setText(user.child("lastname").value as String)
                    view.phoneEditTextEditProfile.setText(user.child("phone").value as String)

                    photoId = user.child("photoId").value as String

                    if (photoId != "" && photoId != null) {
                        val ref2 = FirebaseStorage.getInstance().getReference("/images/users/")
                        ref2.child(photoId!!).getBytes(4 * 1024 * 1024)
                            .addOnSuccessListener { bytes ->
                                view.photoPickerButtonEditProfile.alpha = 0f
                                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                view.imageViewEditProfile.setImageBitmap(bitmap)
                                alertDialog!!.dismiss()
                            }
                    }
                    else {
                        alertDialog!!.dismiss()
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(EditProfileFragment::class.java.name, "ERROR on Database: ${databaseError.message}")
            }
        })
    }

    private fun performUpdateProfile() {

        val uid = FirebaseAuth.getInstance().uid

        if (photoId == null) photoId = ""

        if (context != null) DBManager.verifyLoggedUser(context!!)

        val firstname = firstnameEditTextEditProfile.text.toString().trim()
        val lastname = lastnameEditTextEditProfile.text.toString().trim()
        val phone = phoneEditTextEditProfile.text.toString().trim()

        if (firstname.isEmpty() || lastname.isEmpty()) {
            Toast.makeText(activity, "I campi 'Nome' e 'Cognome' sono obbligatori!.", Toast.LENGTH_LONG).show()
            return
        }

        var isSuccess = true

        val user = FirebaseDatabase.getInstance().getReference("/users/$uid/")

        Log.d(EditProfileFragment::class.java.name, "MODIFICO I CAMPI DEL DATABASE")
        user.child("firstname").setValue(firstname)
            .addOnFailureListener {
                isSuccess = false
            }
        user.child("lastname").setValue(lastname)
            .addOnFailureListener {
                isSuccess = false
            }
        user.child("phone").setValue(phone)
            .addOnFailureListener {
                isSuccess = false
            }

        if (pickedPhotoUri != null) {
            if (photoId == "") photoId = "photo-" + UUID.randomUUID().toString()
            DBManager.uploadPickedPhotoOnStorage(pickedPhotoUri!!, "users/$photoId", context!!)
            user.child("photoId").setValue(photoId)
                .addOnFailureListener {
                    isSuccess = false
                }
        }

        if(isSuccess)
            Toast.makeText(activity, "Profilo aggiornato con successo!", Toast.LENGTH_LONG).show()
        else {
            Toast.makeText(activity, "Qualcosa è andato storto durante l'aggiornamento del profilo. Riprova", Toast.LENGTH_LONG).show()
            return
        }

        fragmentManager!!.popBackStack()
        fragmentManager!!.beginTransaction().replace(R.id.fragment, MyProfileFragment()).commit()
    }

    private fun setListeners(view: View) {

        view.homeButtonEditProfile.setOnClickListener {
            hideKeyboard(view)
            val intentHomepage = Intent(activity, HomepageActivity::class.java)
            intentHomepage.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentHomepage)
        }

        view.photoPickerButtonEditProfile.setOnClickListener {
            hideKeyboard(view)
            val intentPicker = Intent(Intent.ACTION_PICK)
            intentPicker.type = "image/*"
            startActivityForResult(intentPicker, 0)
        }

        view.updateButtonMyProfile.setOnClickListener {
            hideKeyboard(view)
            performUpdateProfile()
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