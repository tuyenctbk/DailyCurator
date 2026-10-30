package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import android.content.Context
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object NetworkClient {
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val builder = OkHttpClient.Builder()

        appContext?.let { ctx ->
            val cacheSize = 50L * 1024L * 1024L // 50 MB cache
            val cache = Cache(File(ctx.cacheDir, "http_cache"), cacheSize)
            builder.cache(cache)

            // Offline cache interceptor: allow stale cache up to 30 days when offline
            builder.addInterceptor(Interceptor { chain ->
                var request = chain.request()
                val cacheControl = "public, max-stale=" + (60 * 60 * 24 * 30)
                request = request.newBuilder()
                    .header("Cache-Control", cacheControl)
                    .build()
                chain.proceed(request)
            })

            // Network cache interceptor: cache online responses for 7 days
            builder.addNetworkInterceptor(Interceptor { chain ->
                val response = chain.proceed(chain.request())
                val cacheControl = "public, max-age=" + (60 * 60 * 24 * 7)
                response.newBuilder()
                    .header("Cache-Control", cacheControl)
                    .removeHeader("Pragma")
                    .build()
            })
        }

        builder
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Unified Retrofit API Service supporting both The Metropolitan Museum of Art API
     * and the Art Institute of Chicago API.
     */
    val artworkApiService: ArtworkApiService by lazy {
        Retrofit.Builder()
            .baseUrl(ArtworkApiService.MET_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ArtworkApiService::class.java)
    }

    val metApi: MetMuseumApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://collectionapi.metmuseum.org/public/collection/v1/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(MetMuseumApi::class.java)
    }

    val articApi: ArticApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.artic.edu/api/v1/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ArticApi::class.java)
    }
}
