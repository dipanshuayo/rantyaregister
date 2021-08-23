package com.bookies.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_teacher.*

class TeacherActivity : AppCompatActivity() {
    var options:MutableList<Option> = mutableListOf()
    lateinit var listViewIcons:Array<Int>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher)
        getListViewIcons()
        createOptionsList()
        createOptionsListView()
        setOnClickListenerOnListView()
    }
    private fun setOnClickListenerOnListView(){
        options_list_view.setOnItemClickListener { _, _, position, _ ->
            handleOnClickListenerForListView(position)
        }
    }
    private fun handleOnClickListenerForListView(position:Int){
        when(position){
            0->studentsOptionClicked()
            1->datesOptionClicked()
            2->takeAttendanceOptionClicked()
            3->helpOptionClicked()
        }
    }

    private fun helpOptionClicked() {
        startActivity(Intent(this@TeacherActivity,HelpActivity::class.java))
    }

    private fun takeAttendanceOptionClicked() {
        TODO("Not yet implemented")
    }

    private fun datesOptionClicked() {
        TODO("Not yet implemented")
    }

    private fun studentsOptionClicked() {
        TODO("Not yet implemented")
    }

    private fun getListViewIcons(){
        listViewIcons= arrayOf(
        R.drawable.ic_student_64,
        R.drawable.ic_date,
        R.drawable.ic_take_attendance,
        R.drawable.ic_help
        )
    }
    private fun createOptionsListView(){
       options_list_view.adapter=OptionsListViewAdapter(options,this@TeacherActivity)
    }
    private fun createOptionsList(){
        val titles: Array<String> =resources.getStringArray(R.array.options_titles_for_list_view)
        val descriptions:Array<String> =resources.getStringArray(R.array.options_descriptions_for_list_view)
        titles.forEachIndexed { index, title ->
            options.add(
                Option(
                    listViewIcons[index],title,descriptions[index]
                )
            )
        }
    }

}