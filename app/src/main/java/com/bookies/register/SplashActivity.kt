package com.bookies.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input

class SplashActivity : AppCompatActivity() {
    lateinit var state:Store
    val s:String="\"jdfkj\""
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
    //ayo this is your place do your functions in this function
    private fun handleLogin(passCode:String){
        //TODO("AYO IMPLEMENT THE CHECK")
        makeIntentToTeacherActivity()
    }
    private fun makeIntentToTeacherActivity(){
       startActivity(Intent(this@SplashActivity,TeacherActivity::class.java))
    }

}