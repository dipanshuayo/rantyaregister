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
    lateinit var progress: ProgressCircle
    lateinit var docRef: DocumentReference
    var classAttendanceMap: MutableMap<String, Boolean> = mutableMapOf()
    var studentNamesOrdered = listOf<String>()
    val db = FireBaseUtils().db
    val TAG: String = "DateActivityDocument"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date)
        state = Store(applicationContext)
        progress = ProgressCircle(this@DateActivity)
        //gets the date from date picker
        setDate()
        //gets the class attendance in proper order
        getClassAttendanceMap()
        //attached the db class and other properties
        setUpSaveChangesButton()
    }

    private fun setUpSaveChangesButton() {
        save_changes_attendance_button.setOnClickListener {
            handleSaveChangesButton()
        }
    }

    private fun handleSaveChangesButton() {
        progress.show()
        val newClassAttendance = studentChangedAttendanceFragment.getStudentsAttendance()
        if (isAttendanceChanged(newClassAttendance)) {
            sendChangedAttendance()
        } else {
            progress.dismiss()
            Toast.makeText(applicationContext, "No changes made", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendChangedAttendance() {
        val term = state getStringValue "term"
        val dataToBeSent = mapOf(
        term to   studentChangedAttendanceFragment.getStudentsAttendance()
        )
        docRef.set(dataToBeSent, SetOptions.merge()).addOnFailureListener {
            Toast.makeText(
                applicationContext,
                R.string.failed_class_code_verification,
                Toast.LENGTH_SHORT
            ).show()
        }
        dataToBeSent[term]?.let { updateStudentsAttendance(it) }
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
                ).addOnFailureListener {
                    Toast.makeText(
                        applicationContext,
                        R.string.failed_class_code_verification,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                //removes the date from date_absent
                removeDate(studentAttendanceDoc, "${term}.dates_present")
                //adds the new date to dates_present
                studentAttendanceDoc.update(
                    "${term}.dates_absent",
                    FieldValue.arrayUnion(date)
                ).addOnFailureListener {
                    Toast.makeText(
                        applicationContext,
                        R.string.failed_class_code_verification,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        progress.dismiss()
    }

    private fun removeDate(studentAttendanceDoc: DocumentReference, from: String) {
        studentAttendanceDoc.update(
            from,
            FieldValue.arrayRemove(date)
        ).addOnFailureListener {
            Toast.makeText(
                applicationContext,
                R.string.failed_class_code_verification,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun isAttendanceChanged(newClassAttendance: MutableMap<String, Boolean>): Boolean {
        val booleanList = mutableListOf<Boolean>()
        classAttendanceMap.keys.forEach { name ->
            booleanList.add(
                newClassAttendance[name] == classAttendanceMap[name]
            )
        }
        return booleanList.contains(false)
    }


    private fun getClassAttendanceMap() {
        progress.show()
        var unOrderedClassAttendance = mapOf<String, Boolean>()
        val className = state getStringValue "class"
        val term = state getStringValue "term"
        docRef = db.collection(term)
            .document(date)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document.exists() && !document.data.isNullOrEmpty()) {
                    unOrderedClassAttendance =
                        document.get(className) as Map<String, Boolean>
                    //sets class attendance in order
                    getOrderClassAttendance(unOrderedClassAttendance)
                    Log.d(TAG, unOrderedClassAttendance.toString())
                } else {
                    //creates new classAttendance for new dates
                    createNewClassMap()
                }

            }
            .addOnFailureListener { exception ->
                Toast.makeText(applicationContext, "Failed", Toast.LENGTH_SHORT).show()
                Log.d(TAG, exception.toString())
            }

    }

    private fun createNewClassMap() {
        val className = state getStringValue "class"
        db.document("${Constants.CLASSES_COLLECTION_PATH}/${className}")
            .get()
            .addOnSuccessListener { document ->
                //gets student names
                studentNamesOrdered =
                    document.get(Constants.STUDENT_NAMES_ARRAY_FIELD_NAME) as List<String>
                classAttendanceSetToFalse(studentNamesOrdered)
                progress.dismiss()
                makeFragment()
            }
            .addOnFailureListener {
                Toast.makeText(
                    applicationContext,
                    R.string.failed_class_code_verification,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun classAttendanceSetToFalse(studentNamesOrdered: List<String>) {
        studentNamesOrdered.forEach { name ->
            classAttendanceMap.put(name, false)
        }

    }

    private fun getOrderClassAttendance(unOrderedClassAttendance: Map<String, Boolean>) {
        val className = state getStringValue "class"
        db.document("${Constants.CLASSES_COLLECTION_PATH}/${className}")
            .get()
            .addOnSuccessListener { document ->
                studentNamesOrdered =
                    document.get(Constants.STUDENT_NAMES_ARRAY_FIELD_NAME) as List<String>
                makeOrderedAttendance(studentNamesOrdered, unOrderedClassAttendance)
                progress.dismiss()
                makeFragment()
            }.addOnFailureListener {
                Toast.makeText(
                    applicationContext,
                    R.string.failed_class_code_verification,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun makeOrderedAttendance(
        studentNamesOrdered: List<String>,
        unOrderedClassAttendance: Map<String, Boolean>
    ) {
        studentNamesOrdered.forEach { name ->
            unOrderedClassAttendance[name]?.let { it -> classAttendanceMap[name] = it }
        }
        Log.d(TAG, "$classAttendanceMap should be ordered")
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