package com.bookies.register.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.afollestad.materialdialogs.MaterialDialog
import com.bookies.register.R
import kotlinx.android.synthetic.main.activity_help.*

class HelpActivity : AppCompatActivity() {
    lateinit var arrayAdapterForHelpOptions:ArrayAdapter<String>
    lateinit var descriptionsForHelpOptions:Array<String>
    lateinit var titleForHelpOptions:Array<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        getDescriptionsAndTitleForHelpOptions()
        initializeArrayAdapter()
        setAdapterToListView()
        setOnClickListenerForListView()
    }

    private fun setOnClickListenerForListView() {
        help_option_list_view.setOnItemClickListener { _, _, position, _ ->
            handleOnClickListenerForListView(position)
        }

    }

    private fun handleOnClickListenerForListView(position: Int) {
        createDialog(title = titleForHelpOptions[position],description = descriptionsForHelpOptions[position])
    }
    private fun createDialog(title:String,description:String){
        MaterialDialog(this@HelpActivity).show {
            title(text=title)
            message(text = description)
            positiveButton(res = R.string.help_dialog_positive_button_text)
        }
    }

    private fun getDescriptionsAndTitleForHelpOptions(){
        descriptionsForHelpOptions=resources.getStringArray(R.array.help_options_descriptions_for_list_view)
        titleForHelpOptions=resources.getStringArray(R.array.help_options_title_for_list_view)
    }
    private fun initializeArrayAdapter(){
        arrayAdapterForHelpOptions= ArrayAdapter(
            this@HelpActivity,
            android.R.layout.simple_list_item_1,
            titleForHelpOptions
        )
    }
    private fun setAdapterToListView(){
        help_option_list_view.adapter=arrayAdapterForHelpOptions
    }

}