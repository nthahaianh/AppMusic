package com.example.appmusic

import android.app.DownloadManager
import android.content.*
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.*
import android.util.Log
import android.webkit.CookieManager
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appmusic.Adapter.SongAdapter
import com.example.appmusic.SQLite.SQLHelper
import com.example.appmusic.Service.SongService
import com.example.appmusic.Service.SongService.Companion.currentSong
import com.example.demoretrofit.IRetrofit
import com.example.demoretrofit.MyRetrofit
import kotlinx.android.synthetic.main.activity_song.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL


class SongActivity : AppCompatActivity() {
    lateinit var sharedPreferences: SharedPreferences
    lateinit var iRetrofit: IRetrofit
    lateinit var sqlHelper: SQLHelper
    var typeRepeat: Int = 0
    var isShuffle = false
    var isFavourite = false
    private lateinit var songService: SongService
    var isSongServiceConnected = false
    private val connectSongService = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as SongService.MyBinder
            songService = binder.getSongService()
            isSongServiceConnected = true
            SongService.isDestroy = false
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isSongServiceConnected = false
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(baseContext, SongService::class.java)
        bindService(intent, connectSongService, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        unbindService(connectSongService)
        isSongServiceConnected = false
    }

    private val myBroadcast = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val bundle = intent!!.extras ?: return
            var action = bundle.getInt("action")
            updateUI(action)
        }
    }

    private fun updateUI(action: Int) {
        when (action) {
            SongService.ON_START -> {
                SongService.isDisplay = false
                updateBtnPlay()
                updateImage()
                try {
                    isFavourite = sqlHelper.isExists(currentSong.id)
                    updateIconFavourite()
                } catch (e: Exception) {
                    e.stackTrace
                }
                song_tvTitle.text = currentSong.displayName
                song_tvSinger.text = currentSong.artist
                setUpSeekBar()
            }
            SongService.ON_PAUSE -> {
                SongService.isPlaying = false
                updateBtnPlay()
            }
            SongService.ON_DONE -> {
                SongService.isPlaying = false
                updateBtnPlay()
            }
            SongService.ON_RESUME -> {
                SongService.isPlaying = true
                updateBtnPlay()
            }
            SongService.ON_RECOMMEND -> {
                setUpRecommend()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(myBroadcast, IntentFilter("ac_service_to_main"))
        sqlHelper = SQLHelper(baseContext)
        iRetrofit = MyRetrofit.getRetrofit().create(IRetrofit::class.java)
        sharedPreferences = getSharedPreferences("SharePreferences", Context.MODE_PRIVATE)
        typeRepeat = sharedPreferences.getInt("typeRepeat", 0)
        isShuffle = sharedPreferences.getBoolean("isShuffle", false)
        song_tvTitle.text = currentSong.displayName
        song_tvSinger.text = currentSong.artist

        updateImage()
        updateIconRepeat()
        updateIconShuffle()
        updateIconFavourite()
        updateBtnPlay()
        setUpSeekBar()
        try {
            isFavourite = sqlHelper.isExists(currentSong.id)
            Log.e("song-act", "isFavourite - sql${isFavourite}")
            updateIconFavourite()
        } catch (e: Exception) {
            e.stackTrace
        }
        song_ivDownload.setOnClickListener {
            if(currentSong.isOnline){
                downloadSong()
            }else{
                Toast.makeText(baseContext,"This song is available",Toast.LENGTH_SHORT).show()
            }
        }
        song_ivBack.setOnClickListener { finish() }
        song_ivFavourite.setOnClickListener {
            try {
                if (isFavourite) {
                    sqlHelper.removeSong(currentSong.id)
                    Log.e("song-ivFa", "remove sql")
                } else {
                    sqlHelper.addSong(currentSong)
                    Log.e("song-ivFa", "add sql ")
                }
            } catch (e: Exception) {
                e.stackTrace
            }
            isFavourite = !isFavourite
            updateIconFavourite()
        }
        song_ivShuffle.setOnClickListener {
            isShuffle = !isShuffle
            updateIconShuffle()
        }
        song_ivTypeRepeat.setOnClickListener {
            when (typeRepeat) {
                2 -> typeRepeat = 0
                else -> typeRepeat++
            }
            updateIconRepeat()
        }
        song_btnPlay.setOnClickListener {
            if (SongService.isPlaying) {
                songService.pauseSong()
            } else {
                songService.resumeSong()
            }
            updateBtnPlay()
        }

        song_btnNext_song.setOnClickListener {
            val intent = Intent(baseContext, SongService::class.java)
            intent.putExtra("action", SongService.ON_NEXT)
            startService(intent)
        }
        song_btnPrevious_song.setOnClickListener {
            val intent = Intent(baseContext, SongService::class.java)
            intent.putExtra("action", SongService.ON_PREVIOUS)
            startService(intent)
        }
        setUpRecommend()
    }

    private fun downloadSong() {
        try {
            var url = "http://api.mp3.zing.vn/api/streaming/audio/${currentSong.id}/128"
            var request = DownloadManager.Request(Uri.parse(url))
            var title = "${currentSong.title}.mp3"
            request.setTitle(title)
            request.setDescription("Downloading")
            var cookie = CookieManager.getInstance().getCookie(url)
            request.addRequestHeader("cookie",cookie)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,title)
            var downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
            Toast.makeText(this, "Download...", Toast.LENGTH_SHORT).show()
        }catch (e: IOException){
            e.stackTrace
            Log.e("IOException","IOException when download")
        }catch (e: Exception){
            e.stackTrace
            Log.e("Exception","Download  fail")
            Toast.makeText(this, "Download fail", Toast.LENGTH_SHORT).show()
        }finally {
        }
    }

    private fun updateImage() {
        try {
            if (currentSong.isOnline) {
                Glide.with(baseContext).load(Uri.parse(currentSong.img)).into(song_ivImage)
            } else {
                var mmr = MediaMetadataRetriever()
                mmr.setDataSource(baseContext, Uri.parse(currentSong.img))
                val byteImage = mmr.embeddedPicture
                var bitmap = BitmapFactory.decodeByteArray(byteImage, 0, byteImage!!.size)
                Glide.with(baseContext).load(bitmap).into(song_ivImage)
            }
        } catch (e: Exception) {
            e.stackTrace
            Glide.with(baseContext).load(R.drawable.music).into(song_ivImage)
        }
    }

    private fun setUpRecommend() {
        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(baseContext, LinearLayoutManager.VERTICAL, false)
        var adapter = SongAdapter(SongService.listRecommend)
        adapter.setCallBack {
            val song = SongService.listRecommend[it]
            val id = song.id
            val title = song.title
            var artist: String = song.artists_names
            var displayName = "${song.title} - ${artist}"
            var data = "http://api.mp3.zing.vn/api/streaming/audio/${song.id}/128"
            var duration: Long = (song.duration * 1000).toLong()
            var mySong = MySong(id, title, artist, displayName, data, duration, song.thumbnail, true)
            currentSong = mySong
            val intent = Intent(baseContext, SongService::class.java)
            intent.putExtra("action", SongService.ON_START)
            startService(intent)
        }
        song_rvRecommend.layoutManager = layoutManager
        song_rvRecommend.adapter = adapter
    }

    private fun updateBtnPlay() {
        var isPlaySong = SongService.isPlaying
        if (isPlaySong) {
            song_btnPlay?.setImageResource(R.drawable.ic_pause)
        } else {
            song_btnPlay?.setImageResource(R.drawable.ic_play_arrow)
        }
    }

    private fun updateIconRepeat() {
        when (typeRepeat) {
            0 -> {
                song_ivTypeRepeat.setImageResource(R.drawable.ic_no_repeat)
            }
            1 -> {
                song_ivTypeRepeat.setImageResource(R.drawable.ic_repeat_one)
            }
            2 -> {
                song_ivTypeRepeat.setImageResource(R.drawable.ic_repeat)
            }
        }
        val editor = sharedPreferences.edit()
        editor.putInt("typeRepeat", typeRepeat)
        editor.apply()
    }

    private fun updateIconShuffle() {
        if (isShuffle) {
            song_ivShuffle.setImageResource(R.drawable.ic__shuffle_choose)
        } else {
            song_ivShuffle.setImageResource(R.drawable.ic_shuffle)
        }
        val editor = sharedPreferences.edit()
        editor.putBoolean("isShuffle", isShuffle)
        editor.apply()
    }

    private fun updateIconFavourite() {
        if (isFavourite) {
            song_ivFavourite.setImageResource(R.drawable.ic_baseline_favorite_24)
        } else {
            song_ivFavourite.setImageResource(R.drawable.ic_not_favorite)
        }
    }

    private fun setUpSeekBar() {
//        updateBtnPlay()
        var nowSong = currentSong
        song_tvTitle.text = nowSong.title

        val totalDuration = nowSong.duration
        var currentPos = SongService.mediaPlayer.currentPosition
        song_tvMaxTime.text = millionSecondsToTime(totalDuration)
        song_tvCurrentTime.text = intToTime(currentPos)
        song_seekBar.max = totalDuration.toInt()
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                currentPos = SongService.mediaPlayer.currentPosition
                song_tvCurrentTime?.text = intToTime(currentPos)
                song_seekBar?.progress = currentPos
                handler.postDelayed(this, 1000)
            }
        }
        handler.postDelayed(runnable, 1000)
        song_seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar,
                progress: Int,
                fromUser: Boolean
            ) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                SongService.mediaPlayer.seekTo(seekBar.progress)
            }

        })
    }

    fun millionSecondsToTime(milliSeconds: Long): String {
        val hours = milliSeconds / (1000 * 60 * 60)
        val minutes = (milliSeconds % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (milliSeconds % (1000 * 60 * 60)) % (1000 * 60) / 1000
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun intToTime(milliSeconds: Int): String {
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