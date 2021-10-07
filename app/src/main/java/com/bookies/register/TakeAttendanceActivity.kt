package com.bookies.register

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.FieldValue
import kotlinx.android.synthetic.main.activity_take_attendance.*
import kotlinx.android.synthetic.main.fragment_student_attendance.*

class TakeAttendanceActivity : AppCompatActivity() {
    private lateinit var studentAttendanceFragment: StudentAttendanceFragment
    private lateinit var state: Store
    lateinit var progress: ProgressCircle
    private val db = FireBaseUtils().db
    private var studentsNameArray = mutableListOf<String>()
    private val studentsAttendance = mutableListOf<Boolean>()

    private val TAG: String = "TakeAttendanceDocument"

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_take_attendance)
        state = Store(applicationContext)
        progress = ProgressCircle(this@TakeAttendanceActivity)
        getStudentsName()

    }

    private fun createAddStudents() {
        save_attendance_button.visibility= View.GONE
        supportFragmentManager
            .beginTransaction()
            .add(
                R.id.student_attendance_fragment_holder,
                AddStudentsFragment.newInstance(
                    arrayOf()
                ),
                "studentAttendance"
            )
            .commit()
    }

    private fun setAttendances() {
        studentsNameArray.forEach { _ ->
            studentsAttendance.add(false)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getStudentsName() {
        progress.show()
        val className = state getStringValue "class"
        db.collection(Constants.CLASSES_COLLECTION_PATH).document(className)
            .get()
            .addOnSuccessListener { document ->

                    if(!document.contains(Constants.STUDENT_NAMES_ARRAY_FIELD_NAME)){
                        createAddStudents()
                    }
                else {
                        studentsNameArray =
                            document.get(Constants.STUDENT_NAMES_ARRAY_FIELD_NAME) as MutableList<String>
                        setAttendances()
                        createStudentAttendance()
                        setSaveButtonOnClickListener()
                        Log.d(TAG, studentsNameArray.toString())
                    }
                    progress.dismiss()

            }
            .addOnCanceledListener {
                progress.dismiss()
                Toast.makeText(applicationContext, "Failed", Toast.LENGTH_SHORT).show()
                FireBaseUtils.gotoTeacherActivity(this@TakeAttendanceActivity)
            }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setSaveButtonOnClickListener() {
        save_attendance_button.setOnClickListener {
            onSaveButtonClick()
        }
    }

    private fun onSaveButtonClick() {
        progress.show()
        val className = state getStringValue "class"
        val todayDate = state getStringValue "today_date"
        val term = state getStringValue "term"
        val dataToBeSent = mapOf(
            className to studentAttendanceFragment.getStudentsAttendance()
        )
        db.collection(term)
            .document(todayDate)
            .set(dataToBeSent)
            .addOnSuccessListener {
                updateStudentCollection(dataToBeSent, className, term, todayDate)
            }


    }

    private fun updateStudentCollection(
        dataToBeSent: Map<String, MutableMap<String, Boolean>>,
        className: String,
        term: String,
        todayDate: String
    ) {
        dataToBeSent[className]?.forEach { (name, attendance) ->
            val studentAttendanceDoc =
                db.collection("${Constants.CLASSES_COLLECTION_PATH}/${className}/${Constants.STUDENTS_COLLECTION_PATH}/")
                    .document(name)
            if (attendance) {
                studentAttendanceDoc.update(
                    "${term}.dates_present",
                    FieldValue.arrayUnion(todayDate)
                )
            } else {
                studentAttendanceDoc.update(
                    "${term}.dates_absent",
                    FieldValue.arrayUnion(todayDate)
                )
            }
        }
        Thread.sleep(2000)
        progress.dismiss()

    }

    private fun createStudentAttendance() {
        studentAttendanceFragment =
            StudentAttendanceFragment.newInstance(
                studentsNameArray.toTypedArray(),
                studentsAttendance.toBooleanArray(), "SAVE"
            )
        supportFragmentManager
            .beginTransaction()
            .add(
                R.id.student_attendance_fragment_holder,
                studentAttendanceFragment,
                "studentAttendance"
            )
            .commit()

    }


}