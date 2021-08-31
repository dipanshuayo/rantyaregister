package com.bookies.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
private const val STUDENT_NAMES = "STUDENT_NAMES_FOR_EDITING"

class AddStudentsFragment : Fragment() {
    private var studentNames: String? = null
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
        addStudentFragmentView=inflater.inflate(R.layout.fragment_add_students, container, false)
        return addStudentFragment
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
        fun newInstance(studentNames: String) =
            AddStudentsFragment().apply {
                arguments = Bundle().apply {
                    putStringArray(STUDENT_NAMES, studentNames)
                }
            }
    }
}