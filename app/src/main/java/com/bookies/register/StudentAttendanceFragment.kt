package com.bookies.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_student_attendance.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "STUDENT_NAMES"
private const val ARG_PARAM2 = "STUDENT_ATTENDANCE"

/**
 * A simple [Fragment] subclass.
 * Use the [StudentAttendanceFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StudentAttendanceFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var studentNames: Array<String>? = null
    private var studentAttendances: BooleanArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            studentNames = it.getStringArray(ARG_PARAM1)
            studentAttendances = it.getBooleanArray(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val studentAttendanceView:View=inflater.inflate(R.layout.fragment_student_attendance, container, false)
        studentNames?.forEachIndexed { index,name->

            studentAttendances?.get(index)?.let{ addToStudentsAttendeesLayout(layout=studentAttendanceView.findViewById(R.id.take_attendance_layout),name=name,isChecked= it) }
        }
        return studentAttendanceView
    }
    private fun addToStudentsAttendeesLayout(layout:LinearLayout,name:String,isChecked:Boolean) {
        layout.addView(addStudentAttendanceRow(name,isChecked))
    }
    private fun addStudentAttendanceRow(name:String,isChecked: Boolean): LinearLayout {
        return addNameAndCheckBox(
            getStudentsAttendanceLayout(),name,isChecked
        )
    }
    private fun getStudentsAttendanceLayout(): LinearLayout {
        val studentsAttendeeRow: LinearLayout = LinearLayout(activity)
        studentsAttendeeRow.orientation= LinearLayout.HORIZONTAL
        setViewToWrapContent(studentsAttendeeRow)
        return studentsAttendeeRow
    }
    private fun addNameAndCheckBox(linearLayout: LinearLayout, name:String,isChecked: Boolean): LinearLayout {
        linearLayout.addView(
            createTextView(name)
        )
        linearLayout.addView(createCheckBox(isChecked))
        return linearLayout
    }
    private fun createTextView(name:String): TextView {
        val textView= TextView(activity)
        setViewToWrapContent(textView)
        textView.text=name
        return textView
    }
    private fun createCheckBox(isChecked: Boolean): CheckBox {
        val checkBox= CheckBox(activity)
        checkBox.isChecked=isChecked
        setViewToWrapContent(checkBox)
        return checkBox
    }
    private fun setViewToWrapContent(view : View){
        view.layoutParams= ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param studentNames Parameter 1.
         * @param studentAttendances Parameter 2.
         * @return A new instance of fragment StudentAttendanceFragment.
         */

        @JvmStatic
        fun newInstance(studentNames: Array<String>, studentAttendances: BooleanArray) =
            StudentAttendanceFragment().apply {
                arguments = Bundle().apply {
                    putStringArray(ARG_PARAM1, studentNames)
                    putBooleanArray(ARG_PARAM2, studentAttendances)
                }
            }
    }
}