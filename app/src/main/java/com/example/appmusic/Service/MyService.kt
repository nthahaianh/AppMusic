package com.example.appmusic.Service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.widget.Toast

class MyService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

//    private val myBinder = MyBinder()
//
//    inner class MyBinder : Binder() {
//        fun getMyService(): MyService = this@MyService
//    }

//    override fun onBind(intent: Intent): IBinder {
//        Toast.makeText(this,"MyService - onBind",Toast.LENGTH_SHORT).show()
//        return myBinder
//    }
//
//    override fun onUnbind(intent: Intent?): Boolean {
//        Toast.makeText(this,"MyService - onUnbind",Toast.LENGTH_SHORT).show()
//        return super.onUnbind(intent)
//    }
//
//    override fun onCreate() {
//        super.onCreate()
//        Toast.makeText(this,"MyService - Create",Toast.LENGTH_SHORT).show()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        Toast.makeText(this,"MyService - Destroy",Toast.LENGTH_SHORT).show()
//    }
//    fun nextSong(){
//
//        Toast.makeText(this,"Next song", Toast.LENGTH_SHORT).show()
//    }
//    fun previousSong(){
//        Toast.makeText(this,"Previous song", Toast.LENGTH_SHORT).show()
//
//    }
//    fun getListMusic(){
//        Toast.makeText(this,"MyService - getListMusic",Toast.LENGTH_SHORT).show()
//
//    }
//    fun getListSearch(){
//        Toast.makeText(this,"MyService - getListSearch",Toast.LENGTH_SHORT).show()
//
//    }
//    fun getListFavourite(){
//        Toast.makeText(this,"MyService - getListFavourite",Toast.LENGTH_SHORT).show()
//
//    }
//    fun getListRecommend(){
//        Toast.makeText(this,"MyService - getListRecommend",Toast.LENGTH_SHORT).show()
//    }
}