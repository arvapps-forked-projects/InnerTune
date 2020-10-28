package com.zionhuang.music.youtube

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.preference.PreferenceManager
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.SearchListResponse
import com.google.api.services.youtube.model.VideoListResponse
import com.zionhuang.music.R
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class YouTubeAPIService(context: Context) : OnSharedPreferenceChangeListener {
    companion object {
        private const val TAG = "YoutubeAPIService"
        private var API_KEY: String = ""
        private const val REGION_CODE = "TW"
        private const val CATEGORY_MUSIC = "10"
    }

    private val API_KEY_RID: String = context.getString(R.string.api_key)
    private val mYouTube: YouTube = YouTube.Builder(NetHttpTransport.Builder().build(), GsonFactory(), null)
            .setApplicationName(context.resources.getString(R.string.app_name))
            .build()

    init {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        API_KEY = sharedPreferences.getString(API_KEY_RID, "").toString()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    fun search(query: String, pageToken: String?): Single<SearchListResponse> {
        return Single.fromCallable {
            mYouTube.search().list("snippet")
                    .setKey(API_KEY)
                    .setQ(query)
                    .setPageToken(pageToken)
                    .setMaxResults(20L)
                    .execute()
        }
    }

    suspend fun searchAsync(query: String, pageToken: String?): SearchListResponse = withContext(Dispatchers.IO) {
        return@withContext mYouTube.search().list("snippet")
                .setKey(API_KEY)
                .setQ(query)
                .setPageToken(pageToken)
                .setMaxResults(20L)
                .execute()
    }

    fun popularMusic(pageToken: String?): Single<VideoListResponse> {
        return Single.fromCallable {
            mYouTube.videos().list("snippet,contentDetails,statistics")
                    .setKey(API_KEY)
                    .setChart("mostPopular")
                    .setVideoCategoryId(CATEGORY_MUSIC)
                    .setRegionCode(REGION_CODE)
                    .setPageToken(pageToken)
                    .setMaxResults(20L)
                    .execute()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == API_KEY_RID) {
            API_KEY = sharedPreferences.getString(key, "").toString()
        }
    }
}