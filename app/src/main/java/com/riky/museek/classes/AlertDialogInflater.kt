package com.riky.museek.classes

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import com.riky.museek.R
import kotlinx.android.synthetic.main.loading_popup_blue.*
import kotlinx.android.synthetic.main.password_popup_blue.*

class AlertDialogInflater {

    companion object {

        val BLUE = 0
        val RED = 1
        val GREY = 2

        fun inflateLoadingDialog (context: Context, color: Int) : AlertDialog {

            val animation = RotateAnimation(
                0.0f,
                360.0f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            )
            animation.interpolator = LinearInterpolator()
            animation.repeatCount = Animation.INFINITE
            animation.duration = 700

            val dialogView =
                when (color) {
                    BLUE -> LayoutInflater.from(context).inflate(R.layout.loading_popup_blue, null)
                    GREY -> LayoutInflater.from(context).inflate(R.layout.loading_popup_grey, null)
                    //RED -> LayoutInflater.from(context).inflate(R.layout.loading_popup_red, null)
                    else -> LayoutInflater.from(context).inflate(R.layout.loading_popup_blue, null)
                }

            val mBuilder = AlertDialog.Builder(context).setView(dialogView)
            val alertDialog = mBuilder.show()

            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.loadingImageView.startAnimation(animation)

            return alertDialog
        }

        fun inflatePasswordDialog (context: Context, color: Int) : AlertDialog {

            val dialogView =
                when (color) {
                    //BLUE -> LayoutInflater.from(context!!).inflate(R.layout.password_popup_blue, null)
                    GREY -> LayoutInflater.from(context!!).inflate(R.layout.password_popup_grey, null)
                    //RED -> LayoutInflater.from(context!!).inflate(R.layout.password_popup_red, null)
                    else -> LayoutInflater.from(context!!).inflate(R.layout.password_popup_grey, null)
                }

            val mBuilder = AlertDialog.Builder(context).setView(dialogView)

            return mBuilder.show()
        }

        fun inflateConfirmDeleteDialog (context: Context, color: Int) : AlertDialog {

            val dialogView =
                when (color) {
                    BLUE -> LayoutInflater.from(context).inflate(R.layout.confirm_delete_popup_blue, null)
                    //GREY -> LayoutInflater.from(context).inflate(R.layout.confirm_delete_popup_grey, null)
                    //RED -> LayoutInflater.from(context).inflate(R.layout.confirm_delete_popup_red, null)
                    else -> LayoutInflater.from(context).inflate(R.layout.confirm_delete_popup_blue, null)
                }
            val mBuilder = AlertDialog.Builder(context).setView(dialogView)
            return mBuilder.show()
        }

        fun inflateConfirmPurchaseDialog (context: Context, color: Int) : AlertDialog {

            val dialogView =
                when (color) {
                    BLUE -> LayoutInflater.from(context).inflate(R.layout.confirm_purchase_popup_blue, null)
                    //GREY -> LayoutInflater.from(context).inflate(R.layout.confirm_purchase_popup_grey, null)
                    //RED -> LayoutInflater.from(context).inflate(R.layout.confirm_purchase_popup_red, null)
                    else -> LayoutInflater.from(context).inflate(R.layout.confirm_purchase_popup_blue, null)
                }
            val mBuilder = AlertDialog.Builder(context).setView(dialogView)
            return mBuilder.show()
        }
    }
}