package com.bookies.register

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class OptionsListViewAdapter(val options:MutableList<Option>, private val context: Context): BaseAdapter(){
    override fun getCount(): Int =options.size

    override fun getItem(position: Int): Any = options[position]

    override fun getItemId(position: Int): Long = options[position].hashCode().toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val optionView=inflateView(parent)
        val optionIcon=optionView?.findViewById<ImageView>(R.id.option_icon)
        val optionTitle=optionView?.findViewById<TextView>(R.id.option_title)
        val optionDescription=optionView?.findViewById<TextView>(R.id.option_description)
        optionIcon?.setImageResource(options[position].icon)
        optionTitle?.text = options[position].title
        optionDescription?.text = options[position].description
        return optionView
    }
    private fun inflateView(parent: ViewGroup?): View? {
        return LayoutInflater.from(context).inflate(R.layout.option_list_view_item,parent,false)
    }




}