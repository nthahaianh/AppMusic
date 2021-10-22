package com.example.appmusic.Model

class SQLSong(
    var id: String,
    val title: String,
    val artists: String,
    val url: String,
    val duration: Long,
    val thumbnail: String,
    val type: String,
    val isOnline:Int
) {
}