package com.bookies.register

import android.content.Context

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
    fun addValueString(key:String,value:String){
        Storeeditor.putString(key,value)
    }
    infix  fun getBooleanValue(key: String):Boolean=store.getBoolean(key,false)
    infix  fun getStringValue(key: String): String? =store.getString(key,"#")
    infix fun deleteStringValue(key: String)= Storeeditor.remove(key)


}