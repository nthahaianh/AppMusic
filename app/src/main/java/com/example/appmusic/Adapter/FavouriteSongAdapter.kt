package com.example.appmusic.Adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appmusic.MySong
import com.example.appmusic.R
import com.example.appmusic.Service.SongService

class FavouriteSongAdapter(val context: Context?, val listSong: MutableList<MySong>) :
    RecyclerView.Adapter<FavouriteSongAdapter.ViewHolder>() {
    lateinit var view: View
    lateinit var itemClick: (position: Int) -> Unit
    fun setCallBack(click: (position: Int) -> Unit) {
        itemClick = click
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FavouriteSongAdapter.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_favourite_song, parent, false)
        this.view = view
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavouriteSongAdapter.ViewHolder, position: Int) {
        val song = listSong[position]
        holder.tvTitle.text = song.title
        holder.tvSinger.text = song.artist
        holder.tvTime.text = millionSecondsToTime(song.duration)
        try {
            if (song.isOnline) {
                holder.tvOnl.text = "ONL"
                Glide.with(view).load("${song.img}").into(holder.ivImage)
            } else {
                holder.tvOnl.text = "OFF"
                var mmr = MediaMetadataRetriever()
                mmr.setDataSource(context, Uri.parse(song.img))
                val byteImage = mmr.embeddedPicture
                var bitmap = BitmapFactory.decodeByteArray(byteImage, 0, byteImage!!.size)
                Glide.with(view).load(bitmap).into(holder.ivImage)
            }
        } catch (e: Exception) {
            e.stackTrace
            Glide.with(view).load(R.drawable.music).into(holder.ivImage)
        }

    }

    override fun getItemCount(): Int {
        return listSong.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvTitle: TextView = view.findViewById(R.id.item_favourite_tvTitle)
        var tvSinger: TextView = view.findViewById(R.id.item_favourite_tvSinger)
        var tvTime: TextView = view.findViewById(R.id.item_favourite_tvTime)
        var ivImage: ImageView = view.findViewById(R.id.item_favourite_ivImage)
        var tvOnl: TextView = view.findViewById(R.id.item_favourite_type)

        init {
            view.setOnClickListener { itemClick.invoke(adapterPosition) }
        }
    }

    private fun millionSecondsToTime(milliSeconds: Long): String {
        val hours = milliSeconds / (1000 * 60 * 60)
        val minutes = (milliSeconds % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (milliSeconds % (1000 * 60 * 60)) % (1000 * 60) / 1000
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}