package com.bookies.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.datePicker
import kotlinx.android.synthetic.main.activity_teacher.*
import java.text.SimpleDateFormat
import java.util.*

class TeacherActivity : AppCompatActivity() {
    var options:MutableList<Option> = mutableListOf()
    var todayDate:Calendar= Calendar.getInstance()
    lateinit var listViewIcons:Array<Int>
    lateinit var state:Store
    val DATE_PATTERN="dd-MM-yyyy"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher)
        state=Store(applicationContext)
        setAcademicTerm()
        getTodayDate()
        getListViewIcons()
        createOptionsList()
        createOptionsListView()
        setOnClickListenerOnListView()
    }
    private fun setAcademicTerm(){
        state.addValue("term","first_term")
    }
    private fun getTodayDate() {
        val calendar=Calendar.getInstance()
        val simpleDateFormat=SimpleDateFormat(DATE_PATTERN)
        state.addValue("today_date",simpleDateFormat.format(calendar.time))

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.teachers_activity_tool_bar_items,menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.log_out->handleOnLogOutClicked()
            R.id.about_app->handleOnAboutAppClicked()
        }
        return true
    }
    private fun handleOnAboutAppClicked() {

        TODO("Not yet implemented")
    }
    private fun handleOnLogOutClicked() {
        TODO("Not yet implemented")
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
        startActivity(Intent(this@TeacherActivity,TakeAttendanceActivity::class.java))
    }

    private fun datesOptionClicked() {
       MaterialDialog(this@TeacherActivity).show {
           datePicker(maxDate = todayDate){dialog,datetime->
               handleDatePicked(datetime)
           }

       }
    }
    private fun studentsOptionClicked() {
        startActivity(Intent(this@TeacherActivity,StudentsActivity::class.java))
    }
    private fun handleDatePicked(datetime: Calendar) {
       val date=SimpleDateFormat(DATE_PATTERN).format(datetime.time)
        val intentToDateActivity=Intent(this@TeacherActivity,DateActivity::class.java)
        state.addValue("selected_date",date)
        startActivity(intentToDateActivity)

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