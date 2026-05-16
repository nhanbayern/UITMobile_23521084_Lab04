package com.example.lab04combined

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.lab04combined.databinding.ActivityMainBinding
import com.example.lab04combined.dictionary.DictionaryActivity
import com.example.lab04combined.health.HealthActivity
import com.example.lab04combined.tv.MovieTvActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonDictionary.setOnClickListener {
            startActivity(Intent(this, DictionaryActivity::class.java))
        }
        binding.buttonHealth.setOnClickListener {
            startActivity(Intent(this, HealthActivity::class.java))
        }
        binding.buttonHomework.setOnClickListener {
            startActivity(Intent(this, MovieTvActivity::class.java))
        }
    }
}

