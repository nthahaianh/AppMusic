package com.example.appmusic.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appmusic.MySong
import com.example.appmusic.R

class MySongAdapter(var listSong: MutableList<MySong>):RecyclerView.Adapter<MySongAdapter.ViewHolder>() {

    lateinit var itemClick:(position:Int)->Unit
    fun setCallBack(click:(position:Int)->Unit){
        itemClick = click
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list_song,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listSong.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var song = listSong[position]
        holder.tvTitle.text = song.title
        holder.tvSinger.text = song.artist
        holder.ivImage.setImageResource(R.drawable.music)
        holder.tvTime.text = millionSecondsToTime(song.duration)
    }

    inner class ViewHolder(view: View):RecyclerView.ViewHolder(view){
        var tvTitle:TextView = view.findViewById(R.id.item_song_tvTitle)
        var tvSinger:TextView = view.findViewById(R.id.item_song_tvSinger)
        var tvTime:TextView = view.findViewById(R.id.item_song_tvTime)
        var ivImage:ImageView = view.findViewById(R.id.item_song_ivImage)
        init {
            view.setOnClickListener { itemClick.invoke(adapterPosition) }
        }
    }
    private fun millionSecondsToTime(milliSeconds: Long): String {
        val hours = milliSeconds / (1000*60*60)
        val minutes = (milliSeconds % (1000*60*60)) / (1000*60)
        val seconds = (milliSeconds % (1000*60*60)) % (1000*60)/1000
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}