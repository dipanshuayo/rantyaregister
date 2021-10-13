package com.bookies.register.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.bookies.register.*
import com.bookies.register.utils.Constants
import com.bookies.register.utils.FireBaseUtils
import com.bookies.register.utils.ProgressCircle
import com.bookies.register.utils.Store
import com.google.firebase.firestore.DocumentSnapshot

class SplashActivity : AppCompatActivity() {
    private lateinit var state: Store
    private lateinit var progress: ProgressCircle
    private val db = FireBaseUtils().db
    private val TAG: String = "ClassCodeDocument"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        state = Store(applicationContext)
        progress= ProgressCircle(this@SplashActivity)
        makeDialog()
    }
    //checks if user is already logged in
    private fun checkLogin(): Boolean {
        return state getBooleanValue "login"
    }

    private fun makeDialog() {
        if (checkLogin()) {
            youAreAlreadyLoggedIn()
            makeIntentToTeacherActivity()
        } else {
            createMaterialInputDialog()
        }
    }

    private fun youAreAlreadyLoggedIn() {
        Toast.makeText(applicationContext, "Your Already Logged in", Toast.LENGTH_SHORT).show()
    }

    private fun createMaterialInputDialog() {
        MaterialDialog(this@SplashActivity).show {
            input(hintRes = R.string.login_dialog_hint_text, maxLength = 5) { _, passCode ->
                progress.show()
                handleLogin(passCode.toString())
            }
            cancelOnTouchOutside(true)
            positiveButton(R.string.login_dialog_postive_button_text)
        }

    }

    private fun handleLogin(classCode: String) {
        val classCodes = db.collection(Constants.PASSCODE_COLLECTION_PATH)
            .document(Constants.CLASS_CODE_DOCUMENT_NAME)
        classCodes.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    verifyAndGetClassCode(document, classCode)
                   // setIsStudentNameAdded()
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                //handles if device offline
                FireBaseUtils.handleDeviceOffline(exception,applicationContext)
                progress.dismiss()
                Log.d(TAG, exception.toString())
            }

    }

//    private fun setIsStudentNameAdded() {
//        val className=state getStringValue "class"
//        if(className != "null") {
//            db.collection(Constants.CLASSES_COLLECTION_PATH).document(className)
//                .get()
//                .addOnSuccessListener { document ->
//                    if (document.contains(Constants.STUDENT_NAMES_ARRAY_FIELD_NAME)) {
//                        state.addValue("isStudentNameAdded", true)
//                    } else {
//                        state.addValue("isStudentNameAdded", false)
//                    }
//
//                }
//        }
//    }


    private fun verifyAndGetClassCode(document: DocumentSnapshot, classCode: String){
        val admin="admin"
        if (document.contains(classCode)) {
            val className: String = document.get(classCode) as String
                state.addValue("class", className)
            if(isAdmin(className)){
                makeIntentToAdminActivity()
            }else {
                state.addValue("login", true)
                makeIntentToTeacherActivity()
                Log.d(TAG, state getStringValue "class")
                Log.d(TAG, className)
            }

        } else {
            Toast.makeText(
                this@SplashActivity,
                R.string.failed_class_code_verification,
                Toast.LENGTH_LONG
            ).show()
            progress.dismiss()
        }
    }
    private fun isAdmin(className:String):Boolean{
        return className.get(0).toString()=="a" && className.get(1).toString()=="d" && className.get(2).toString()=="m"  && className.get(3).toString()=="i" && className.get(4).toString()=="n"
    }
    private fun makeIntentToTeacherActivity() {
        progress.dismiss()
        startActivity(
            Intent(this@SplashActivity, TeacherActivity::class.java)
        )
        finish()
    }
    private fun makeIntentToAdminActivity(){
        progress.dismiss()
        startActivity(
            Intent(this@SplashActivity, AdminActivity::class.java)
        )
        finish()
    }
}