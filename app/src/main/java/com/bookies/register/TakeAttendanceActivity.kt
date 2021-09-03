package com.bookies.register

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_take_attendance.*
import kotlinx.android.synthetic.main.fragment_student_attendance.*

class TakeAttendanceActivity : AppCompatActivity() {
    private lateinit var studentAttendanceFragment: StudentAttendanceFragment
    private lateinit var state: Store
    private val db= Firebase.firestore
    private var studentsNameArray = mutableListOf<String>()
    private val studentsAttendance = mutableListOf<Boolean>()

    private val TAG:String="TakeAttendanceDocument"
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_take_attendance)
        state = Store(applicationContext)
        if(state getBooleanValue "isStudentNameAdded"){
            getStudentsName()

        }
        else{
            createAddStudents()
        }

    }

    private fun createAddStudents() {
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

    private fun setAttendances(){
        studentsNameArray.forEach { _ ->
            studentsAttendance.add(false)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getStudentsName() {
        val className=state getStringValue "class"
      db.collection(Constants.CLASSES_COLLECTION_PATH).document(className)
          .get()
          .addOnSuccessListener { document->
              if(document!==null){
                  studentsNameArray= document.get(Constants.STUDENT_NAMES_ARRAY_FIELD_NAME) as MutableList<String>
                  setAttendances()
                  createStudentAttendance()
                  setSaveButtonOnClickListener()
                  Log.d(TAG,studentsNameArray.toString())
              }
          }
          .addOnCanceledListener{
              Toast.makeText(applicationContext,"Failed", Toast.LENGTH_SHORT).show()
          }



    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setSaveButtonOnClickListener() {
        save_attendance_button.setOnClickListener {
            onSaveButtonClick()
        }
    }

    private fun onSaveButtonClick() {
        val dataToBeSent=studentAttendanceFragment.getStudentsAttendance()
        val className=state getStringValue "class"
        val todayDate=state getStringValue "today_date"
        db.collection(Constants.DATES_COLLECTION_PATH)
            .document(state getStringValue "today_date")
            .set(dataToBeSent)
        dataToBeSent.forEach { (name, attendance) ->
            val studentAttendanceDoc=db.collection("${Constants.CLASSES_COLLECTION_PATH}/${className}/${Constants.STUDENTS_COLLECTION_PATH}/").document(name)
            if(attendance){
                studentAttendanceDoc.update(
                    "dates_present" ,
                    FieldValue.arrayUnion(todayDate)
                )
            }
            else{
                studentAttendanceDoc.update(
                    "dates_absent",
                    FieldValue.arrayUnion(todayDate)
                )
            }
        }

    }

    private fun createStudentAttendance() {
        studentAttendanceFragment =
            StudentAttendanceFragment.newInstance(
                studentsNameArray.toTypedArray(),
                studentsAttendance.toBooleanArray()
                , "SAVE")
        supportFragmentManager
            .beginTransaction()
            .add(
                R.id.student_attendance_fragment_holder,
                studentAttendanceFragment
                ,
                "studentAttendance"
            )
            .commit()

    }


}