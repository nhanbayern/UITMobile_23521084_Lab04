package com.example.lab04combined.tv

data class MovieItem(
    val title: String,
    val overview: String,
    val releaseYear: String,
    val rating: String,
    val posterPath: String? = null,
    val backdropPath: String? = null
) {
    val posterUrl: String?
        get() = posterPath?.let { "$TMDB_IMAGE_BASE/w342$it" }

    val backdropUrl: String?
        get() = backdropPath?.let { "$TMDB_IMAGE_BASE/w1280$it" }

    companion object {
        private const val TMDB_IMAGE_BASE = "https://image.tmdb.org/t/p"
    }
}
