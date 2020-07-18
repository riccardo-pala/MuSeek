package com.riky.museek.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.riky.museek.R
import com.riky.museek.activities.HomepageActivity
import com.riky.museek.classes.AlertDialogInflater
import kotlinx.android.synthetic.main.choose_band_ad_popup_red.*
import kotlinx.android.synthetic.main.choose_popup_red.*
import kotlinx.android.synthetic.main.fragment_band.view.*

class BandFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_band, container, false)

        view.homeButtonBand.setOnClickListener {
            val intentHomepage = Intent(activity, HomepageActivity::class.java)
            intentHomepage.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentHomepage)
        }

        view.newAdButtonBand.setOnClickListener {
            var alertDialog = AlertDialogInflater.inflateLoadingDialog(context!!, AlertDialogInflater.RED)

            alertDialog.dismiss()

            alertDialog = AlertDialogInflater.inflateChooseBandAdDialog(context!!, AlertDialogInflater.RED)

            alertDialog.bandButtonChooseBandAdPopup.setOnClickListener {
                alertDialog.dismiss()
                fragmentManager!!.beginTransaction()
                    .replace(R.id.fragment, NewBandAdBandFragment())
                    .addToBackStack(null).commit()
            }

            alertDialog.memberButtonChooseBandAdPopup.setOnClickListener {
                alertDialog.dismiss()
                fragmentManager!!.beginTransaction()
                    .replace(R.id.fragment, NewMemberAdBandFragment())
                    .addToBackStack(null).commit()
            }
        }

        view.myProfileButtonBand.setOnClickListener {
            fragmentManager!!.beginTransaction().replace(R.id.fragment, MyProfileBandFragment()).addToBackStack(this.javaClass.name).commit()
        }

        view.bandSearchButtonBand.setOnClickListener {

            val alertDialog = AlertDialogInflater.inflateChooseDialog(context!!, AlertDialogInflater.RED)

            alertDialog.submitButtonChoosePopup.setOnClickListener submitListener@ {

                alertDialog.regionSpinnerChoosePopupBand.setBackgroundResource(R.drawable.shadow_spinner)
                alertDialog.musicianSpinnerChoosePopupBand.setBackgroundResource(R.drawable.shadow_spinner)

                val regionSearch = alertDialog.regionSpinnerChoosePopupBand.selectedItemPosition
                val musicianSearch = alertDialog.musicianSpinnerChoosePopupBand.selectedItemPosition

                var isEmptyFields = false

                if (regionSearch == 0) {
                    alertDialog.regionSpinnerChoosePopupBand.setBackgroundResource(R.drawable.shadow_spinner_error)
                    isEmptyFields = true
                }
                if (musicianSearch == 0) {
                    alertDialog.musicianSpinnerChoosePopupBand.setBackgroundResource(R.drawable.shadow_spinner_error)
                    isEmptyFields = true
                }
                if (isEmptyFields) {
                    Toast.makeText(context, "Si prega di compilare correttamente tutti i campi del form.", Toast.LENGTH_LONG).show()
                    return@submitListener
                }

                val fragment = ShowMemberAdsBandFragment()
                val args = Bundle()
                args.putInt("regionSearch", regionSearch)
                args.putInt("musicianSearch", musicianSearch)
                alertDialog.dismiss()
                fragment.arguments = args
                fragmentManager!!.beginTransaction().replace(R.id.fragment, fragment).addToBackStack(null).commit()
            }
        }

        view.memberSearchButtonBand.setOnClickListener {

            val alertDialog = AlertDialogInflater.inflateChooseDialog(context!!, AlertDialogInflater.RED)

            alertDialog.submitButtonChoosePopup.setOnClickListener submitListener@ {

                alertDialog.regionSpinnerChoosePopupBand.setBackgroundResource(R.drawable.shadow_spinner)
                alertDialog.musicianSpinnerChoosePopupBand.setBackgroundResource(R.drawable.shadow_spinner)

                val regionSearch = alertDialog.regionSpinnerChoosePopupBand.selectedItemPosition
                val musicianSearch = alertDialog.musicianSpinnerChoosePopupBand.selectedItemPosition

                var isEmptyFields = false

                if (regionSearch == 0) {
                    alertDialog.regionSpinnerChoosePopupBand.setBackgroundResource(R.drawable.shadow_spinner_error)
                    isEmptyFields = true
                }
                if (musicianSearch == 0) {
                    alertDialog.musicianSpinnerChoosePopupBand.setBackgroundResource(R.drawable.shadow_spinner_error)
                    isEmptyFields = true
                }
                if (isEmptyFields) {
                    Toast.makeText(context, "Si prega di compilare correttamente tutti i campi del form.", Toast.LENGTH_LONG).show()
                    return@submitListener
                }

                val fragment = ShowBandAdsBandFragment()
                val args = Bundle()
                args.putInt("regionSearch", regionSearch)
                args.putInt("musicianSearch", musicianSearch)
                alertDialog.dismiss()
                fragment.arguments = args
                fragmentManager!!.beginTransaction().replace(R.id.fragment, fragment).addToBackStack(null).commit()
            }
        }

        return view
    }
}