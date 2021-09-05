package com.bookies.register

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_date.*

class DateActivity : AppCompatActivity() {
    private lateinit var studentChangedAttendanceFragment: StudentAttendanceFragment
    lateinit var date: String
    lateinit var state: Store
    lateinit var docRef: DocumentReference
    var classAttendanceMap: MutableMap<String, Boolean> = mutableMapOf()
    var studentNamesOrdered= listOf<String>()
    val db =FireBaseUtils().db

    val TAG: String = "DateActivityDocument"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date)
        state = Store(applicationContext)
        setDate()
        getClassAttendanceMap()
        setUpSaveChangesButton()
    }

    private fun setUpSaveChangesButton() {
        save_changes_attendance_button.setOnClickListener {
            handleSaveChangesButton()
        }
    }

    private fun handleSaveChangesButton() {
        val newClassAttendance = studentChangedAttendanceFragment.getStudentsAttendance()
        if (isAttendanceChanged(newClassAttendance)) {
            sendChangedAttendance()
        } else {
            Toast.makeText(applicationContext, "No changes made", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendChangedAttendance() {
        val dataToBeSent = studentChangedAttendanceFragment.getStudentsAttendance()
        docRef.set(dataToBeSent, SetOptions.merge())
        updateStudentsAttendance(dataToBeSent)
    }

    private fun updateStudentsAttendance(dataToBeSent: MutableMap<String, Boolean>) {
        val className = state getStringValue "class"
        val term = state getStringValue "term"
        dataToBeSent.forEach { (name, attendance) ->
            val studentAttendanceDoc =
                db.collection("${Constants.CLASSES_COLLECTION_PATH}/${className}/${Constants.STUDENTS_COLLECTION_PATH}/")
                    .document(name)
            if (attendance) {
                //removes the date from date_absent
                removeDate(studentAttendanceDoc, "${term}.dates_absent")
                //adds the new date to dates_present
                studentAttendanceDoc.update(
                    "${term}.dates_present",
                    FieldValue.arrayUnion(date)
                )
            } else {
                //removes the date from date_absent
                removeDate(studentAttendanceDoc, "${term}.dates_present")
                //adds the new date to dates_present
                studentAttendanceDoc.update(
                    "${term}.dates_absent",
                    FieldValue.arrayUnion(date)
                )
            }
        }
    }

    private fun removeDate(studentAttendanceDoc: DocumentReference, from: String) {
        studentAttendanceDoc.update(
            from,
            FieldValue.arrayRemove(date)
        ).addOnSuccessListener {
            Toast.makeText(applicationContext,"deleted ",Toast.LENGTH_SHORT).show()
        }
    }

    private fun isAttendanceChanged(newClassAttendance: MutableMap<String, Boolean>): Boolean {
        return !newClassAttendance.equals(classAttendanceMap)
    }


    private fun getClassAttendanceMap() {
        var unOrderedClassAttendance=mapOf<String,Boolean>()
        val className = state getStringValue "class"
        val term = state getStringValue "term"
        docRef = db.collection("${Constants.DATES_COLLECTION_PATH}/${term}/")
            .document(date)
        docRef.get()
            .addOnSuccessListener { document ->
                if(document.exists() && document.data.isNullOrEmpty()) {
                    unOrderedClassAttendance =
                        document.get(className)  as Map<String,Boolean>
                    getOrderClassAttendance(unOrderedClassAttendance)
                    Log.d(TAG,unOrderedClassAttendance.toString())
                }
                else{
                    createNewClassMap()
                }

            }
            .addOnFailureListener { exception ->
                Toast.makeText(applicationContext, "Failed", Toast.LENGTH_SHORT).show()
                Log.d(TAG, exception.toString())
            }

    }
    private fun createNewClassMap(){
        val className=state getStringValue "class"
        db.document("${Constants.CLASSES_COLLECTION_PATH}/${className}")
            .get()
            .addOnSuccessListener { document->
                studentNamesOrdered= document.get(Constants.STUDENT_NAMES_ARRAY_FIELD_NAME) as List<String>
                classAttendanceSetTOFalse(studentNamesOrdered)
                makeFragment()
            }
    }
    private fun classAttendanceSetTOFalse(studentNamesOrdered: List<String>){
        studentNamesOrdered.forEach {name->
            classAttendanceMap.put(name,false)
        }

    }
    private fun getOrderClassAttendance(unOrderedClassAttendance: Map<String, Boolean>) {
        val className=state getStringValue "class"
        db.document("${Constants.CLASSES_COLLECTION_PATH}/${className}")
            .get()
            .addOnSuccessListener { document->
               studentNamesOrdered= document.get(Constants.STUDENT_NAMES_ARRAY_FIELD_NAME) as List<String>

                makeOrderedAttendance(studentNamesOrdered,unOrderedClassAttendance)
                makeFragment()
            }
    }

    private fun makeOrderedAttendance(studentNamesOrdered: List<String>, unOrderedClassAttendance: Map<String, Boolean>) {
        studentNamesOrdered.forEach {name->
            unOrderedClassAttendance[name]?.let { it -> classAttendanceMap[name] = it }
        }
        Log.d(TAG,"$classAttendanceMap should be ordered")
    }


    private fun setDate() {
        date = state.getStringValue("selected_date")
    }

    private fun makeFragment() {
        studentChangedAttendanceFragment =
            StudentAttendanceFragment.newInstance(
                classAttendanceMap.keys.toTypedArray(),
                classAttendanceMap.values.toBooleanArray(), "EDIT"
            )
        supportFragmentManager
            .beginTransaction()
            .add(
                R.id.student_change_attendance_fragment_holder,
                studentChangedAttendanceFragment,
                "studentEditAttendance"
            )
            .commit()
    }


}