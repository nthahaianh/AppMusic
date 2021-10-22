package com.example.appmusic.SQLite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.getStringOrNull
import com.example.appmusic.Model.SQLSong
import com.example.appmusic.MySong
import com.example.demoretrofit.Model.Song

class SQLHelper(context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    companion object {
        const val DB_NAME = "Favourite.db"
        const val DB_TABLE_FAVOURITE = "Favourite"
        const val DB_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        val queryCreateTable = "CREATE TABLE $DB_TABLE_FAVOURITE (" +
                "id string not null primary key," +
                "artist string," +
                "duration int," +
                "name string," +
                "thumbnail string," +
                "type string" +
                "isOnline int" +
                ")"
        db.execSQL(queryCreateTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (newVersion != oldVersion) {
            db.execSQL("DROP TABLE IF exists $DB_TABLE_FAVOURITE")
            onCreate(db)
        }
    }
    fun addSongOnline(song: Song) {
        val sqLiteDatabase = writableDatabase
        var contentValues = ContentValues()
        contentValues.put("id", song.id)
        contentValues.put("artist", song.artists_names)
        contentValues.put("duration", song.duration)
        contentValues.put("name", song.title)
        contentValues.put("thumbnail", song.thumbnail)
        contentValues.put("type", song.type)
        contentValues.put("isOnline", 0)
        sqLiteDatabase.insert(DB_TABLE_FAVOURITE, null, contentValues)
    }
    fun addSongOffline(song: MySong) {
        val sqLiteDatabase = writableDatabase
        var contentValues = ContentValues()
        contentValues.put("id", song.id)
        contentValues.put("artist", song.artist)
        contentValues.put("duration", song.duration)
        contentValues.put("name", song.title)
        contentValues.put("thumbnail","")
        contentValues.put("type", "audio")
        contentValues.put("isOnline", 1)
        sqLiteDatabase.insert(DB_TABLE_FAVOURITE, null, contentValues)
    }
    fun removeSong(id:String){
        val sqLiteDatabase = writableDatabase
        sqLiteDatabase.delete(DB_TABLE_FAVOURITE,"id = ?", arrayOf(id))
    }
    fun isExists(id:String): Boolean {
        val sqLiteDatabase = writableDatabase
        val cursor: Cursor = sqLiteDatabase.rawQuery(
            "SELECT * FROM $DB_TABLE_FAVOURITE where id = ?",
            arrayOf(id)
        )
        return cursor.count == 1
    }
    fun getAll(): MutableList<SQLSong>{
        val songList: MutableList<SQLSong> = mutableListOf()
        val sqLiteDatabase = readableDatabase
        val cursor = sqLiteDatabase.query(
            false,
            DB_TABLE_FAVOURITE,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )
        while (cursor.moveToNext()) {
            val id = cursor.getString(cursor.getColumnIndex("id"))
            val artist = cursor.getString(cursor.getColumnIndex("artist"))
            val duration = cursor.getLong(cursor.getColumnIndex("duration"))
            val name = cursor.getString(cursor.getColumnIndex("name"))
            val url = cursor.getString(cursor.getColumnIndex("name"))
            val thumbnail = cursor.getString(cursor.getColumnIndex("thumbnail"))
            val type = cursor.getString(cursor.getColumnIndex("type"))
            val isOnline = cursor.getInt(cursor.getColumnIndex("isOnline"))
            val song=SQLSong(id,name,artist,url,duration,thumbnail,type,isOnline)
            songList.add(song)
        }
        return songList
    }
}