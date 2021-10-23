package com.example.appmusic.SQLite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.appmusic.Model.MySong

class SQLHelper(context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    companion object {
        const val DB_NAME = "Favourite.db"
        const val DB_TABLE_FAVOURITE = "Favourite"
        const val DB_VERSION = 1
        const val DB_ID = "id"
        const val DB_TITLE = "title"
        const val DB_ARTIST = "artist"
        const val DB_DISPLAY_NAME = "displayName"
        const val DB_DATA = "data"
        const val DB_DURATION = "duration"
        const val DB_THUMBNAIL = "thumbnail"
        const val DB_IS_ONLINE = "isOnline"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val queryCreateTable = "CREATE TABLE $DB_TABLE_FAVOURITE (" +
                "$DB_ID string not null primary key," +
                "$DB_TITLE string," +
                "$DB_ARTIST string," +
                "$DB_DISPLAY_NAME string," +
                "$DB_DATA string," +
                "$DB_DURATION long," +
                "$DB_THUMBNAIL string," +
                "$DB_IS_ONLINE int" +
                ")"
        db.execSQL(queryCreateTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (newVersion != oldVersion) {
            db.execSQL("DROP TABLE IF exists $DB_TABLE_FAVOURITE")
            onCreate(db)
        }
    }
    fun addSong(song: MySong) {
        val sqLiteDatabase = writableDatabase
        var contentValues = ContentValues()
        contentValues.put("$DB_ID", song.id)
        contentValues.put("$DB_TITLE", song.title)
        contentValues.put("$DB_ARTIST", song.artist)
        contentValues.put("$DB_DISPLAY_NAME", song.displayName)
        contentValues.put("$DB_DATA", song.data)
        contentValues.put("$DB_DURATION", song.duration)
        contentValues.put("$DB_THUMBNAIL", song.img)
        if (song.isOnline){
            contentValues.put("$DB_IS_ONLINE", 1)
        }else{
            contentValues.put("$DB_IS_ONLINE", 0)
        }
        sqLiteDatabase.insert(DB_TABLE_FAVOURITE, null, contentValues)
    }

    fun removeSong(id:String){
        val sqLiteDatabase = writableDatabase
        sqLiteDatabase.delete(DB_TABLE_FAVOURITE,"$DB_ID = ?", arrayOf(id))
    }

    fun isExists(id:String): Boolean {
        val sqLiteDatabase = writableDatabase
        val cursor: Cursor = sqLiteDatabase.rawQuery(
            "SELECT * FROM $DB_TABLE_FAVOURITE where $DB_ID = ?",
            arrayOf(id)
        )
        return cursor.count == 1
    }

    fun getAll(): MutableList<MySong>{
        val songList: MutableList<MySong> = mutableListOf()
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
            val id = cursor.getString(cursor.getColumnIndex("$DB_ID"))
            val title = cursor.getString(cursor.getColumnIndex("$DB_TITLE"))
            val artist = cursor.getString(cursor.getColumnIndex("$DB_ARTIST"))
            val displayName = cursor.getString(cursor.getColumnIndex("$DB_DISPLAY_NAME"))
            val data = cursor.getString(cursor.getColumnIndex("$DB_DATA"))
            val duration = cursor.getLong(cursor.getColumnIndex("$DB_DURATION"))
            val thumbnail = cursor.getString(cursor.getColumnIndex("$DB_THUMBNAIL"))
            val getInfor = cursor.getInt(cursor.getColumnIndex("$DB_IS_ONLINE"))
            var isOnline = (getInfor==1)
            val song= MySong(id,title,artist,displayName,data,duration,thumbnail,isOnline)
            songList.add(song)
        }
        return songList
    }
}