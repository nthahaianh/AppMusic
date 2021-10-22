package com.example.appmusic

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.appmusic.Service.SongService
import com.example.appmusic.Service.SongService.Companion.ON_NEXT
import com.example.appmusic.Service.SongService.Companion.ON_PAUSE
import com.example.appmusic.Service.SongService.Companion.ON_PREVIOUS
import com.example.appmusic.Service.SongService.Companion.ON_RESUME
import com.example.appmusic.Service.SongService.Companion.ON_START
import com.example.appmusic.Service.SongService.Companion.isDisplay
import com.example.appmusic.Service.SongService.Companion.isPlaying
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE_PERMISSIONS = 1
    lateinit var navHostFragment: NavHostFragment
    lateinit var controller: NavController
    private lateinit var songService:SongService
    var isSongServiceConnected = false

    private val myBroadcast = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val bundle = intent!!.extras ?: return
            var action=bundle.getInt("action")
            updateUI(action)
        }
    }

    private fun updateUI(action: Int) {
        when (action) {
            ON_START -> {
                isDisplay = true
                updateMusicManager()
            }
            ON_PAUSE -> {
                SongService.isPlaying = false
                updateBtnPlay()
            }
            SongService.ON_DONE-> {
                updateMusicManager()
                updateBtnPlay()
            }
            SongService.ON_STOP -> {
                isDisplay = false
                updateMusicManager()
            }
            ON_RESUME -> {
                SongService.isPlaying = true
                updateBtnPlay()
            }
            ON_PREVIOUS->{
                Toast.makeText(baseContext,"Previous",Toast.LENGTH_SHORT).show()
            }
            ON_NEXT->{
                Toast.makeText(baseContext,"Next",Toast.LENGTH_SHORT).show()
            }
        }
    }
    private val connectSongService = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as SongService.MyBinder
            songService = binder.getSongService()
            isSongServiceConnected = true
            SongService.isDestroy = false
            main_btnPlay.setOnClickListener {
                if(SongService.isPlaying){
                    songService.pauseSong()
                }else{
                    songService.resumeSong()
                }
                updateBtnPlay()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isSongServiceConnected = false
            SongService.isDestroy = true
        }
    }

    override fun onStart() {
        super.onStart()
        SongService.isDestroy = false
        Intent(this, SongService::class.java).also { intent ->
            bindService(intent, connectSongService, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(connectSongService)
        isSongServiceConnected = false
    }

    override fun onDestroy() {
        super.onDestroy()
        SongService.isDestroy = true
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermission()
        LocalBroadcastManager.getInstance(this).registerReceiver(myBroadcast, IntentFilter("ac_service_to_main"))
        navHostFragment = supportFragmentManager.findFragmentById(R.id.myNavHostFragment) as NavHostFragment
        controller = navHostFragment.navController
        main_bottomNavigationView.setupWithNavController(controller)

        updateMusicManager()
        main_tvTitle.setOnClickListener {
            val intent = Intent(this,SongActivity::class.java)
            intent.putExtra("title_song",SongService.currentSong.title)
            startActivity(intent)
        }
    }

    private fun updateMusicManager() {
        if (isPlaying){
            isDisplay=true
        }
        if (isDisplay) {
            main_clDisplay.visibility = View.VISIBLE
            main_tvTitle.text = SongService.currentSong.title
            updateBtnPlay()
        } else {
            main_clDisplay.visibility = View.GONE
        }
    }

    private fun updateBtnPlay() {
        if (SongService.isPlaying) {
            main_btnPlay.setImageResource(R.drawable.ic_pause)
        } else {
            main_btnPlay.setImageResource(R.drawable.ic_play_arrow)
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE_PERMISSIONS
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE) {
                    if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(this, "Please allow storage permission", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                    }
                }
            }
        }
    }
}