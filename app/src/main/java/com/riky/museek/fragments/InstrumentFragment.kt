package com.riky.museek.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.riky.museek.R
import com.riky.museek.activities.HomepageActivity
import com.riky.museek.classes.AlertDialogInflater
import com.riky.museek.classes.DBManager
import kotlinx.android.synthetic.main.fragment_instrument.view.*

class InstrumentFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_instrument, container, false)

        view.homeButtonInstr.setOnClickListener {
            val intentHomepage = Intent(activity, HomepageActivity::class.java)
            intentHomepage.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentHomepage)
        }

        view.newAdButtonInstr.setOnClickListener {
            val alertDialog = AlertDialogInflater.inflateLoadingDialog(context!!, AlertDialogInflater.BLUE)

            DBManager.verifyInstrumentUser(context!!, alertDialog, null)
        }

        view.myProfileButtonInstr.setOnClickListener {
            fragmentManager!!.beginTransaction().replace(R.id.fragment, MyProfileInstrumentFragment()).addToBackStack(this.javaClass.name).commit()
        }

        var searchValue = ""

        view.searchViewInstr.setOnQueryTextListener(object :  SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                searchValue = newText
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                search(0, searchValue)
                return false
            }
        })

        view.searchButtonInstr.setOnClickListener {
            search(0, searchValue)
        }

        view.catWindsButtonInstr.setOnClickListener {
            search(1, "F")
        }
        view.catStringsButtonInstr.setOnClickListener {
            search(1, "C")
        }
        view.catKeyboardsButtonInstr.setOnClickListener {
            search(1, "T")
        }
        view.catArchesButtonInstr.setOnClickListener {
            search(1, "A")
        }

        return view
    }

    fun search(searchType: Int, searchValue: String) {

        if (searchValue == "" || searchValue == null) {
            Toast.makeText(context, "Si prega di inserire almeno un carattere nel campo!", Toast.LENGTH_LONG).show()
            return
        }
        val fragment = ShowAdsInstrumentFragment()
        val args = Bundle()
        args.putInt("searchType", searchType)
        args.putString("searchValue", searchValue)
        fragment.arguments = args
        fragmentManager!!.beginTransaction().replace(R.id.fragment, fragment).addToBackStack(this.javaClass.name).commit()
    }
}