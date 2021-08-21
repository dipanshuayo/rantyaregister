package com.bookies.register

import android.content.Context

class Store(context: Context){
    val STORE_NAME="com.bookies.register.store"
    val store=context.getSharedPreferences(STORE_NAME,Context.MODE_PRIVATE)
    val Storeeditor=store.edit()
    fun addValue(key:String,value:Any){
       when(value){
           is String->Storeeditor.putString(key,value)
           is Int->Storeeditor.putInt(key,value)
           is Float->Storeeditor.putFloat(key, value)
           is Long->Storeeditor.putLong(key,value)
           is Boolean->Storeeditor.putBoolean(key,value)
       }
    }
    infix  fun getBooleanValue(key: String):Boolean=store.getBoolean(key,false)
    infix  fun getStringValue(key: String): String? =store.getString(key,"#")
    infix fun deleteStringValue(key: String)= Storeeditor.remove(key)


}