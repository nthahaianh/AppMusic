package com.example.appmusic.Service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.appmusic.MainActivity
import com.example.appmusic.MyApplication.Companion.CHANNEL_ID
import com.example.appmusic.MySong
import com.example.appmusic.R
import com.example.appmusic.SongReceiver
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
        var songsFavourite: MutableList<MySong> = mutableListOf()
        var listRecommend: MutableList<Song> = mutableListOf()
        var mediaPlayer = MediaPlayer()
        var isPlaying = false
        var isDisplay = false
        var isDestroy = false
        var isOnline = false
        lateinit var currentSong:MySong
        lateinit var img:Bitmap
        const val ON_PAUSE = 11
        const val ON_START = 12
        const val ON_RESUME = 13
        const val ON_STOP = 14
        const val ON_PREVIOUS = 15
        const val ON_NEXT = 16
        const val ON_DONE = 19
        const val ON_RECOMMEND = 17
    }
    private val myBinder = MyBinder()

    inner class MyBinder : Binder() {
        fun getSongService(): SongService = this@SongService
    }
    override fun onBind(intent: Intent): IBinder {
//        Toast.makeText(this,"SongService - onBind",Toast.LENGTH_SHORT).show()
        return myBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
//        Toast.makeText(this,"SongService - onUnbind",Toast.LENGTH_SHORT).show()
    }
    override fun onCreate() {
        super.onCreate()
//        Toast.makeText(this,"SongService - onCreate",Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer != null) {
            mediaPlayer.stop()
        }
        isPlaying=false
        isDisplay=false
//        Toast.makeText(this,"SongService - onDestroy", Toast.LENGTH_SHORT).show()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        Toast.makeText(this,"SongService - onStartCommand", Toast.LENGTH_SHORT).show()
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
                sendActiontoActivity(ON_PREVIOUS)
            }
            ON_NEXT -> {
                sendActiontoActivity(ON_NEXT)
            }
        }
    }
    fun startSong() {
        try {
            if(mediaPlayer==null){
                mediaPlayer = MediaPlayer()
            }
            if (mediaPlayer.isPlaying) {
                mediaPlayer.release()
            }
            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(this, Uri.parse(currentSong.data))
            mediaPlayer.prepare()
            mediaPlayer.start()
            isPlaying=true
            isDisplay=true
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
                        startSong()
                    }
                    2 -> {
                        mediaPlayer.stop()
                        isPlaying = false
                        sendNotification()
                        sendActiontoActivity(ON_NEXT)
                        return@setOnCompletionListener
                    }
                }
            }
            if (isOnline){
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
                                for (a in listRecommend) {
                                    Log.e("recommend", a.toString())
                                }
                                Log.e("homeFragment", "listRecommend not null")
                                sendActiontoActivity(ON_RECOMMEND)
                            } else {
                                Log.e("homeFragment", "listRecommend null")
                            }
                        }
                    }

                    override fun onFailure(call: Call<ResultRecommend>, t: Throwable) {
                        Log.e("homeFragment", "Recommend error")
                    }

                })
            }
            sendNotification()
            sendActiontoActivity(ON_START)
        } catch (ex: Exception) {
            ex.stackTrace
        }
    }
    fun pauseSong(){
        if(mediaPlayer!=null && isPlaying){
            mediaPlayer.pause()
            isPlaying=false
        }
        sendNotification()
        sendActiontoActivity(ON_PAUSE)
    }
    fun resumeSong(){
        if(mediaPlayer!=null && !isPlaying){
            mediaPlayer.start()
            isPlaying=true
        }
        sendNotification()
        sendActiontoActivity(ON_RESUME)
    }
    fun nextSong(){

        Toast.makeText(this, "Next song", Toast.LENGTH_SHORT).show()
    }
    fun previousSong(){
        Toast.makeText(this, "Previous song", Toast.LENGTH_SHORT).show()

    }
    fun sendNotification(){
        val remoteViews = RemoteViews(packageName, R.layout.notification)
        if(currentSong!=null){
            remoteViews.setTextViewText(R.id.notification_tvTitle, "${currentSong.title}")
            remoteViews.setTextViewText(R.id.notification_tvText, "${currentSong.artist}")
        }
        if (isPlaying) {
            remoteViews.setImageViewResource(R.id.notification_btnPlay, R.drawable.ic_pause)
            remoteViews.setOnClickPendingIntent(
                R.id.notification_btnPlay, getPendingIntent(
                    this,
                    ON_PAUSE
                )
            )
        } else {
            remoteViews.setImageViewResource(R.id.notification_btnPlay, R.drawable.ic_play_arrow)
            remoteViews.setOnClickPendingIntent(
                R.id.notification_btnPlay, getPendingIntent(
                    this,
                    ON_RESUME
                )
            )
        }
        remoteViews.setOnClickPendingIntent(
            R.id.notification_btnNext_song, getPendingIntent(
                this,
                ON_NEXT
            )
        )
        remoteViews.setOnClickPendingIntent(
            R.id.notification_btnPrevious_song, getPendingIntent(
                this,
                ON_PREVIOUS
            )
        )
        remoteViews.setOnClickPendingIntent(
            R.id.notification_btnClose, getPendingIntent(
                this,
                ON_STOP
            )
        )

        var intent = Intent(this, MainActivity::class.java)
        isDisplay = true
        var pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
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
        return PendingIntent.getBroadcast(
            context.applicationContext,
            action,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
    private fun sendActiontoActivity(action: Int) {
        var intent = Intent("ac_service_to_main")
        var bundle = Bundle()
        bundle.putInt("action", action)
        intent.putExtras(bundle)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}