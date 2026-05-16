package com.example.lab04combined.tv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lab04combined.databinding.ItemMovieCardBinding

class MovieAdapter(
    private val onMovieFocused: (MovieItem) -> Unit,
    private val imageLoader: TvImageLoader
) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {
    private val movies = mutableListOf<MovieItem>()

    fun submitList(items: List<MovieItem>) {
        movies.clear()
        movies.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(movies[position])
    }

    override fun getItemCount(): Int = movies.size

    inner class MovieViewHolder(
        private val binding: ItemMovieCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: MovieItem) {
            binding.movieTitle.text = movie.title
            binding.posterImage.setImageDrawable(null)
            imageLoader.load(movie.posterUrl, binding.posterImage)
            binding.root.setOnFocusChangeListener { view, hasFocus ->
                view.animate()
                    .scaleX(if (hasFocus) 1.08f else 1f)
                    .scaleY(if (hasFocus) 1.08f else 1f)
                    .setDuration(120L)
                    .start()
                if (hasFocus) {
                    onMovieFocused(movie)
                }
            }
            binding.root.setOnClickListener {
                onMovieFocused(movie)
            }
        }
    }
}
