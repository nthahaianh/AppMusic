package com.example.appmusic.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appmusic.R
import com.example.demoretrofit.Model.SongSearch

class SearchListAdapter(var listSong: MutableList<SongSearch>):RecyclerView.Adapter<SearchListAdapter.ViewHolder>() {
    val urlThumb = "https://photo-zmp3.zadn.vn/"
    lateinit var view : View
    lateinit var itemClick:(position:Int)->Unit
    fun setCallBack(click:(position:Int)->Unit){
        itemClick = click
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song_search,parent,false)
        this.view = view
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var song = listSong[position]

        Log.e("search adapter", song.toString())
        holder.tvTitle.text = song.name
        holder.tvSinger.text = song.artist
        Glide.with(view).load("$urlThumb${song.thumb}").into(holder.ivImage)
        holder.tvTime.text = millionSecondsToTime((song.duration*1000).toLong())
    }

    override fun getItemCount(): Int {
        return listSong.size
    }

    inner class ViewHolder(view:View):RecyclerView.ViewHolder(view) {
        var tvTitle: TextView = view.findViewById(R.id.item_search_tvTitle)
        var tvSinger: TextView = view.findViewById(R.id.item_search_tvSinger)
        var tvTime: TextView = view.findViewById(R.id.item_search_tvTime)
        var ivImage: ImageView = view.findViewById(R.id.item_search_ivImage)
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