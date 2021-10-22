package com.example.demoretrofit

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MyRetrofit {
    companion object{
        const val baseUrl = " http://mp3.zing.vn/"
        const val baseUrlSearch = "http://ac.mp3.zing.vn/"
        fun getRetrofit(): Retrofit {
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        fun getRetrofitSearch(): Retrofit {
            return Retrofit.Builder()
                .baseUrl(baseUrlSearch)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }
}