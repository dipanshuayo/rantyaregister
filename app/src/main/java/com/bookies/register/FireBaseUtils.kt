package com.bookies.register

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.FirestoreRegistrar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase

class FireBaseUtils() {
    val db = Firebase.firestore

    init {
        val settings = firestoreSettings {
            isPersistenceEnabled = false
        }
        db.firestoreSettings = settings
    }

    companion object {
        fun gotoTeacherActivity(context: Context) {
            context.startActivity(
                Intent(context, TeacherActivity::class.java)
            )
        }
    }


}