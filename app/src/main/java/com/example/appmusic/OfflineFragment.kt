package com.example.appmusic

import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appmusic.Adapter.MySongAdapter
import com.example.appmusic.Service.SongService
import com.example.appmusic.Service.SongService.Companion.ON_START
import com.example.appmusic.Service.SongService.Companion.isOnline
import kotlinx.android.synthetic.main.fragment_offline.*

class OfflineFragment: Fragment() {
    companion object {

    }
    var songs: MutableList<MySong> = mutableListOf()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_offline,container,false)
        loadSongs()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        var adapter = MySongAdapter(songs)
        offline_rvSongs.layoutManager = layoutManager
        offline_rvSongs.adapter = adapter
        adapter.setCallBack {
            isOnline = false
            Toast.makeText(context,"${songs[it].displayName}", Toast.LENGTH_SHORT).show()
            val intent = Intent(context, SongService::class.java)
            SongService.currentSong = songs[it]
            intent.putExtra("action",ON_START)
            activity?.startService(intent)
            val intentSong = Intent(context,SongActivity::class.java)
            intent.putExtra("title_song",songs[it].displayName)
            startActivity(intentSong)
        }
    }

    private fun loadSongs() {
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION
        )
        val cursor = activity?.contentResolver?.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null
        )
        while (cursor!!.moveToNext()) {
            if (cursor.getLong(5)>0){
                songs.add(
                    MySong(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getLong(5),
                        cursor.getString(4),
                        false
                    )
                )
            }
        }
        for (song in songs) {
            Log.e("Sv-Song", "$song")
        }
    }
}