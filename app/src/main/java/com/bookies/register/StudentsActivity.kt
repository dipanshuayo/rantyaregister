package com.bookies.register

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_students.*

class StudentsActivity : AppCompatActivity() {
    lateinit var arrayAdapterForStudentsName: ArrayAdapter<String>
    lateinit var state: Store
    lateinit var className: String
    lateinit var progress: ProgressCircle
    val db = FireBaseUtils().db
    private val TAG = "StudentsActivityDocument"
    private var studentsNameArray = listOf<String>()
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
        deleteStudent(name)
    }

    private fun deleteStudent(name: String) {
        val classDoc = db.collection(Constants.CLASSES_COLLECTION_PATH).document(className)
        classDoc.update(Constants.STUDENT_NAMES_ARRAY_FIELD_NAME, FieldValue.arrayRemove(name))
            .addOnSuccessListener {
                arrayAdapterForStudentsName.remove(name)
            }.addOnFailureListener {
                Toast.makeText(
                    applicationContext,
                    R.string.failed_class_code_verification,
                    Toast.LENGTH_SHORT
                ).show()
            }
        deleteStudentFromCollection(classDoc, name)
    }

    private fun deleteStudentFromCollection(classDoc: DocumentReference, name: String) {
        classDoc.collection(Constants.STUDENTS_COLLECTION_PATH).document(name).delete()
            .addOnFailureListener {
                Toast.makeText(
                    applicationContext,
                    R.string.failed_class_code_verification,
                    Toast.LENGTH_SHORT
                ).show()
            }
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
                addStudent(addLatestRollNumberToName(it.getInputField().text.toString()))
            }
            cancelOnTouchOutside(false)
            negativeButton(res = R.string.help_dialog_positive_button_text) {
                dismiss()
            }

        }
    }

    private fun addStudent(name: String) {
        val classDoc = db.collection(Constants.CLASSES_COLLECTION_PATH).document(className)

        classDoc.update(Constants.STUDENT_NAMES_ARRAY_FIELD_NAME, FieldValue.arrayUnion(name))
            .addOnSuccessListener {
                arrayAdapterForStudentsName.add(name)

                makeStudentsSubCollection(classDoc, name)
            }.addOnFailureListener {
                Toast.makeText(
                    applicationContext,
                    R.string.failed_class_code_verification,
                    Toast.LENGTH_SHORT
                ).show()
            }

    }

    private fun addLatestRollNumberToName(name: String): String {
        return name.plus("-${students_list_view.adapter.count}")
    }

    private fun makeStudentsSubCollection(document: DocumentReference, addedName: String) {
        val dataToBeSent = mapOf(
            state getStringValue "term" to mapOf<String, List<String>>(
                "dates_present" to listOf<String>(),
                "dates_absent" to listOf<String>()
            )

        )

        document.collection("students").document(addedName)
            .set(dataToBeSent)
            .addOnSuccessListener {
                Toast.makeText(applicationContext, "Saving of students done", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext, "Saving of students failed", Toast.LENGTH_LONG)
                    .show()

            }

    }


    private fun getStudentsName() {
        progress.show()
        val className = state getStringValue "class"
        db.collection(Constants.CLASSES_COLLECTION_PATH).document(className)
            .get()
            .addOnSuccessListener { document ->
                if (document !== null) {
                    studentsNameArray =
                        document.get(Constants.STUDENT_NAMES_ARRAY_FIELD_NAME) as List<String>
                    Log.d(TAG, studentsNameArray.toString())
                    initializeArrayAdapter()
                    addArrayAdapterToListView()
                    setOnClickListenerToListView()
                    setOnHoldClickListenerToListView()
                    progress.dismiss()

                }
            }
            .addOnCanceledListener {
                progress.dismiss()
                Toast.makeText(applicationContext, "Failed", Toast.LENGTH_SHORT).show()
                FireBaseUtils.gotoTeacherActivity(this@StudentsActivity)
            }

    }

    private fun setOnClickListenerToListView() {
        students_list_view.setOnItemClickListener { _, _, position, _ ->
            handleOnClickListenerForStudentListView(position)
        }
    }

    private fun handleOnClickListenerForStudentListView(position: Int) {
        val studentName = studentsNameArray[position]
        createFinalDialog(studentName)
    }

    private fun makeDialog(studentName: String, message: String) {
        MaterialDialog(this@StudentsActivity).show {
            title(text = studentName)
            message(text = message)
            positiveButton(res = R.string.help_dialog_positive_button_text)
        }
    }

    private fun createFinalDialog(studentName: String) {
        progress.show()
        val term = state getStringValue "term"
        val docRef =
            db.collection("${Constants.CLASSES_COLLECTION_PATH}/${className}/${Constants.STUDENTS_COLLECTION_PATH}")
                .document(studentName)
        docRef.get().addOnSuccessListener { document ->
            val numberOfDatesPresent = document.get("${term}.dates_present") as List<String>
            val numberOfDatesAbsent = document.get("${term}.dates_absent") as List<String>
            val message = createStudentAttendanceHistoryText(
                numberOfDatesPresent.size,
                numberOfDatesAbsent.size
            )
            progress.dismiss()
            makeDialog(studentName, message)
            Log.d(TAG, "absent-${numberOfDatesAbsent}present-${numberOfDatesPresent}")
            Toast.makeText(applicationContext, "got the dates", Toast.LENGTH_SHORT).show()
        }
            .addOnFailureListener {
                progress.dismiss()
                Toast.makeText(applicationContext, "Failed", Toast.LENGTH_SHORT).show()
            }

    }

    private fun createStudentAttendanceHistoryText(
        NumberOfPresentDates: Int,
        NumberOfAbsentDates: Int,
    ): String {
        return "Number of days PRESENT:$NumberOfPresentDates\n " +
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