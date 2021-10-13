@file:Suppress("PrivatePropertyName")

package com.bookies.register.activities

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.bookies.register.*
import com.bookies.register.utils.Constants
import com.bookies.register.utils.FireBaseUtils
import com.bookies.register.utils.ProgressCircle
import com.bookies.register.utils.Store
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import kotlinx.android.synthetic.main.activity_students.*

class StudentsActivity : AppCompatActivity() {
    private lateinit var arrayAdapterForStudentsName: ArrayAdapter<String>
    private lateinit var state: Store
    private lateinit var className: String
    private lateinit var progress: ProgressCircle
    private val db = FireBaseUtils().db
    private val TAG = "StudentsActivityDocument"
    private var studentsNameArray:List<String> = listOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_students)
        state = Store(applicationContext)
        progress = ProgressCircle(this@StudentsActivity)
        className = state getStringValue "class"
        //the rest of the code check the db calls
        getStudentsName()
    }

    private fun setOnHoldClickListenerToListView() {
        students_list_view.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { _, _, position, _ ->
                handleOnLongClick(studentsNameArray[position])
                true
            }
    }

    private fun handleOnLongClick(name: String) {
        makeOnLongClickDeleteDialog(name)
    }

    private fun makeOnLongClickDeleteDialog(name: String) {
        MaterialDialog(this@StudentsActivity).show {
            title(
                text = setColorToText(
                    "Student $name is about to be deleted",
                    Color.RED
                ).toString()
            )
            message(
                res = R.string.delete_message
            )
            positiveButton(text = "Delete") {
                deleteStudent(name)
            }
        }
    }

    private fun setColorToText(text: String, color: Int): SpannableString {
        val spannableString = SpannableString(text)
        spannableString.setSpan(ForegroundColorSpan(color), 0, text.length - 1, 0)
        return spannableString
    }

    private fun deleteStudent(name: String) {
        progress.show()
        val classDoc = db.collection(Constants.CLASSES_COLLECTION_PATH).document(className)
        classDoc.update(Constants.STUDENT_NAMES_ARRAY_FIELD_NAME, FieldValue.arrayRemove(name))
            .addOnSuccessListener {
                arrayAdapterForStudentsName.remove(name)
                deleteStudentFromCollection(classDoc, name)
            }.addOnFailureListener {
                progress.dismiss()
                FireBaseUtils.handleFailure(applicationContext)
            }
    }


    private fun deleteStudentFromCollection(classDoc: DocumentReference, name: String) {
        classDoc.collection(Constants.STUDENTS_COLLECTION_PATH).document(name).delete()
            .addOnSuccessListener {
                Toast.makeText(applicationContext, "Student Deleted", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                FireBaseUtils.handleFailure(applicationContext)
            }
        progress.dismiss()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.students_activity_tool_bar_items, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_students -> handleAddStudents()
        }
        return true
    }

    private fun handleAddStudents() {
        MaterialDialog(this@StudentsActivity).show {
            title(text = "Add a student")
            input(hint = "Student Name", inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
            positiveButton(res = R.string.add) {
                val name= addLatestRollNumberToName(it.getInputField().text.toString())
                addStudent(
                   name
                )
            }
            cancelOnTouchOutside(false)
            negativeButton(res = R.string.help_dialog_positive_button_text) {
                dismiss()
            }

        }
    }

    private fun addStudent(name: String) {
        progress.show()
        arrayAdapterForStudentsName.add(name)
        val classDoc = db.collection(Constants.CLASSES_COLLECTION_PATH).document(className)
        classDoc.update(Constants.STUDENT_NAMES_ARRAY_FIELD_NAME, FieldValue.arrayUnion(name))
            .addOnSuccessListener {
                makeStudentsSubCollection(classDoc, name)
            }.addOnFailureListener {
                arrayAdapterForStudentsName.remove(name)
                FireBaseUtils.handleFailure(applicationContext)
            }

    }

    private fun addLatestRollNumberToName(name: String): String {
        return name.plus("-${students_list_view.adapter.count + 1}")
    }

    private fun makeStudentsSubCollection(document: DocumentReference, addedName: String) {
        val dataToBeSent:Map<String, Map<String, List<String>>> = mapOf(
            state getStringValue "term" to mapOf(
                "dates_present" to listOf(),
                "dates_absent" to listOf()
            )

        )

        document.collection("students").document(addedName)
            .set(dataToBeSent)
            .addOnSuccessListener {
                progress.dismiss()
                Toast.makeText(applicationContext, "Saving of student done", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener {
                FireBaseUtils.handleFailure(applicationContext)
            }
    }


    private fun getStudentsName() {
        progress.show()
        val className = state getStringValue "class"
        db.collection(Constants.CLASSES_COLLECTION_PATH).document(className)
            .get()
            .addOnSuccessListener { document ->
                if (document.contains(Constants.STUDENT_NAMES_ARRAY_FIELD_NAME)) {
                    studentsNameArray =
                        document.get(Constants.STUDENT_NAMES_ARRAY_FIELD_NAME) as List<String>
                    Log.d(TAG, studentsNameArray.toString())
                    if (studentsNameArray.isNullOrEmpty()) {
                        Toast.makeText(baseContext, R.string.no_student, Toast.LENGTH_LONG).show()
                    }
                    initializeArrayAdapter()
                    addArrayAdapterToListView()
                    setOnClickListenerToListView()
                    setOnHoldClickListenerToListView()
                    progress.dismiss()
                }
                else{
                    Toast.makeText(baseContext, R.string.no_student, Toast.LENGTH_LONG).show()
                    progress.dismiss()
                    goToAddStudentActivity()
                }
            }
            .addOnFailureListener {
                progress.dismiss()
                FireBaseUtils.handleFailure(this@StudentsActivity)
                FireBaseUtils.gotoTeacherActivity(this@StudentsActivity)
            }

    }
    private fun goToAddStudentActivity() {
        startActivity(Intent(this@StudentsActivity,TakeAttendanceActivity::class.java))
        finish()
    }

    private fun setOnClickListenerToListView() {
        students_list_view.setOnItemClickListener { _, _, position, _ ->
            handleOnClickListenerForStudentListView(position)
        }
    }

    private fun handleOnClickListenerForStudentListView(position: Int) {
        val studentName = studentsNameArray[position]
        createSummaryDialog(studentName)
    }

    private fun showSummaryDialog(studentName: String, message: String) {
        MaterialDialog(this@StudentsActivity).show {
            title(text = studentName)
            message(text = message)
            positiveButton(res = R.string.help_dialog_positive_button_text)
        }
    }

    private fun createSummaryDialog(studentName: String) {
        progress.show()
        val term = state getStringValue "term"
        val docRef =
            db.collection("${Constants.CLASSES_COLLECTION_PATH}/${className}/${Constants.STUDENTS_COLLECTION_PATH}")
                .document(studentName)
        docRef.get().addOnSuccessListener { document ->
            var numberOfDatesPresent:List<String> = listOf()
            var numberOfDatesAbsent:List<String> =listOf()
            if(document.contains("${term}.dates_present")){
                numberOfDatesPresent= document.get("${term}.dates_present") as List<String>
            }
            if(document.contains("${term}.dates_absent")){
                numberOfDatesAbsent = document.get("${term}.dates_absent") as List<String>
            }
            val message = createStudentAttendanceHistoryText(
                numberOfDatesPresent.size,
                numberOfDatesAbsent.size
            )
            progress.dismiss()
            showSummaryDialog(studentName, message)
            Log.d(TAG, "absent-${numberOfDatesAbsent}present-${numberOfDatesPresent}")
        }
            .addOnFailureListener {
                progress.dismiss()
                FireBaseUtils.handleFailure(this@StudentsActivity)
            }

    }

    private fun createStudentAttendanceHistoryText(
        NumberOfPresentDates: Int,
        NumberOfAbsentDates: Int,
    ): String {
        return "Student history for ${state.getStringValue("term").replace("_"," ")} \n"+
                "Number of days PRESENT:$NumberOfPresentDates\n " +
                "Number of days ABSENT:$NumberOfAbsentDates\n" +
                "Total number of days:${NumberOfAbsentDates + NumberOfPresentDates}"

    }

    private fun addArrayAdapterToListView() {
        students_list_view.adapter = arrayAdapterForStudentsName
    }

    private fun initializeArrayAdapter() {
        arrayAdapterForStudentsName = ArrayAdapter(
            this@StudentsActivity,
            android.R.layout.simple_list_item_1,
            studentsNameArray
        )
    }
}