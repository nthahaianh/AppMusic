package com.example.appmusic

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.appmusic.Service.SongService
import kotlinx.android.synthetic.main.fragment_favourite.*

class FavouriteFragment: Fragment() {
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
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        favourite_tvTitle.setOnClickListener {
        }
    }
}