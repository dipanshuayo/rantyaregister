package com.bookies.register

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SplashActivity : AppCompatActivity() {
    lateinit var state: Store
    val db = FireBaseUtils().db
    private val TAG: String = "ClassCodeDocument"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        state = Store(applicationContext)
        //clearStore()
        //checkInternetConnection()
        makeDialog()

    }
//    @SuppressLint("CheckResult")
//    private fun checkInternetConnection(){
//        val single:Single<Boolean> = ReactiveNetwork.checkInternetConnectivity()
//        single
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe { isConnectedToInternet->
//                if(!isConnectedToInternet){
//                    Toast.makeText(applicationContext,R.string.failed_class_code_verification,Toast.LENGTH_LONG).show()
//                }
//                else{
//                    makeDialog()
//                }
//            }
//    }


    private fun clearStore() {
        state.Storeeditor.clear()
    }

    //checks if user is already logged in
    private fun checkLogin(): Boolean {
        return state getBooleanValue "login"
    }

    private fun makeDialog() {
        if (checkLogin()) {
            Toast.makeText(applicationContext, "Your Already Logged in", Toast.LENGTH_SHORT).show()
            makeIntentToTeacherActivity()
        } else {
            createMaterialInputDialog()
        }
    }

    private fun createMaterialInputDialog() {
        MaterialDialog(this@SplashActivity).show {
            input(hintRes = R.string.login_dialog_hint_text, maxLength = 5) { _, passCode ->
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
                    setIsStudentNameAdded()
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                //handles if device offline
                if (exception.message?.contains("offline") == true) {
                    Toast.makeText(applicationContext, R.string.offline_message, Toast.LENGTH_LONG)
                        .show()
                }
                Log.d(TAG, exception.toString())
            }
    }

    private fun setIsStudentNameAdded() {
        val className=state getStringValue "class"
        val classDoc=db.collection(Constants.CLASSES_COLLECTION_PATH).document(className)
            .get()
            .addOnSuccessListener { document->
                if(document.contains("is_students_added") && document.getBoolean("is_students_added") == true){
                    state.addValue("isStudentNameAdded",true)
                }
                else{
                    state.addValue("isStudentNameAdded",false)
                }
            }
    }

    private fun verifyAndGetClassCode(document: DocumentSnapshot, classCode: String) {
        if (document.contains(classCode)) {
            val className: String = document.get(classCode).toString()
            state.addValue("class", className)
            state.addValue("login", true)
            Log.d(TAG, state getStringValue "class")
            Log.d(TAG, className)
            makeIntentToTeacherActivity()
        } else {
            Toast.makeText(
                this@SplashActivity,
                R.string.failed_class_code_verification,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun makeIntentToTeacherActivity() {
        startActivity(Intent(this@SplashActivity, TeacherActivity::class.java))
    }
}