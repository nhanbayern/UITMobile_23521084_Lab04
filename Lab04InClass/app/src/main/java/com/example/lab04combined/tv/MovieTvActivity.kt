package com.example.lab04combined.tv

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lab04combined.databinding.ActivityMovieTvBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MovieTvActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMovieTvBinding
    private lateinit var imageLoader: TvImageLoader

    private val tmdbRepository = TmdbMovieRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieTvBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageLoader = TvImageLoader(lifecycleScope)
        setupRows()
        setupHeroActions()
        loadMovies()
    }

    private fun setupRows() {
        val favoriteAdapter = MovieAdapter(::showMovie, imageLoader)
        val trendingAdapter = MovieAdapter(::showMovie, imageLoader)

        binding.favoritesRow.apply {
            layoutManager = LinearLayoutManager(this@MovieTvActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = favoriteAdapter
        }
        binding.trendingRow.apply {
            layoutManager = LinearLayoutManager(this@MovieTvActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = trendingAdapter
        }

        val favorites = favoriteMovies()
        favoriteAdapter.submitList(favorites)
        trendingAdapter.submitList(favorites.reversed())
        showMovie(favorites.first())
    }

    private fun setupHeroActions() {
        val focusScale = View.OnFocusChangeListener { view, hasFocus ->
            view.animate()
                .scaleX(if (hasFocus) 1.05f else 1f)
                .scaleY(if (hasFocus) 1.05f else 1f)
                .setDuration(120L)
                .start()
        }
        binding.playButton.onFocusChangeListener = focusScale
        binding.moreButton.onFocusChangeListener = focusScale
    }

    private fun loadMovies() {
        binding.statusText.text = "Loading TMDB popular movies..."
        lifecycleScope.launch {
            val remoteMovies = withContext(Dispatchers.IO) {
                tmdbRepository.loadPopularMovies()
            }
            val movies = remoteMovies.ifEmpty { favoriteMovies() }
            (binding.trendingRow.adapter as MovieAdapter).submitList(movies)
            binding.trendingTitle.text = if (remoteMovies.isEmpty()) {
                "Offline picks"
            } else {
                "Trending from TMDB"
            }
            binding.statusText.text = if (remoteMovies.isEmpty()) {
                "TMDB token is missing or unavailable. Showing built-in homework data."
            } else {
                "TMDB data loaded. Move with D-pad and focus a card to update the hero."
            }
        }
    }

    private fun showMovie(movie: MovieItem) {
        binding.heroTitle.text = movie.title
        binding.heroMeta.text = "${movie.releaseYear}   Rating ${movie.rating}   UHD"
        binding.heroOverview.text = movie.overview
        imageLoader.load(movie.backdropUrl, binding.heroBackdrop)
    }

    private fun favoriteMovies(): List<MovieItem> = listOf(
        MovieItem(
            title = "Interstellar",
            overview = "A team of explorers travels through a wormhole in space to secure humanity's future.",
            releaseYear = "2014",
            rating = "8.4",
            posterPath = "/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
            backdropPath = "/pbrkL804c8yAv3zBZR4QPEafpAR.jpg"
        ),
        MovieItem(
            title = "Inception",
            overview = "A skilled thief enters dreams to steal secrets, then faces a final job built around planting an idea.",
            releaseYear = "2010",
            rating = "8.4",
            posterPath = "/oYuLEt3zVCKq57qu2F8dT7NIa6f.jpg",
            backdropPath = "/8ZTVqvKDQ8emSGUEMjsS4yHAwrp.jpg"
        ),
        MovieItem(
            title = "The Dark Knight",
            overview = "Batman faces a criminal mastermind whose chaos forces Gotham to question its heroes.",
            releaseYear = "2008",
            rating = "8.5",
            posterPath = "/qJ2tW6WMUDux911r6m7haRef0WH.jpg",
            backdropPath = "/nMKdUUepR0i5zn0y1T4CsSB5chy.jpg"
        ),
        MovieItem(
            title = "Dune: Part Two",
            overview = "Paul Atreides unites with Chani and the Fremen while choosing between love and the fate of the universe.",
            releaseYear = "2024",
            rating = "8.2",
            posterPath = "/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg",
            backdropPath = "/xOMo8BRK7PfcJv9JCnx7s5hj0PX.jpg"
        ),
        MovieItem(
            title = "Spider-Man: Across the Spider-Verse",
            overview = "Miles Morales joins a team of Spider-People and challenges how they protect the multiverse.",
            releaseYear = "2023",
            rating = "8.3",
            posterPath = "/8Vt6mWEReuy4Of61Lnj5Xj704m8.jpg",
            backdropPath = "/4HodYYKEIsGOdinkGi2Ucz6X9i0.jpg"
        )
    )
}
