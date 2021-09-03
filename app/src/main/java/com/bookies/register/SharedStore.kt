package com.bookies.register

import android.content.Context

/*
 *Keys include
 * isStudentNameAdded to check if studentNameIsAdded
 * login check if teacher logged in
 * class gets class name of teacher
 * today_date gets today's date
 */
class Store(context: Context){
    val STORE_NAME="com.bookies.register.store"
    val store=context.getSharedPreferences(STORE_NAME,Context.MODE_PRIVATE)
    val Storeeditor=store.edit()
    fun addValue(key:String,value:Any){
       when(value){
           is String->Storeeditor.putString(key,value).commit()
           is Int->Storeeditor.putInt(key,value).commit()
           is Float->Storeeditor.putFloat(key, value).commit()
           is Long->Storeeditor.putLong(key,value).commit()
           is Boolean->Storeeditor.putBoolean(key,value).commit()
       }
    }
    infix fun getIntValue(key:String):Int=store.getInt(key,0)
    infix  fun getBooleanValue(key: String):Boolean=store.getBoolean(key,false)
    infix  fun getStringValue(key: String): String =store.getString(key,"#")?:"null"
    infix fun deleteStringValue(key: String)= Storeeditor.remove(key)



}