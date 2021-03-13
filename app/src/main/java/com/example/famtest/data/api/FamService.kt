package com.example.famtest.data.api

import android.util.Log
import com.example.famtest.data.model.GroupCardsResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val TAG = "FamService"

interface FamService {
    @GET("fefcfbeb-5c12-4722-94ad-b8f92caad1ad")
    suspend fun fetchContextualCards(): GroupCardsResponse

    companion object {
        private const val BASE_URL = "https://run.mocky.io/v3/"

        fun create(): FamService {
            Log.e(TAG, "Create called...")
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                this.level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(FamService::class.java)
        }
    }
}