package com.bookies.register

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.android.synthetic.main.activity_students.*

class StudentsActivity : AppCompatActivity() {
    lateinit var arrayAdapterForStudentsName:ArrayAdapter<String>
    private val studentsNameArray=arrayOf(
        "Asdkfalsfd","Bsdfflas kfaslkdf","Calksdf ksldf","D asldfsf","Eskdlfj lsndfl","Fsdkfjlk lsdkjf",
        "Gsdklf","Hsdfnlafd","Iasdfsdf","sdkflJ ds","sklfKdskf",
        "Lsdfadsf sdf","Msdfsd dasfd","Nasdfds adsf","Osfsdf","Pjklsdf",
        "Qsdfsd","Rfsdfsf dsfd"
        ,"Ssdfsdc","Tsdfsdfs sddsf","Usfsf dsfs","Vsdjflk kds","Wlkjsdf",
        "Xdfsdfds","Ysdfsdfs","Zsdfsdfs")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_students)
        initializeArrayAdapter()
        addArrayAdapterToListView()
        setOnClickListenerToListView()
    }

    private fun setOnClickListenerToListView() {
        students_list_view.setOnItemClickListener { _, _, position, _ ->
            handleOnClickListenerForStudentListView(position)
        }

    }

    private fun handleOnClickListenerForStudentListView(position: Int) {
        MaterialDialog(this@StudentsActivity).show {
            title(text = studentsNameArray[position])
            message(text = "test")
            positiveButton(res = R.string.help_dialog_positive_button_text)
        }
    }

    private fun addArrayAdapterToListView() {

        students_list_view.adapter=arrayAdapterForStudentsName
    }

    private fun initializeArrayAdapter() {
        arrayAdapterForStudentsName= ArrayAdapter(this@StudentsActivity,android.R.layout.simple_list_item_1,studentsNameArray)
    }
}