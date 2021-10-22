package com.example.appmusic

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
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
import com.example.appmusic.Adapter.SearchListAdapter
import com.example.appmusic.Service.SongService
import com.example.demoretrofit.IRetrofit
import com.example.demoretrofit.Model.ResultSearch
import com.example.demoretrofit.Model.SongSearch
import com.example.demoretrofit.MyRetrofit
import kotlinx.android.synthetic.main.fragment_offline.*
import kotlinx.android.synthetic.main.fragment_search.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchFragment: Fragment() {
    lateinit var iSearchRetrofit: IRetrofit
    lateinit var listSearch: MutableList<SongSearch>
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
        val view = inflater.inflate(R.layout.fragment_search,container,false)
        iSearchRetrofit = MyRetrofit.getRetrofitSearch().create(IRetrofit::class.java)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listSearch = mutableListOf()
        search_btnSeach.setOnClickListener {
            var key = search_etSearch.text.toString()
            iSearchRetrofit.getResultSearch("name,artist,song",500,key).enqueue(object :
                Callback<ResultSearch> {
                override fun onResponse(
                    call: Call<ResultSearch>,
                    response: Response<ResultSearch>
                ) {
                    var dataRespone = response.body()
                    if (dataRespone?.data!=null && dataRespone.data.size>0){
                        var dataSearch = dataRespone.data[0].song
                        listSearch=dataSearch
                        for (a in listSearch){
                            Log.e("search", a.toString())
                        }
                        setUpResult()
                    } else {
                        Log.e("homeFragment","listSearch null")
                        Toast.makeText(context,"No result",Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResultSearch>, t: Throwable) {
                    Log.e("homeFragment","Search error")
                }
            })
        }
    }

    private fun setUpResult() {
        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        var adapter = SearchListAdapter(listSearch)
        adapter.setCallBack {
            val song = listSearch[it]
            val id = song.id
            val title = song.name
            var artist: String = song.artist
            var displayName = "${song.name} - ${song.artist}"
            var data = "http://api.mp3.zing.vn/api/streaming/audio/${song.id}/128"
            var duration: Long = (song.duration*1000).toLong()

            val urlThumb = "https://photo-zmp3.zadn.vn/"
            var mySong=MySong(id,title,artist,displayName,data,duration,"$urlThumb${song.thumb}",true)
            SongService.currentSong = mySong

            val intent = Intent(context, SongService::class.java)
            intent.putExtra("action", SongService.ON_START)
            activity?.startService(intent)

            val intentSong = Intent(context,SongActivity::class.java)
//            intent.putExtra("title_song",SongService.currentSong.displayName)
//            intent.putExtra("singer",artist)
            startActivity(intentSong)
        }
        search_rvSearch.layoutManager = layoutManager
        search_rvSearch.adapter = adapter
    }
}