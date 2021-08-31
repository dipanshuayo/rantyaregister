package com.bookies.register

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudentNamesRecyclerViewAdapter(val studentNames:MutableList<String>):
    RecyclerView.Adapter<StudentNamesRecyclerViewAdapter.StudentNameViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentNameViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.student_name_item,parent,false)
        return StudentNameViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentNameViewHolder, position: Int) {
        holder.studentName.text=studentNames[position]
    }
    private fun handleDeleteButton(position:Int){
        studentNames.removeAt(position)
    }
    override fun getItemCount(): Int =studentNames.size
    class StudentNameViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val studentName: TextView
        val deleteButton:ImageButton
        init {
            studentName=itemView.findViewById(R.id.student_name_text_view)
            deleteButton=itemView.findViewById(R.id.student_name_delete_button)
        }
    }

}