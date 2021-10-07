package com.bookies.register.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.bookies.register.R
import com.bookies.register.activities.TeacherActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase

class FireBaseUtils {
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
        fun handleDeviceOffline(exception:Exception,context: Context){
            if (exception.message?.contains("offline") == true) {
                Toast.makeText(context, R.string.offline_message, Toast.LENGTH_LONG)
                    .show()
            }
        }
        fun handleFailure(context: Context){
            Toast.makeText(
                context,
                R.string.failed,
                Toast.LENGTH_SHORT
            ).show()
        }
    }


}