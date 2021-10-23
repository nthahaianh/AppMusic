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
import com.example.appmusic.Adapter.SongAdapter
import com.example.appmusic.Model.MySong
import com.example.appmusic.Service.SongService
import com.example.demoretrofit.IRetrofit
import com.example.demoretrofit.Model.ResultChart
import com.example.demoretrofit.Model.Song
import com.example.demoretrofit.MyRetrofit
import kotlinx.android.synthetic.main.fragment_home.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {
    lateinit var iRetrofit: IRetrofit
    lateinit var listTopSong: MutableList<Song>
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
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        iRetrofit = MyRetrofit.getRetrofit().create(IRetrofit::class.java)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        if (checkConnectivity()){
            getCharts()
//        }else{
//            home_progressBar.visibility = View.GONE
//            home_btnReload.visibility = View.VISIBLE
//            home_btnReload.setOnClickListener {
//                home_progressBar.visibility = View.VISIBLE
//                home_btnReload.visibility = View.GONE
//                getCharts()
//            }
//        }
    }

    private fun getCharts() {
        if (checkConnectivity()) {
            iRetrofit.getChart30().enqueue(object : Callback<ResultChart> {
                override fun onResponse(call: Call<ResultChart>, response: Response<ResultChart>) {
                    if (response.isSuccessful) {
                        listTopSong = mutableListOf()
                        var dataRespone = response.body()
                        if (dataRespone?.data?.song != null) {
                            listTopSong = dataRespone.data.song
                            for (a in listTopSong) {
                                Log.e("chart", a.toString())
                            }
                            setUpChart()
                            Log.e("homeFragment", "listMySong not null")
                            home_progressBar.visibility = View.GONE
                        } else {
                            Log.e("homeFragment", "listMySong null")
                        }
                    }
                }

                override fun onFailure(call: Call<ResultChart>, t: Throwable) {
                    Log.e("homeFragment", "Load error")
                    home_progressBar.visibility = View.GONE
                }
            })
        } else {
            home_progressBar.visibility = View.GONE
            home_btnReload.visibility = View.VISIBLE
            home_btnReload.setOnClickListener {
                home_progressBar.visibility = View.VISIBLE
                home_btnReload.visibility = View.GONE
                getCharts()
            }
        }
    }

    private fun setUpChart() {
        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        var adapter = SongAdapter(listTopSong)
        adapter.setCallBack {
            val song = listTopSong[it]
            val id = song.id
            val title = song.title
            var artist: String = song.artists_names
            var displayName = "${song.title} - ${song.artists_names}"
            var data = "http://api.mp3.zing.vn/api/streaming/audio/${song.id}/128"
            var duration: Long = (song.duration * 1000).toLong()

            var mySong =
                MySong(id, title, artist, displayName, data, duration, song.thumbnail, true)
            SongService.currentSong = mySong

            val intent = Intent(context, SongService::class.java)
            intent.putExtra("action", SongService.ON_START)
            activity?.startService(intent)
            val intentSong = Intent(context, SongActivity::class.java)
            startActivity(intentSong)
        }
        home_rvCharts?.layoutManager = layoutManager
        home_rvCharts?.adapter = adapter
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