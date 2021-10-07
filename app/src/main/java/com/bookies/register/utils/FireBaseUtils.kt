package com.bookies.register.utils

import android.content.Context
import android.content.Intent
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
    }


}