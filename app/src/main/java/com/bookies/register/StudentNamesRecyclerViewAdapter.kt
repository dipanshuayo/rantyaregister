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
    val addedNames= mutableListOf<String>()
    val deletedNames= mutableListOf<String>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentNameViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.student_name_item,parent,false)
        return StudentNameViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentNameViewHolder, position: Int) {
        holder.studentName.text=studentNames[position]
        holder.deleteButton.setOnClickListener {
            handleDeleteButton(position)
        }
    }
    private fun handleDeleteButton(position:Int){
        deletedNames.add(studentNames[position])
        studentNames.removeAt(position)
        notifyDataSetChanged()
        notifyItemRemoved(position)
    }
    fun addName(name:String){
        studentNames.add(name)
        addedNames.add(name)
        print("add name has been called")
        notifyDataSetChanged()
        notifyItemInserted(studentNames.size-1)
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