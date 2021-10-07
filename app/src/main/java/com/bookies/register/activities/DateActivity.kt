@file:Suppress("PropertyName")

package com.bookies.register.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bookies.register.*
import com.bookies.register.fragments.StudentAttendanceFragment
import com.bookies.register.utils.Constants
import com.bookies.register.utils.FireBaseUtils
import com.bookies.register.utils.ProgressCircle
import com.bookies.register.utils.Store
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_date.*
import kotlinx.android.synthetic.main.activity_take_attendance.*

class DateActivity : AppCompatActivity() {
    private lateinit var studentChangedAttendanceFragment: StudentAttendanceFragment
    private lateinit var date: String
    private lateinit var state: Store
    private lateinit var progress: ProgressCircle
    private lateinit var docRef: DocumentReference
    private lateinit var className: String
    private lateinit var term: String
    private var classAttendanceMap: MutableMap<String, Boolean> = mutableMapOf()
    private var changedAttendanceMap: MutableMap<String, Boolean> = mutableMapOf()
    private var studentNamesOrdered = listOf<String>()
    private val db = FireBaseUtils().db
    private val TAG: String = "DateActivityDocument"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date)
        state = Store(applicationContext)
        progress = ProgressCircle(this@DateActivity)
        //sets className from state
        setClassName()
        //set term from state
        setTerm()
        //gets the date from date picker
        setDate()
        //set text to tool bar
        setTextToToolbar()
        //gets the class attendance in proper order
        getClassAttendanceMap()
        //attached the db class and other properties
        setUpSaveChangesButton()
        //set progress bar at the end so that it can overlap the fragment
    }

    private fun setTextToToolbar() {
        supportActionBar?.title = "$className: $date"
    }

    private fun setTerm() {
        term = state.getStringValue("term")
    }

    private fun setClassName() {
        className = state.getStringValue("class")
    }

    private fun onFailure(progressDisable: Boolean = true) {
        if (progressDisable) {
            progress.dismiss()
        }
        FireBaseUtils.handleFailure(this@DateActivity)

    }

    private fun setUpSaveChangesButton() {
        save_changes_attendance_button.setOnClickListener {
            save_changes_attendance_button.isEnabled = false
            handleSaveChangesButton()
        }
    }

    private fun handleSaveChangesButton() {
        progress.show()
        changedAttendanceMap = studentChangedAttendanceFragment.getStudentsAttendance()
        Log.d(TAG, "students attendance $changedAttendanceMap")
        if (changedAttendanceMap.isNotEmpty()) {
            sendChangedAttendance()
        } else {
            progress.dismiss()
            Toast.makeText(applicationContext, "No changes made", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendChangedAttendance() {
        Log.d(TAG, "student Attendance is $changedAttendanceMap")
        progress.show()
        changedAttendanceMap.keys.forEach { name ->
            val booleanValue: Boolean = changedAttendanceMap[name] == true
            db.runBatch { batch ->
                batch.set(
                    docRef, mapOf(
                        className to mapOf<String, Boolean>(name to booleanValue)
                    ), SetOptions.merge()
                )
            }.addOnCompleteListener {
                updateStudentsAttendance(name, booleanValue)
            }.addOnCompleteListener {
                Toast.makeText(applicationContext, "please work", Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }

        }
    }

    private fun updateStudentsAttendance(name: String, attendance: Boolean) {
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
                .addOnFailureListener {
                    onFailure()
                }
        } else {
            //removes the date from date_absent
            removeDate(studentAttendanceDoc, "${term}.dates_present")
            //adds the new date to dates_present
            studentAttendanceDoc.update(
                "${term}.dates_absent",
                FieldValue.arrayUnion(date)
            ).addOnFailureListener {
                onFailure()
            }
        }
        save_changes_attendance_button.isEnabled = true
        progress.dismiss()
    }

    private fun removeDate(studentAttendanceDoc: DocumentReference, from: String) {
        studentAttendanceDoc.update(
            from,
            FieldValue.arrayRemove(date)

        ).addOnFailureListener {

            onFailure()
        }
    }

    private fun getClassAttendanceMap() {
        progress.show()
        docRef = db.collection(term).document(date)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document.exists() && !document.data.isNullOrEmpty() && document.contains(
                        className
                    )
                ) {
                    val unOrderedClassAttendance: Map<String, Boolean> =
                        document.get(className) as Map<String, Boolean>
                    //sets class attendance in order
                    getOrderClassAttendance(unOrderedClassAttendance)
                    Log.d(TAG, unOrderedClassAttendance.toString())
                } else {
                    //creates new classAttendance for new dates
                    createNewClassMap()
                }
            }
            .addOnFailureListener {
                onFailure()
            }
    }

    private fun createNewClassMap() {
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
                onFailure()
            }
    }

    private fun classAttendanceSetToFalse(studentNamesOrdered: List<String>) {
        studentNamesOrdered.forEach { name ->
            classAttendanceMap[name] = false
        }

    }

    private fun getOrderClassAttendance(unOrderedClassAttendance: Map<String, Boolean>) {
        db.document("${Constants.CLASSES_COLLECTION_PATH}/${className}")
            .get()
            .addOnSuccessListener { document ->
                studentNamesOrdered =
                    document.get(Constants.STUDENT_NAMES_ARRAY_FIELD_NAME) as List<String>
                makeOrderedAttendance(studentNamesOrdered, unOrderedClassAttendance)
                progress.dismiss()
                makeFragment()
            }.addOnFailureListener {
                onFailure()
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