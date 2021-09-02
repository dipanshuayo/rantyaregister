package com.bookies.register

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val STUDENT_NAMES = "STUDENT_NAMES_FOR_EDITING"

class AddStudentsFragment : Fragment() {
    private var studentNames: Array<String>? = null
    private val studentsNameArray= mutableListOf(
        "Asdkfalsfd","Bsdfflas kfaslkdf","Calksdf ksldf","D asldfsf","Eskdlfj lsndfl","Fsdkfjlk lsdkjf",
        "Gsdklf","Hsdfnlafd","Iasdfsdf","sdkflJ ds","sklfKdskf",
        "Lsdfadsf sdf","Msdfsd dasfd","Nasdfds adsf","Osfsdf","Pjklsdf",
        "Qsdfsd","Rfsdfsf dsfd"
        ,"Ssdfsdc","Tsdfsdfs sddsf","Usfsf dsfs","Vsdjflk kds","Wlkjsdf",
        "Xdfsdfds","Ysdfsdfs","Zsdfsdfs")
    lateinit var studentNamesRecyclerViewAdapter:StudentNamesRecyclerViewAdapter
    lateinit var addButton:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            studentNames = it.getStringArray(STUDENT_NAMES)

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       val addStudentFragmentView=inflater.inflate(R.layout.fragment_add_students, container, false)
        setUpRecyclerView(addStudentFragmentView)
        setUpButton(addStudentFragmentView)

        return addStudentFragmentView
    }
    private fun setUpRecyclerView(addStudentFragmentView:View){

        val studentNameRecyclerView=addStudentFragmentView.findViewById<RecyclerView>(R.id.students_name_edit_recyclerView)
        studentNamesRecyclerViewAdapter=StudentNamesRecyclerViewAdapter(studentsNameArray)
        studentNameRecyclerView.layoutManager= LinearLayoutManager(activity,LinearLayoutManager.VERTICAL,false)
        studentNameRecyclerView.adapter=studentNamesRecyclerViewAdapter
    }
    private fun setUpButton(addStudentFragmentView: View){
        addButton=addStudentFragmentView.findViewById(R.id.add_student_name_button)
        val editText=addStudentFragmentView.findViewById<EditText>(R.id.enter_student_name_edit_text)
        disableAddButton(editText.text.toString())
        handleEditText(editText)
        addButton.setOnClickListener {
            handleSaveButton(editText.text.toString(),editText)
        }
    }
    private fun handleSaveButton(name:String,editText: EditText){
        studentNamesRecyclerViewAdapter.addName(name)
        editText.text.clear()
    }
    private fun handleEditText(editText:EditText){
        editText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                disableAddButton(s)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                disableAddButton(s)
            }

            override fun afterTextChanged(s: Editable?) {
                disableAddButton(s)
            }

        })

    }
    private fun disableAddButton(text:CharSequence?){
        addButton.isEnabled = !text.isNullOrEmpty()
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param studentNames Parameter 1.
         * @return A new instance of fragment AddStudentsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(studentNames: Array<String>) =
            AddStudentsFragment().apply {
                arguments = Bundle().apply {
                    putStringArray(STUDENT_NAMES, studentNames)
                }
            }
    }
}