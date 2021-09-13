package com.bookies.register

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.Toast

class ProgressCircle(val context: Context) {
    lateinit var alertDialog: AlertDialog

    init {
        build()
    }

    fun show() {
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

            .create()
        alertDialog.setCanceledOnTouchOutside(false)

    }

    fun onClose(java: Class<*>) {
        Toast.makeText(context, "Progress cancelled", Toast.LENGTH_LONG).show()
        context.startActivity(
            Intent(context, java)
        )

    }
}