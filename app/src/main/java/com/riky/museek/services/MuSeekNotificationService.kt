package com.riky.museek.services

import android.app.*
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.riky.museek.R
import com.riky.museek.activities.HomepageActivity

class MuSeekNotificationService : Service() {

    private val CHANNEL_ID = "i.apps.notifications"
    private val CHANNEL_DESCRIPTION = "Test notification"

    override fun onCreate() {

        notifySend()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {

    }

    private fun notifySend() {

        val uid = FirebaseAuth.getInstance().uid ?: ""

        val ref = FirebaseDatabase.getInstance().getReference("/instrument_purchased_ads/")

        ref.addChildEventListener(object : ChildEventListener {

            override fun onCancelled(error: DatabaseError) {
                //do nothing
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                //do nothing
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                if (uid == snapshot.child("buyeruid").value.toString() &&
                    snapshot.child("send").value.toString().toBoolean() &&
                    !snapshot.child("sendNotified").value.toString().toBoolean()) {

                    ref.child(snapshot.child("aid").value.toString()).child("sendNotified").setValue(true)
                    promptNotify(
                        "TICKER", "Strumento Spedito!", "Il tuo pacco contenente " +
                            "${snapshot.child("brand").value.toString()} ${snapshot.child("model").value.toString()} è stato spedito.")
                }
                if (uid == snapshot.child("selleruid").value.toString() &&
                    !snapshot.child("soldNotified").value.toString().toBoolean()) {

                    ref.child(snapshot.child("aid").value.toString()).child("soldNotified").setValue(true)
                    promptNotify(
                        "TICKER",
                        "Strumento Venduto!",
                        "Il tuo annuncio contenente ${snapshot.child("brand").value.toString()} ${snapshot.child("model").value.toString()} è stato acquistato."
                    )
                }
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                //do nothing
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                //do nothing
            }
        })
    }

    private fun promptNotify(ticker: String, title: String, text: String) {

        val intentHomepage = Intent(this, HomepageActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getActivity(this, 0, intentHomepage, flags)

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val notification : Notification

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_DESCRIPTION, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(false)
            manager.createNotificationChannel(notificationChannel)
            notification = NotificationCompat
                .Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_logo)
                .setTicker(ticker)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

        }
        else {
            notification = NotificationCompat
                .Builder(this)
                .setSmallIcon(R.drawable.notification_logo)
                .setTicker(ticker)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
        }

        val NOTIFICATION_ID = 1
        manager.notify(NOTIFICATION_ID, notification)
    }
}