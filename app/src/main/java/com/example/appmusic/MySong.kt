package com.example.appmusic

import android.graphics.Bitmap
import java.io.Serializable

class MySong(
    var id: String,
    var title: String?,
    var artist: String?,
    var displayName: String?,
    var data: String?,
    var duration: Long,
    var img: String,
    var isOnline: Boolean
) : Serializable {
    override fun toString(): String {
        return "[id:$id,  title:$title,  artist:$artist,  displayName:$displayName, data:$data, duration:$duration,img:$img]"
    }

}