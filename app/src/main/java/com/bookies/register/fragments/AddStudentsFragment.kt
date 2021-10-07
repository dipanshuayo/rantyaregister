package com.bookies.register.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.model.Document
import com.google.firebase.ktx.Firebase

private const val STUDENT_NAMES = "STUDENT_NAMES_FOR_EDITING"

class AddStudentsFragment : Fragment() {
    private var studentNames: Array<String>? = null
    val db = FireBaseUtils().db
    private var DOCUMENT_NAME: String = "JSS1A"
    lateinit var studentNamesRecyclerViewAdapter: StudentNamesRecyclerViewAdapter
    lateinit var addButton: Button
    lateinit var saveButton: Button
    lateinit var state: Store
    lateinit var progress: ProgressCircle
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
        state = activity?.let { Store(it.applicationContext) }!!
        progress = activity?.let { ProgressCircle(it) }!!
        val addStudentFragmentView =
            inflater.inflate(R.layout.fragment_add_students, container, false)
        setUpRecyclerView(addStudentFragmentView)
        setUpAddButtonAndEditText(addStudentFragmentView)
        setUpSaveButton(addStudentFragmentView)

        return addStudentFragmentView
    }

    private fun setUpSaveButton(addStudentFragmentView: View) {
        saveButton = addStudentFragmentView.findViewById(R.id.save_student_name_button)
        //saveButton.isEnabled = studentNames?.size != 0
        saveButton.setOnClickListener {
            handleSaveButton()
        }

    }

    private fun handleSaveButton() {
        progress.show()
        var addedNames: MutableList<String> = mutableListOf()
        var deletedNames: MutableList<String> = mutableListOf()
        if (studentNames?.size == 0) {
            addedNames = studentNamesRecyclerViewAdapter.addedNames
        } else {
            deletedNames = studentNamesRecyclerViewAdapter.deletedNames
        }
        if (addedNames.isNotEmpty()) {
            sendAddedNames(addedNames)
        }
        if (deletedNames.isNotEmpty()) {
            sendDeletedNames(deletedNames)
        }
        if (addedNames.isEmpty()) {
            Toast.makeText(activity, "You have not added any student name", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun sendAddedNames(addedNames: MutableList<String>) {
        val dataToSend = mapOf(
            "names" to attachLatestRoleNumber(addedNames)
        )
        DOCUMENT_NAME = state getStringValue "class"
        val studentNamesDocument =
            db.collection(Constants.CLASSES_COLLECTION_PATH).document(DOCUMENT_NAME)
        if (studentNames.isNullOrEmpty()) {
            studentNamesDocument
                .set(dataToSend)
                .addOnSuccessListener {
                    state.addValue("isStudentNameAdded", true)
                    dataToSend["names"]?.toMutableList()?.let { it1 ->
                        makeStudentsSubCollection(
                            studentNamesDocument,
                            it1
                        )
                    }
                    progress.dismiss()
                    Toast.makeText(activity, "Students name saved", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    progress.dismiss()
                    Toast.makeText(
                        activity,
                        "Students name failed to save. You don\'t have network or data",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }

        } else {
            //sends a new name list to db
            studentNamesDocument.update(
                "names",
                FieldValue.arrayUnion(*arrayOf(dataToSend["names"]))
            ).addOnSuccessListener {
                progress.dismiss()
            }
        }


    }

    private fun makeStudentsSubCollection(
        document: DocumentReference,
        addedNames: MutableList<String>
    ) {
        val dataToBeSent = mapOf(
            state getStringValue "term" to mapOf<String, List<String>>(
                "dates_present" to listOf<String>(),
                "dates_absent" to listOf<String>()
            )

        )
        addedNames.forEach { id ->
            document.collection(Constants.STUDENTS_COLLECTION_PATH).document(id)
                .set(dataToBeSent)
                .addOnSuccessListener {
                    Toast.makeText(activity, "Saving of students done", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Saving of students failed", Toast.LENGTH_LONG).show()
                }
        }

    }


    private fun attachLatestRoleNumber(names: MutableList<String>): List<String> {
        var rollNumber = state getIntValue "lastRollNumber"
        return names.map { name -> "$name-${rollNumber++}" }

    }

    private fun sendDeletedNames(deletedNames: MutableList<String>) {

    }


    private fun setUpRecyclerView(addStudentFragmentView: View) {

        val studentNameRecyclerView =
            addStudentFragmentView.findViewById<RecyclerView>(R.id.students_name_edit_recyclerView)
        studentNamesRecyclerViewAdapter =
            StudentNamesRecyclerViewAdapter(studentNames?.toMutableList() ?: mutableListOf())
        studentNameRecyclerView.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        studentNameRecyclerView.adapter = studentNamesRecyclerViewAdapter
    }

    private fun setUpAddButtonAndEditText(addStudentFragmentView: View) {
        addButton = addStudentFragmentView.findViewById(R.id.add_student_name_button)
        val editText =
            addStudentFragmentView.findViewById<EditText>(R.id.enter_student_name_edit_text)
        disableAddButton(editText.text.toString())
        handleEditText(editText)
        addButton.setOnClickListener {
            handleAddButton(editText.text.toString(), editText)
        }
    }

    private fun handleAddButton(name: String, editText: EditText) {
        studentNamesRecyclerViewAdapter.addName(name)
        editText.text.clear()
    }

    private fun handleEditText(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
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

    private fun disableAddButton(text: CharSequence?) {
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