package com.bookies.register

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.afollestad.materialdialogs.utils.MDUtil.isLandscape
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_date.*
import kotlinx.android.synthetic.main.activity_take_attendance.*

class DateActivity : AppCompatActivity() {
    private lateinit var studentChangedAttendanceFragment: StudentAttendanceFragment
    lateinit var date: String
    lateinit var state: Store
    lateinit var progress: ProgressCircle
    lateinit var docRef: DocumentReference
    lateinit var className:String
    lateinit var term:String
    var classAttendanceMap: MutableMap<String, Boolean> = mutableMapOf()
    var changedAttendanceMap: MutableMap<String, Boolean> = mutableMapOf()
    var studentNamesOrdered = listOf<String>()
    val db = FireBaseUtils().db
    val TAG: String = "DateActivityDocument"


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
        //gets the class attendance in proper order
        getClassAttendanceMap()
        //attached the db class and other properties
        setUpSaveChangesButton()
        //set progress bar at the end so that it can overlap the fragment
    }

    private fun setTerm() {
        term=state.getStringValue("term")
    }

    private fun setClassName() {
        className=state.getStringValue("class")
    }

    private fun onFailure(res:Int=R.string.failed,progressDisable:Boolean=true){
        if(progressDisable) {
            progress.dismiss()
        }
        Toast.makeText(
            applicationContext,
            res,
            Toast.LENGTH_SHORT
        ).show()

    }
    private fun setUpSaveChangesButton() {
        save_changes_attendance_button.setOnClickListener {
            save_changes_attendance_button.isEnabled=false
            handleSaveChangesButton()

        }
    }
    private fun saveChangesProgressShow(){
        Log.d(TAG,"show progress")
        save_changes_progress.visibility= View.VISIBLE
    }
    private fun saveChangesProgressDismiss(){
        save_changes_progress.visibility= View.INVISIBLE
    }
    private fun handleSaveChangesButton() {
       progress.show()
       saveChangesProgressShow()
        changedAttendanceMap = studentChangedAttendanceFragment.getStudentsAttendance()
        Log.d(TAG,"students attedance ${changedAttendanceMap}")
        if (changedAttendanceMap.isNotEmpty()) {
            sendChangedAttendance()
        } else {
            progress.dismiss()
            Toast.makeText(applicationContext, "No changes made", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendChangedAttendance() {

        Log.d(TAG,"student Attendance is ${changedAttendanceMap}")
        progress.show()
        changedAttendanceMap.keys.forEach { name->
            val booleanValue:Boolean = changedAttendanceMap[name] == true

            db.runBatch { batch->
                batch.set(docRef,  mapOf(
                    className to mapOf<String,Boolean>(name to booleanValue )
                ), SetOptions.merge())
            }.addOnCompleteListener {
                updateStudentsAttendance(name,booleanValue)
            }.addOnCompleteListener {
                Toast.makeText(applicationContext, "please work", Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }

        }

//        docRef.set(dataToBeSent, SetOptions.merge()).addOnFailureListener {
//           onFailure()
//        }
        //dataToBeSent[className]?.let { updateStudentsAttendance(it) }

    }
    private  fun updateStudentsAttendance(name:String,attendance:Boolean){
            val studentAttendanceDoc = db.collection("${Constants.CLASSES_COLLECTION_PATH}/${className}/${Constants.STUDENTS_COLLECTION_PATH}/")
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


        save_changes_attendance_button.isEnabled=true
        saveChangesProgressDismiss()
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

    /*private fun isAttendanceChanged(newClassAttendance: MutableMap<String, Boolean>): Boolean {
        val booleanList = mutableListOf<Boolean>()
        classAttendanceMap.keys.forEach { name ->
            booleanList.add(
                newClassAttendance[name] == classAttendanceMap[name]
            )
        }
        return booleanList.contains(false)
    }*/


    private fun getClassAttendanceMap() {
        progress.show()
        docRef = db.collection(term).document(date)
        docRef.get()
            .addOnSuccessListener { document ->
                progress.dismiss()
                if (document.exists() && !document.data.isNullOrEmpty() && document.contains(className)) {
                   val unOrderedClassAttendance :Map<String,Boolean> =
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
            classAttendanceMap[name]= false
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
       supportActionBar?.title=date
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