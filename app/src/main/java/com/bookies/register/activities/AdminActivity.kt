package com.bookies.register.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.bookies.register.R
import com.bookies.register.utils.Constants
import com.bookies.register.utils.FireBaseUtils
import com.bookies.register.utils.ProgressCircle
import com.bookies.register.utils.Store
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.getField
import kotlinx.android.synthetic.main.activity_about_our_set.*
import kotlinx.android.synthetic.main.activity_admin.*
import kotlinx.android.synthetic.main.student_name_item.*

class AdminActivity : AppCompatActivity() {
    private lateinit var state: Store
    private lateinit var progress: ProgressCircle
    private lateinit var currentTerm: String
    private var db = FireBaseUtils().db
    private val termsArray: Array<String> = arrayOf("first_term", "second_term", "third_term")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        state = Store(applicationContext)
        progress = ProgressCircle(this@AdminActivity, false)
        getTerm()
    }

    private fun getTerm() {
        progress.show()
        if (state.getStringValue("term") == "null") {
            db.collection(Constants.ADMIN_COLLECTION_PATH)
                .document(Constants.TERM_INFO_DOCUMENT_NAME)
                .get()
                .addOnSuccessListener { document ->
                    if (document.contains("term")) {
                        currentTerm = document.get("term") as String
                        state.addValue("term", currentTerm)
                        setOnClickListenersToButtons()
                        progress.dismiss()

                    }
                }
                .addOnFailureListener {
                    FireBaseUtils.handleFailure(this@AdminActivity)
                }
        } else {
            currentTerm = state.getStringValue("term")
            progress.dismiss()
            setOnClickListenersToButtons()
        }

    }

    private fun setOnClickListenersToButtons() {
        setChangeTermButtonOnClick()
        setChangeAcademicYearButtonOnClick()
        setChangeClassCodeTermButtonOnClick()
    }

    private fun setChangeClassCodeTermButtonOnClick() {
        change_class_code_button.setOnClickListener {
            db.collection(Constants.PASSCODE_COLLECTION_PATH)
                .document(Constants.CLASS_CODE_DOCUMENT_NAME)
                .get()
                .addOnCompleteListener { classData ->
                    makeClassSelectorDialog(classData.result)
                }
                .addOnFailureListener {
                    FireBaseUtils.handleFailure(applicationContext)
                }
            // handleOnChangeClassCodeButton(classData)
        }
    }

    private fun makeClassSelectorDialog(classData: DocumentSnapshot?) {
        val classNames = classData?.get(Constants.CLASS_LIST_FIELD_NAME) as ArrayList<String>
        MaterialDialog(this@AdminActivity).show {
            listItemsSingleChoice(items = classNames) { dialog, _, text ->
                handleOnChangeClassCodeButton(classData, text.toString())
                dialog.dismiss()
            }
            positiveButton(text = "Next")
        }

    }

    private fun successToast() {
        Toast.makeText(applicationContext, "Done", Toast.LENGTH_LONG).show()
    }

    private fun handleOnChangeClassCodeButton(classData: DocumentSnapshot?, className: String) {
        MaterialDialog(this@AdminActivity).show {
            title(text = "Enter class code for $className")
            input(hint = "Enter class code", maxLength = 5) { dialog, text ->
                val truePassWord: String? =
                    classData?.data?.entries?.find { it.value == className }?.key
                Log.d("PassWord", "truepassword $truePassWord edit password $text")
                if (text.toString() == truePassWord) {
                    dialog.dismiss()
                    makeNewClassCodeDialog(className, truePassWord)

                } else {
                    Toast.makeText(applicationContext, "Wrong Password", Toast.LENGTH_LONG).show()
                }
            }
            positiveButton(res = R.string.login_dialog_postive_button_text)
        }
    }

    private fun makeNewClassCodeDialog(className: String, oldClassCode: String) {

        MaterialDialog(this@AdminActivity).show {
            title(text = "Enter new class code for $className")
            input(hint = "Enter new class code", maxLength = 5) { materialDialog, charSequence ->
                progress.show()
                changePassCode(
                    className = className,
                    classCode = charSequence.toString(),
                    oldClassCode = oldClassCode
                )
            }
            positiveButton(res = R.string.login_dialog_postive_button_text)
        }
    }

    private fun changePassCode(className: String, classCode: String, oldClassCode: String) {
        val doc = db.collection(Constants.PASSCODE_COLLECTION_PATH)
            .document(Constants.CLASS_CODE_DOCUMENT_NAME)

        doc.update(mapOf(oldClassCode to FieldValue.delete())).addOnCompleteListener {
            doc.set(mapOf(classCode to className), SetOptions.merge()).addOnCompleteListener {
                successToast()
                progress.dismiss()
            }

        }
    }

    private fun setChangeAcademicYearButtonOnClick() {
        new_academic_year_button.setOnClickListener {
            createWarningDialog()
        }

    }


    private fun createWarningDialog() {
        MaterialDialog(this@AdminActivity).show {
            title(res = R.string.new_academic_dialog_title)
            message(res = R.string.new_academic_dialog_message)
            positiveButton(res = R.string.start_academic_year_text) {
                progress.show()
                handleOnNewAcademicSession()
            }
        }
    }

    private fun handleOnNewAcademicSession() {
        if (currentTerm[0].toString() != "t") {
            Toast.makeText(
                applicationContext,
                "Term must be third in order to start new academic session",
                Toast.LENGTH_LONG
            ).show()
            progress.dismiss()
        } else {
            progress.show()
            val adminDoc = db.collection(Constants.ADMIN_COLLECTION_PATH)
                .document(Constants.TERM_INFO_DOCUMENT_NAME)
            val datesDoc = db.collection(currentTerm)
            val studentsDoc = db.collection(Constants.CLASSES_COLLECTION_PATH)
            db.runTransaction { transaction ->
                transaction.update(adminDoc, "term", "first_term")
                datesDoc.get().addOnSuccessListener { documents ->
                    for (document in documents) {
                        db.runBatch { batch ->
                            batch.delete(datesDoc.document(document.id))
                        }
                    }
                }
            }.addOnCompleteListener {
                deleteOtherTerms()
            }.addOnCompleteListener {
                deleteStudentSubCollection(studentsDoc)
            }.addOnCompleteListener {
                progress.dismiss()
                successToast()
            }
                .addOnFailureListener {
                    Log.d("admin", it.toString())
                    FireBaseUtils.handleFailure(applicationContext)
                }
        }
    }

    private fun deleteOtherTerms() {
        val firstTermDates = db.collection(termsArray[0])
        val secondTermDates = db.collection(termsArray[1])
        firstTermDates.get().addOnSuccessListener { documents ->
            for (document in documents) {
                if (document.exists()) {
                    db.runBatch { batch ->
                        batch.delete(firstTermDates.document(document.id))
                    }
                }
            }
        }.addOnCompleteListener {
            secondTermDates.get().addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document.exists()) {
                        db.runBatch { batch ->
                            batch.delete(secondTermDates.document(document.id))
                        }
                    }
                }
            }
        }
    }


    private fun deleteStudentSubCollection(studentsDoc: CollectionReference) {
        studentsDoc.get().addOnSuccessListener { documents ->
            for (document in documents) {
                if (!document.data.isNullOrEmpty()) {
                    studentsDoc
                        .document(document.id)
                        .update(Constants.STUDENT_NAMES_ARRAY_FIELD_NAME, FieldValue.delete())
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

    private fun setChangeTermButtonOnClick() {
        val newTerm = termsArray[getNextIndex(currentTerm)]
        change_term_button.text = "change to ${newTerm.replace("_", " ")}"
        change_term_button.setOnClickListener {
            handleOnChangeTermButtonClick(newTerm)
        }
    }

    private fun getNextIndex(string: String): Int {
        return if (termsArray.indexOf(string) == termsArray.lastIndex) {
            0
        } else {
            termsArray.indexOf(string) + 1
        }
    }

    private fun handleOnChangeTermButtonClick(newTerm: String) {
        progress.show()
        db.collection(Constants.ADMIN_COLLECTION_PATH)
            .document(Constants.TERM_INFO_DOCUMENT_NAME)
            .update("term", newTerm)
            .addOnSuccessListener {
                progress.dismiss()
                state.addValue("term", newTerm)
                successToast()
                finishAffinity()
                finish()
            }
            .addOnFailureListener { FireBaseUtils.handleFailure(applicationContext) }
    }

}
