package com.bookies.register.utils

import android.content.Context
import android.content.SharedPreferences

/*
 *Keys include
 * isStudentNameAdded to check if studentNameIsAdded
 * login check if teacher logged in
 * class gets class name of teacher
 * today_date gets today's date
 * selected_date gets the date selected on date picker
 * term gets the current term
 */
class Store(context: Context){
    private val STORE_NAME="com.bookies.register.store"
    private val store: SharedPreferences =context.getSharedPreferences(STORE_NAME,Context.MODE_PRIVATE)
    private val Storeeditor: SharedPreferences.Editor =store.edit()
    fun addValue(key:String,value:Any){
       when(value){
           is String->Storeeditor.putString(key,value).commit()
           is Int->Storeeditor.putInt(key,value).commit()
           is Float->Storeeditor.putFloat(key, value).commit()
           is Long->Storeeditor.putLong(key,value).commit()
           is Boolean->Storeeditor.putBoolean(key,value).commit()
       }
    }
    fun clearStore(){
        Storeeditor.clear()
    }
    infix  fun getBooleanValue(key: String):Boolean=store.getBoolean(key,false)
    infix  fun getStringValue(key: String): String =store.getString(key,"null")?:"null"
    infix fun deleteStringValue(key: String)= Storeeditor.remove(key)



}