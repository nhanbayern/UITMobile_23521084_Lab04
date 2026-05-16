package com.example.lab04combined.tv

import com.example.lab04combined.BuildConfig
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class TmdbMovieRepository {
    fun loadPopularMovies(): List<MovieItem> {
        val token = BuildConfig.TMDB_BEARER_TOKEN.trim()
        if (token.isBlank()) return emptyList()

        val connection = (URL(POPULAR_MOVIES_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10000
            readTimeout = 10000
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("accept", "application/json")
        }

        return try {
            if (connection.responseCode !in 200..299) return emptyList()
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val results = JSONObject(body).getJSONArray("results")
            buildList {
                for (index in 0 until minOf(results.length(), 16)) {
                    val item = results.getJSONObject(index)
                    add(
                        MovieItem(
                            title = item.optString("title", "Untitled"),
                            overview = item.optString("overview"),
                            releaseYear = item.optString("release_date").take(4).ifBlank { "Movie" },
                            rating = String.format(Locale.US, "%.1f", item.optDouble("vote_average", 0.0)),
                            posterPath = item.optString("poster_path").takeIf { it.isNotBlank() && it != "null" },
                            backdropPath = item.optString("backdrop_path").takeIf { it.isNotBlank() && it != "null" }
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val POPULAR_MOVIES_URL =
            "https://api.themoviedb.org/3/movie/popular?language=en-US&page=1"
    }
}
