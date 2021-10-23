package com.example.appmusic

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appmusic.Adapter.FavouriteSongAdapter
import com.example.appmusic.Model.MySong
import com.example.appmusic.SQLite.SQLHelper
import com.example.appmusic.Service.SongService
import kotlinx.android.synthetic.main.fragment_favourite.*

class FavouriteFragment: Fragment() {
    lateinit var sqlHelper: SQLHelper
    var favouriteList: MutableList<MySong> = mutableListOf()
    private lateinit var songService: SongService
    var isSongServiceConnected = false
    private val connectSongService = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as SongService.MyBinder
            songService = binder.getSongService()
            isSongServiceConnected = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isSongServiceConnected = false
        }
    }
    override fun onStart() {
        super.onStart()
        val intent = Intent(context, SongService::class.java)
        activity?.bindService(intent, connectSongService, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        activity?.unbindService(connectSongService)
        isSongServiceConnected = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favourite,container,false)
        sqlHelper = SQLHelper(context)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            favouriteList = sqlHelper.getAll()
            val layoutManager: RecyclerView.LayoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            var adapter = FavouriteSongAdapter(context,favouriteList)
            adapter.setCallBack {
                if (favouriteList[it].isOnline){
                    if (checkConnectivity()){
                        SongService.currentSong = favouriteList[it]
                        val intent = Intent(context, SongService::class.java)
                        intent.putExtra("action", SongService.ON_START)
                        activity?.startService(intent)
                        val intentSong = Intent(context,SongActivity::class.java)
                        startActivity(intentSong)
                    }
                }else{
                    SongService.currentSong = favouriteList[it]
                    val intent = Intent(context, SongService::class.java)
                    intent.putExtra("action", SongService.ON_START)
                    activity?.startService(intent)
                    val intentSong = Intent(context,SongActivity::class.java)
                    startActivity(intentSong)
                }
            }
            favourite_rvFavourite.layoutManager = layoutManager
            favourite_rvFavourite.adapter = adapter
        }catch (e:Exception){
            e.stackTrace
            Log.e("favourite-SQL","Read SQL error")
        }
    }

    private fun checkConnectivity(): Boolean {
        val connectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivityManager.activeNetworkInfo
        return if (info == null || !info.isConnected || !info.isAvailable) {
            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
        return false
    }
}