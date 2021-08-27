package com.bookies.register

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_take_attendance.*
import kotlinx.android.synthetic.main.fragment_student_attendance.*

class TakeAttendanceActivity : AppCompatActivity() {
    lateinit var   studentAttendanceFragment:StudentAttendanceFragment
    private val studentsNameArray=arrayOf(
        "Asdkfalsfd","Bsdfflas kfaslkdf","Calksdf ksldf","D asldfsf","Eskdlfj lsndfl","Fsdkfjlk lsdkjf",
        "Gsdklf","Hsdfnlafd","Iasdfsdf","sdkflJ ds","sklfKdskf",
        "Lsdfadsf sdf","Msdfsd dasfd","Nasdfds adsf","Osfsdf","Pjklsdf",
        "Qsdfsd","Rfsdfsf dsfd"
        ,"Ssdfsdc","Tsdfsdfs sddsf","Usfsf dsfs","Vsdjflk kds","Wlkjsdf",
        "Xdfsdfds","Ysdfsdfs","Zsdfsdfs")
    private val studentsAttendance=booleanArrayOf(
        false,false,false,false,false,false,false,false,false,false,false,false,false,
        false,false,false,false,false,false,false,false,false,false,false,false,false,
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_take_attendance)
        //getStudentsName()
        createStudentAttendance()
    }
    private fun getStudentsName(){
        TODO("IMPLEMENT LATER")
    }
    private fun setSaveButtonOnClickListener(){
        save_attendance_button.setOnClickListener {
            TODO("IMPLEMENT LATER")
        }
    }
    private fun onSaveButtonClick(){
        studentAttendanceFragment.getStudentsAttendance()
    }
    private fun createStudentAttendance(){
         studentAttendanceFragment=StudentAttendanceFragment.newInstance(studentsNameArray,studentsAttendance,"SAVE")
        supportFragmentManager
            .beginTransaction()
            .add(R.id.student_attendance_fragment_holder,studentAttendanceFragment,"studentAttendance")
            .commit()

    }


}