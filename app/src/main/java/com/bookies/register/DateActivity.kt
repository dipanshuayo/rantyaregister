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
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_date.*

class DateActivity : AppCompatActivity() {
    private lateinit var studentChangedAttendanceFragment: StudentAttendanceFragment
    lateinit var date:String
    lateinit var state:Store
    lateinit var docRef: DocumentReference
    var classAttendanceMap:Map<String,Boolean> = mapOf()
    val db= Firebase.firestore
    val TAG:String="DateActivityDocument"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date)
        state=Store(applicationContext)
        setDate()
        getClassAttendanceMap()
        makeFragment()
        setUpSaveChangesButton()
    }

    private fun setUpSaveChangesButton() {
        save_changes_attendance_button.setOnClickListener {
            handleSaveChangesButton()
        }
    }

    private fun handleSaveChangesButton() {
        val newClassAttendance=studentChangedAttendanceFragment.getStudentsAttendance()
        if(isAttendanceChanged(newClassAttendance)){
            sendChangedAttendance()
        }
        else{
            Toast.makeText(applicationContext,"No changes made",Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendChangedAttendance() {
        val dataToBeSent=studentChangedAttendanceFragment.getStudentsAttendance()
        docRef.set(dataToBeSent, SetOptions.merge())
        updateStudentsAttendance(dataToBeSent)
    }
    private fun updateStudentsAttendance(dataToBeSent: MutableMap<String, Boolean>) {
        val className=state getStringValue "class"
        val term=state getStringValue "term"
        dataToBeSent.forEach { (name, attendance) ->
            val studentAttendanceDoc=db.collection("${Constants.CLASSES_COLLECTION_PATH}/${className}/${Constants.STUDENTS_COLLECTION_PATH}/").document(name)
            if(attendance){
                //removes the date from date_absent
                removeDate(studentAttendanceDoc,"${term}.dates_absent")
                //adds the new date to dates_present
                studentAttendanceDoc.update(
                    "${term}.dates_present" ,
                    FieldValue.arrayUnion(date)
                )
            }
            else{
                //removes the date from date_absent
                removeDate(studentAttendanceDoc,"${term}.dates_present")
                //adds the new date to dates_present
                studentAttendanceDoc.update(
                    "${term}.dates_absent",
                    FieldValue.arrayUnion(date)
                )
            }
        }
    }

    private fun removeDate(studentAttendanceDoc: DocumentReference, from: String) {
        studentAttendanceDoc.get()
            .addOnSuccessListener { document->
                val oldList=document.getField<List<String>>(from)
                val newList= oldList?.filter { it!==date }
                studentAttendanceDoc.set(mapOf(
                    from to newList
                ))
                Toast.makeText(applicationContext,"changes being made",Toast.LENGTH_SHORT).show()
            }

    }

    private fun isAttendanceChanged(newClassAttendance: MutableMap<String, Boolean>): Boolean {
       return !newClassAttendance.equals(classAttendanceMap)
    }


    private fun getClassAttendanceMap() {
        val className=state getStringValue "class"
        val term=state getStringValue "term"
        docRef=db.collection("${Constants.DATES_COLLECTION_PATH}/${term}/${date}/")
            .document(className)
        docRef.get()
            .addOnSuccessListener { document->
                classAttendanceMap=document.toObject<Map<String,Boolean>>()?:mapOf()
                Log.d(TAG,classAttendanceMap.toString())
            }
            .addOnFailureListener {exception->
                Toast.makeText(applicationContext,"Failed", Toast.LENGTH_SHORT).show()
                Log.d(TAG,exception.toString())
            }

    }





    private fun setDate() {
        TODO("Not yet implemented")
    }
    private fun makeFragment() {
        studentChangedAttendanceFragment =
            StudentAttendanceFragment.newInstance(
                classAttendanceMap.keys.toTypedArray(),
                classAttendanceMap.values.toBooleanArray()
                , "EDIT")
        supportFragmentManager
            .beginTransaction()
            .add(
                R.id.student_change_attendance_fragment_holder,
                studentChangedAttendanceFragment
                ,
                "studentEditAttendance"
            )
            .commit()
    }


}