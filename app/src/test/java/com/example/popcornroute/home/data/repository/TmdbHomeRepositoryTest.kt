package com.example.popcornroute.home.data.repository

import android.app.Application
import com.example.popcornroute.home.domain.model.HomeMediaType
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class TmdbHomeRepositoryTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer().apply {
            dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val type = if (request.path.orEmpty().contains("/tv/")) "tv" else "movie"
                    val body = if (type == "tv") tvResponse else movieResponse
                    return MockResponse().setResponseCode(200).setBody(body)
                }
            }
            start()
        }
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `loads and maps every home section`() = runTest {
        val repository = TmdbHomeRepository(
            client = OkHttpClient(),
            readAccessToken = "token",
            apiKey = "",
            baseUrl = server.url("3").toString().removeSuffix("/"),
        )

        val content = repository.getHomeContent()

        assertEquals(1, content.featured.size)
        assertEquals(HomeMediaType.MOVIE, content.featured.single().mediaType)
        assertEquals("Movie title", content.topMovies.single().title)
        assertEquals("Show title", content.topShows.single().title)
        assertEquals("2026", content.nowPlaying.single().year)
        assertEquals("https://image.tmdb.org/t/p/w500/poster.jpg", content.comingSoon.single().posterUrl)
        assertEquals(5, server.requestCount)
        repeat(5) {
            assertEquals("Bearer token", server.takeRequest().getHeader("Authorization"))
        }
    }

    @Test
    fun `uses api key when bearer token is absent`() = runTest {
        val repository = TmdbHomeRepository(
            client = OkHttpClient(),
            readAccessToken = "",
            apiKey = "key",
            baseUrl = server.url("3").toString().removeSuffix("/"),
        )

        repository.getHomeContent()

        repeat(5) {
            val request = server.takeRequest()
            assertTrue(request.requestUrl?.queryParameter("api_key") == "key")
        }
    }

    private companion object {
        const val movieResponse = """
            {
              "results": [
                {
                  "id": 10,
                  "media_type": "movie",
                  "title": "Movie title",
                  "poster_path": "/poster.jpg",
                  "backdrop_path": "/backdrop.jpg",
                  "release_date": "2026-07-01"
                }
              ]
            }
        """
        const val tvResponse = """
            {
              "results": [
                {
                  "id": 20,
                  "media_type": "tv",
                  "name": "Show title",
                  "poster_path": "/show.jpg",
                  "backdrop_path": null,
                  "first_air_date": "2025-03-01"
                }
              ]
            }
        """
    }
}
