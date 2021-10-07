package com.bookies.register

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast

class ProgressCircle(val context: Context) {
    lateinit var alertDialog: AlertDialog

    init {
        build()
    }

    fun show() {
        dismiss()
        alertDialog.show()

    }

    fun dismiss() {
        alertDialog.dismiss()
    }

    @SuppressLint("InflateParams")
    private fun build() {
        val progessView = LayoutInflater.from(context).inflate(R.layout.progess_dialog_view, null)
        alertDialog = AlertDialog.Builder(context)
            .setView(progessView)
            .setOnKeyListener { dialog, keyCode, event ->
                if(keyCode==KeyEvent.KEYCODE_BACK && event.action==KeyEvent.ACTION_UP){
                    dialog.cancel()
                    onClose(TeacherActivity::class.java)
                    return@setOnKeyListener true
                }
                return@setOnKeyListener false
            }
            .setCancelable(false)

            .create()



        alertDialog
            .setCanceledOnTouchOutside(false)

    }

    fun onClose(java: Class<*>) {
        Toast.makeText(context, "Progress cancelled", Toast.LENGTH_LONG).show()
        context.startActivity(
            Intent(context, java)
        )


    }
}