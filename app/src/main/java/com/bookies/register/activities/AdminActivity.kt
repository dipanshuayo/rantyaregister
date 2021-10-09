package com.bookies.register.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.bookies.register.R
import com.bookies.register.utils.Constants
import com.bookies.register.utils.FireBaseUtils
import com.bookies.register.utils.ProgressCircle
import com.bookies.register.utils.Store
import com.google.firebase.firestore.FieldValue
import kotlinx.android.synthetic.main.activity_about_our_set.*
import kotlinx.android.synthetic.main.activity_admin.*
import kotlinx.android.synthetic.main.student_name_item.*

class AdminActivity : AppCompatActivity() {
   private lateinit var state:Store
    private  lateinit var progress:ProgressCircle
    private lateinit var currentTerm:String
    private var db=FireBaseUtils().db
    private val termsArray:Array<String> = arrayOf("first_term","second_term","third_term")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        state= Store(applicationContext)
        progress= ProgressCircle(this@AdminActivity)
        getTerm()
    }

    private fun getTerm() {
        progress.show()
        if(state.getStringValue("term")=="null") {
            db.collection(Constants.ADMIN_COLLECTION_PATH)
                .document(Constants.TERM_INFO_DOCUMENT_NAME)
                .get()
                .addOnSuccessListener { document ->
                    if (document.contains("term")) {
                        currentTerm= document.get("term") as String
                        setOnClickListenersToButtons()

                    }
                }
                .addOnFailureListener {
                    FireBaseUtils.handleFailure(this@AdminActivity)
                }
        }
        else {
            currentTerm=state.getStringValue("term")
            setOnClickListenersToButtons()
        }
        progress.dismiss()
    }

    private fun setOnClickListenersToButtons() {
        setChangeTermButtonOnClick()
        setChangeAcademicYearButtonOnClick()
        setChangeClassCodeTermButtonOnClick()
    }
    private fun setChangeClassCodeTermButtonOnClick() {

    }
    private fun setChangeAcademicYearButtonOnClick() {
        new_academic_year_button.setOnClickListener {
            createWarningDialog()
        }

    }

    private fun createWarningDialog() {
        MaterialDialog(this@AdminActivity).show {
            title(res = R.string.new_academic_dialog_title)
            message(res=R.string.new_academic_dialog_message)
            positiveButton(res = R.string.start_academic_year_text){
                handleOnNewAcademicSession()
            }
        }
    }

    private fun handleOnNewAcademicSession() {
        val adminDoc = db.collection(Constants.ADMIN_COLLECTION_PATH)
            .document(Constants.TERM_INFO_DOCUMENT_NAME)
        val datesDoc = db.collection(currentTerm)
        val studentsDoc = db.collection(Constants.CLASSES_COLLECTION_PATH)
        db.runTransaction { transaction ->
            transaction.update(adminDoc, "term", "first_term")
            datesDoc.get().addOnSuccessListener { documents ->
                for (document in documents) {
                   db.runBatch {batch->
                       batch.delete(datesDoc.document(document.id))
                   }
                }
            }

        }.addOnCompleteListener{
            Toast.makeText(applicationContext,"yesyes yes yes ",Toast.LENGTH_LONG).show()
            studentsDoc.get().addOnSuccessListener { documents ->
                for (document in documents) {
                    if (!document.data.isNullOrEmpty()) {
                        studentsDoc
                            .document(document.id)
                            .update(Constants.STUDENT_NAMES_ARRAY_FIELD_NAME,FieldValue.delete())
                        studentsDoc
                            .document(document.id)
                            .collection(Constants.STUDENTS_COLLECTION_PATH)
                            .get()
                            .addOnSuccessListener { students ->
                                for (student in students) {
                                    studentsDoc
                                        .document(document.id)
                                        .collection(Constants.STUDENTS_COLLECTION_PATH)
                                        .document(student.id)
                                        .delete()
                                }
                            }

                    }

                }
            }
        }
            .addOnFailureListener {
                Log.d("admin",it.toString())
                FireBaseUtils.handleFailure(applicationContext)
            }
    }
//    db.runBatch { batch->
//        batch.update(adminDoc,"term","first_term")
//        datesDoc.get().addOnSuccessListener { documents->
//            for(document in documents){
//                batch.delete(datesDoc.document(document.id))
//            }
//        }
//    }
//    studentsDoc.get().addOnSuccessListener { documents ->
//        for (document in documents) {
//            if (!document.data.isNullOrEmpty()) {
//                studentsDoc
//                    .document(document.id)
//                    .collection(Constants.STUDENTS_COLLECTION_PATH)
//                    .get()
//                    .addOnSuccessListener {students->
//                        for(student in students) {
//                            document.
//                        }
//                    }
//            }
//        }
//    }
//
//
//}

    private fun setChangeTermButtonOnClick() {
        val newTerm=termsArray[getNextIndex(currentTerm)]
        Toast.makeText(applicationContext,newTerm,Toast.LENGTH_LONG).show()
        change_term_button.text = newTerm.replace("_"," ")
            change_term_button.setOnClickListener {
                handleOnChangeTermButtonClick(newTerm)
            }
        }
    private fun getNextIndex(string: String):Int{
        return if(termsArray.indexOf(string)==termsArray.lastIndex){
            0
        } else{
            termsArray.indexOf(string)+1
        }
    }
    private fun handleOnChangeTermButtonClick(newTerm: String) {
        progress.show()
      db.collection(Constants.ADMIN_COLLECTION_PATH)
          .document(Constants.TERM_INFO_DOCUMENT_NAME)
          .update("term",newTerm)
          .addOnSuccessListener { progress.dismiss() }
          .addOnFailureListener { FireBaseUtils.handleFailure(applicationContext) }
    }


}