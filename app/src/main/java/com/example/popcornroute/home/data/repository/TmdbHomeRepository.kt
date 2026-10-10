package com.example.popcornroute.home.data.repository

import com.example.popcornroute.home.domain.model.HomeContent
import com.example.popcornroute.home.domain.model.HomeMedia
import com.example.popcornroute.home.domain.model.HomeMediaType
import com.example.popcornroute.home.domain.repository.HomeRepository
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class TmdbHomeRepository(
    private val client: OkHttpClient,
    private val readAccessToken: String,
    private val apiKey: String,
    private val baseUrl: String = DEFAULT_BASE_URL,
) : HomeRepository {
    override suspend fun getHomeContent(): HomeContent = withContext(Dispatchers.IO) {
        check(readAccessToken.isNotBlank() || apiKey.isNotBlank()) {
            "TMDB credentials are missing from local.properties"
        }

        coroutineScope {
            val requests = listOf(
                async { getMedia("trending/all/week", defaultType = null, limit = 8) },
                async { getMedia("trending/movie/week", HomeMediaType.MOVIE, limit = 10) },
                async { getMedia("trending/tv/week", HomeMediaType.TV, limit = 10) },
                async { getMedia("movie/now_playing", HomeMediaType.MOVIE, limit = 20) },
                async { getMedia("movie/upcoming", HomeMediaType.MOVIE, limit = 20) },
            ).awaitAll()

            HomeContent(
                featured = requests[0],
                topMovies = requests[1],
                topShows = requests[2],
                nowPlaying = requests[3],
                comingSoon = requests[4],
            )
        }
    }

    private fun getMedia(
        path: String,
        defaultType: HomeMediaType?,
        limit: Int,
    ): List<HomeMedia> {
        val urlBuilder = "$baseUrl/$path".toHttpUrl().newBuilder()
            .addQueryParameter("language", "en-US")
            .addQueryParameter("page", "1")
        if (readAccessToken.isBlank()) {
            urlBuilder.addQueryParameter("api_key", apiKey)
        }

        val requestBuilder = Request.Builder()
            .url(urlBuilder.build())
            .header("Accept", "application/json")
        if (readAccessToken.isNotBlank()) {
            requestBuilder.header("Authorization", "Bearer $readAccessToken")
        }

        client.newCall(requestBuilder.build()).execute().use { response ->
            if (!response.isSuccessful) {
                throw TmdbHttpException(response.code)
            }
            val body = response.body?.string() ?: throw IOException("TMDB returned an empty response")
            val results = JSONObject(body).getJSONArray("results")
            return buildList {
                for (index in 0 until results.length()) {
                    val item = results.getJSONObject(index)
                    val media = item.toHomeMedia(defaultType) ?: continue
                    add(media)
                    if (size == limit) break
                }
            }
        }
    }

    private fun JSONObject.toHomeMedia(defaultType: HomeMediaType?): HomeMedia? {
        val type = when (optString("media_type")) {
            "movie" -> HomeMediaType.MOVIE
            "tv" -> HomeMediaType.TV
            else -> defaultType
        } ?: return null
        val title = when (type) {
            HomeMediaType.MOVIE -> optString("title")
            HomeMediaType.TV -> optString("name")
        }.takeIf(String::isNotBlank) ?: return null
        val date = when (type) {
            HomeMediaType.MOVIE -> optString("release_date")
            HomeMediaType.TV -> optString("first_air_date")
        }

        return HomeMedia(
            tmdbId = getInt("id"),
            mediaType = type,
            title = title,
            posterUrl = imageUrl(optNullableString("poster_path"), POSTER_SIZE),
            backdropUrl = imageUrl(optNullableString("backdrop_path"), BACKDROP_SIZE),
            year = date.takeIf { it.length >= 4 }?.take(4),
        )
    }

    private fun JSONObject.optNullableString(name: String): String? =
        if (isNull(name)) null else optString(name).takeIf(String::isNotBlank)

    private fun imageUrl(path: String?, size: String): String? =
        path?.let { "$IMAGE_BASE_URL/$size$it" }

    private companion object {
        const val DEFAULT_BASE_URL = "https://api.themoviedb.org/3"
        const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p"
        const val POSTER_SIZE = "w500"
        const val BACKDROP_SIZE = "w780"
    }
}

class TmdbHttpException(val statusCode: Int) : IOException("TMDB request failed with HTTP $statusCode")
