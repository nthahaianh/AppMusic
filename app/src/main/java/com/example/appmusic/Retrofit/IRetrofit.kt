package com.example.demoretrofit

import com.example.demoretrofit.Model.ResultChart
import com.example.demoretrofit.Model.ResultRecommend
import com.example.demoretrofit.Model.ResultSearch
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface IRetrofit {
    @GET("xhr/chart-realtime")
    fun getChart30(): Call<ResultChart>

    @GET("xhr/recommend")
    fun getSongRecommend(@Query("type") type: String, @Query("id") id: String): Call<ResultRecommend>

    @GET("complete")
    fun getResultSearch(@Query("type") type:String, @Query("num") num:Int, @Query("query") query:String ):Call<ResultSearch>
}