package com.bookies.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SplashActivity : AppCompatActivity() {
    lateinit var state:Store
    val db= Firebase.firestore
    private val PASSCODE_COLLECTION_PATH:String="passcodes"
    private val CLASS_CODE_DOCUMENT_NAME:String="class_codes"
    private val TAG:String="ClassCode_Document"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        state=Store(applicationContext)
        makeDialog()
    }
    //checks if user is already logged in
    private fun checkLogin(): Boolean {
        return state getBooleanValue "login"
    }
    private fun makeDialog(){

        if(checkLogin()){
            Toast.makeText(applicationContext,"Logged in",Toast.LENGTH_SHORT).show()
        }
        else{
            createMaterialInputDialog()
        }
    }
    private fun createMaterialInputDialog(){
        MaterialDialog(this@SplashActivity).show {
            input(hintRes = R.string.login_dialog_hint_text,maxLength = 5){dialog,passCode->
                handleLogin(passCode.toString())
            }
            positiveButton(R.string.login_dialog_postive_button_text)
        }
    }

    private fun handleLogin(classCode:String){
        val classCodes=db.collection(PASSCODE_COLLECTION_PATH).document(CLASS_CODE_DOCUMENT_NAME)
        classCodes.get()
            .addOnSuccessListener { document->
                if (document != null) {
                    verifyAndGetClassCode(document,classCode)
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener{exception->
                Log.d(TAG, exception.toString())
            }

    }

    private fun verifyAndGetClassCode(document: DocumentSnapshot,classCode: String) {
        if(document.contains(classCode)){
            val className:String = document.get(classCode).toString()
            state.addValue("class",className)
            Log.d(TAG, state getStringValue "class"?:"null")
            Log.d(TAG,className)

            makeIntentToTeacherActivity()


        }
        else{
            Toast.makeText(this@SplashActivity,R.string.failed_class_code_verification,Toast.LENGTH_LONG).show()
            makeIntentToTeacherActivity()
        }
    }


    private fun makeIntentToTeacherActivity(){
       startActivity(Intent(this@SplashActivity,TeacherActivity::class.java))
    }

}