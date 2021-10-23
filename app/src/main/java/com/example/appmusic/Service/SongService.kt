package com.example.appmusic.Service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.appmusic.MainActivity
import com.example.appmusic.Receiver.MyApplication.Companion.CHANNEL_ID
import com.example.appmusic.Model.MySong
import com.example.appmusic.R
import com.example.appmusic.Receiver.SongReceiver
import com.example.demoretrofit.IRetrofit
import com.example.demoretrofit.Model.ResultRecommend
import com.example.demoretrofit.Model.Song
import com.example.demoretrofit.MyRetrofit
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SongService : Service() {
    companion object {
        lateinit var sharedPreferences: SharedPreferences
        var listHistory: MutableList<MySong> = mutableListOf()
        var listRecommend: MutableList<Song> = mutableListOf()
        var mediaPlayer = MediaPlayer()
        var isPlaying = false
        var isDisplay = false
        var isDestroy = false
        lateinit var currentSong: MySong
        const val ON_PAUSE = 11
        const val ON_START = 12
        const val ON_RESUME = 13
        const val ON_STOP = 14
        const val ON_PREVIOUS = 15
        const val ON_NEXT = 16
        const val ON_DONE = 19
        const val ON_RECOMMEND = 17
    }
    var offlineSongs: MutableList<MySong> = mutableListOf()

    private val myBinder = MyBinder()

    inner class MyBinder : Binder() {
        fun getSongService(): SongService = this@SongService
    }

    override fun onBind(intent: Intent): IBinder {
        return myBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences("SharePreferences", Context.MODE_PRIVATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer != null) {
            mediaPlayer.stop()
        }
        isPlaying = false
        isDisplay = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var action = intent?.getIntExtra("action", 0)
        manageMusic(action)
        return START_NOT_STICKY
    }

    fun manageMusic(action: Int?) {
        when (action) {
            ON_START -> {
                startSong()
            }
            ON_PAUSE -> {
                pauseSong()
            }
            ON_STOP -> {
                if (isDestroy) {
                    stopSelf()
                }
            }
            ON_RESUME -> {
                resumeSong()
            }
            ON_PREVIOUS -> {
                previousSong()
                sendNotification()
                sendActiontoActivity(ON_PREVIOUS)
            }
            ON_NEXT -> {
                nextSong()
                sendNotification()
                sendActiontoActivity(ON_NEXT)
            }
        }
    }

    fun startSong() {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer()
            }
            if (mediaPlayer.isPlaying) {
                mediaPlayer.release()
            }
            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(this, Uri.parse(currentSong.data))
            mediaPlayer.prepare()
            mediaPlayer.start()
            if (currentSong.isOnline) {
                listHistory.add(currentSong)
            }
            isPlaying = true
            isDisplay = true
            mediaPlayer.setOnCompletionListener {
                var sharedPreferences = getSharedPreferences(
                    "SharePreferences",
                    Context.MODE_PRIVATE
                )
                var typeRepeat = sharedPreferences.getInt("typeRepeat", 0)
                when (typeRepeat) {
                    0 -> {
                        mediaPlayer.seekTo(0)
                        mediaPlayer.pause()
                        isPlaying = false
                        sendNotification()
                        sendActiontoActivity(ON_DONE)
                    }
                    1 -> {
                        mediaPlayer.seekTo(0)
                        mediaPlayer.start()
                    }
                    2 -> {
                        if(!checkConnectivity()){
                            mediaPlayer.seekTo(0)
                            mediaPlayer.pause()
                            isPlaying = false
                            sendNotification()
                            sendActiontoActivity(ON_DONE)
                        }
                        nextSong()
                    }
                }
            }
            if (currentSong.isOnline) {
                var type = "audio"
                var id = "${currentSong.id}"
                val iRetrofit = MyRetrofit.getRetrofit().create(IRetrofit::class.java)
                iRetrofit.getSongRecommend(type, id).enqueue(object : Callback<ResultRecommend> {
                    override fun onResponse(
                        call: Call<ResultRecommend>,
                        response: Response<ResultRecommend>
                    ) {
                        if (response.isSuccessful) {
                            var dataRespone = response.body()
                            if (dataRespone?.data?.items != null) {
                                listRecommend = dataRespone.data.items
                                sendActiontoActivity(ON_RECOMMEND)
                            } else {
                                Log.e("Service", "listRecommend null")
                            }
                        }
                    }

                    override fun onFailure(call: Call<ResultRecommend>, t: Throwable) {
                        Log.e("Service", "Recommend error")
                    }
                })
            } else {
                listRecommend.clear()
            }
            sendNotification()
            sendActiontoActivity(ON_START)
        } catch (ex: Exception) {
            ex.stackTrace
        }
    }

    fun startSongPrevious() {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer()
            }
            if (mediaPlayer.isPlaying) {
                mediaPlayer.release()
            }
            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(this, Uri.parse(currentSong.data))
            mediaPlayer.prepare()
            mediaPlayer.start()
            isPlaying = true
            isDisplay = true
            mediaPlayer.setOnCompletionListener {
                var sharedPreferences = getSharedPreferences(
                    "SharePreferences",
                    Context.MODE_PRIVATE
                )
                var typeRepeat = sharedPreferences.getInt("typeRepeat", 0)
                when (typeRepeat) {
                    0 -> {
                        mediaPlayer.seekTo(0)
                        mediaPlayer.pause()
                        isPlaying = false
                        sendNotification()
                        sendActiontoActivity(ON_DONE)
                    }
                    1 -> {
                        mediaPlayer.seekTo(0)
                        mediaPlayer.start()
                    }
                    2 -> {
                        if(!checkConnectivity()){
                            mediaPlayer.seekTo(0)
                            mediaPlayer.pause()
                            isPlaying = false
                            sendNotification()
                            sendActiontoActivity(ON_DONE)
                        }
                        nextSong()
                    }
                }
            }
            if (currentSong.isOnline) {
                var type = "audio"
                var id = "${currentSong.id}"
                val iRetrofit = MyRetrofit.getRetrofit().create(IRetrofit::class.java)
                iRetrofit.getSongRecommend(type, id).enqueue(object : Callback<ResultRecommend> {
                    override fun onResponse(
                        call: Call<ResultRecommend>,
                        response: Response<ResultRecommend>
                    ) {
                        if (response.isSuccessful) {
                            var dataRespone = response.body()
                            if (dataRespone?.data?.items != null) {
                                listRecommend = dataRespone.data.items
                                sendActiontoActivity(ON_RECOMMEND)
                            } else {
                                Log.e("service", "listRecommend null")
                            }
                        }
                    }

                    override fun onFailure(call: Call<ResultRecommend>, t: Throwable) {
                        Log.e("Service", "Recommend error")
                    }
                })
            } else {
                listRecommend.clear()
            }
            sendNotification()
            sendActiontoActivity(ON_START)
        } catch (ex: Exception) {
            ex.stackTrace
        }
    }

    fun pauseSong() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause()
            isPlaying = false
        }
        sendNotification()
        sendActiontoActivity(ON_PAUSE)
    }

    fun resumeSong() {
        if (mediaPlayer != null && !isPlaying) {
            mediaPlayer.start()
            isPlaying = true
        }
        sendNotification()
        sendActiontoActivity(ON_RESUME)
    }

    fun nextSong() {
        if (currentSong.isOnline) {
            if(checkConnectivity()) {
                var isShuffle = sharedPreferences.getBoolean("isShuffle", false)
                var song: Song
                if (isShuffle) {
                    song = listRecommend.random()
                } else {
                    song = listRecommend[0]
                }
                var data = "http://api.mp3.zing.vn/api/streaming/audio/${song.id}/128"
                var duration: Long = (song.duration * 1000).toLong()
                var nextSong = MySong(
                    song.id,
                    song.title,
                    song.artists_names,
                    song.title,
                    data,
                    duration,
                    song.thumbnail,
                    true
                )
                currentSong = nextSong
                startSong()
            }
        } else {
            currentSong = getNextOfflineSong(currentSong)
            startSong()
        }
    }

    fun previousSong() {
        if (currentSong.isOnline) {
            if(checkConnectivity()) {
                if (listHistory != null && listHistory.size > 1) {
                    listHistory.remove(currentSong)
                    currentSong = listHistory[listHistory.size - 1]
                    for (a in listHistory) {
                        Log.e("history", "current song $a")
                    }
                    startSongPrevious()
                } else {
                    Toast.makeText(this, "This is the first song", Toast.LENGTH_SHORT).show()
                    sendActiontoActivity(ON_PREVIOUS)
                }
            }
        } else {
            var indexSong = 0
            loadSongs()
            if (offlineSongs.size < 1) {
                startSongPrevious()
            } else {
                for (i in 0 until offlineSongs.size) {
                    if (offlineSongs[i].id == currentSong.id)
                        indexSong = i
                }
                if (indexSong == (0))
                    indexSong = offlineSongs.size - 1
                else
                    indexSong--
                currentSong = offlineSongs[indexSong]
                startSongPrevious()
            }
        }
    }

    fun sendNotification() {
        val remoteViews = RemoteViews(packageName, R.layout.notification)
        if (currentSong != null) {
            remoteViews.setTextViewText(R.id.notification_tvTitle, "${currentSong.title}")
            remoteViews.setTextViewText(R.id.notification_tvText, "${currentSong.artist}")
        }
        if (isPlaying) {
            remoteViews.setImageViewResource(R.id.notification_btnPlay, R.drawable.ic_pause)
            remoteViews.setOnClickPendingIntent(R.id.notification_btnPlay, getPendingIntent(this,ON_PAUSE))
        } else {
            remoteViews.setImageViewResource(R.id.notification_btnPlay, R.drawable.ic_play_arrow)
            remoteViews.setOnClickPendingIntent(R.id.notification_btnPlay, getPendingIntent(this,ON_RESUME))
        }
        remoteViews.setOnClickPendingIntent(R.id.notification_btnNext_song, getPendingIntent(this,ON_NEXT))
        remoteViews.setOnClickPendingIntent(R.id.notification_btnPrevious_song, getPendingIntent(this,ON_PREVIOUS))
        remoteViews.setOnClickPendingIntent(R.id.notification_btnClose, getPendingIntent(this,ON_STOP))

        var intent = Intent(this, MainActivity::class.java)
        isDisplay = true
        var pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT)
        var notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.music)
            .setContentTitle("Title")
            .setContentText("name")
            .setContentIntent(pendingIntent)
            .setCustomContentView(remoteViews)
        startForeground(1, notification.build())
    }

    fun getPendingIntent(context: Context, action: Int): PendingIntent {
        var intent = Intent(this, SongReceiver::class.java)
        intent.putExtra("action", action)
        return PendingIntent.getBroadcast(context.applicationContext,action,intent,PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun sendActiontoActivity(action: Int) {
        var intent = Intent("ac_service_to_main")
        var bundle = Bundle()
        bundle.putInt("action", action)
        intent.putExtras(bundle)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun getNextOfflineSong(curSong: MySong): MySong {
        var isShuffle = sharedPreferences.getBoolean("isShuffle", false)
        loadSongs()
        if (offlineSongs.size > 1) {
            var listSong: MutableList<MySong> = mutableListOf()
            var indexSong = 0
            for (i in 0 until offlineSongs.size) {
                if (offlineSongs[i].id != curSong.id)
                    listSong.add(offlineSongs[i])
                else {
                    indexSong = i
                }
            }
            return if (isShuffle) {
                listSong.random()
            } else {
                indexSong++
                if (indexSong == offlineSongs.size)
                    indexSong = 0
                offlineSongs[indexSong]
            }
        }
        return curSong
    }
    private fun checkConnectivity(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivityManager.activeNetworkInfo
        return if (info == null || !info.isConnected || !info.isAvailable) {
            Toast.makeText(baseContext, "No internet connection", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
        return false
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
        val cursor = contentResolver?.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null
        )
        while (cursor!!.moveToNext()) {
            if (cursor.getLong(5) > 0) {
                offlineSongs.add(
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
    }
}