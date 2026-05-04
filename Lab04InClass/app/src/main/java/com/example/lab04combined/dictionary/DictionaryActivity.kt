package com.example.lab04combined.dictionary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lab04combined.databinding.ActivityDictionaryBinding
import kotlinx.coroutines.launch

class DictionaryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDictionaryBinding
    private lateinit var repository: DictionaryRepository
    private val adapter = WordAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDictionaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = DictionaryDatabase.getInstance(this)
        repository = DictionaryRepository(db.wordDao())

        binding.recyclerWords.layoutManager = LinearLayoutManager(this)
        binding.recyclerWords.adapter = adapter

        binding.buttonLookup.setOnClickListener {
            val input = binding.inputWord.text.toString().trim()
            if (input.isEmpty()) {
                binding.textDefinition.text = ""
                adapter.submitList(emptyList())
                return@setOnClickListener
            }
            lookupWord(input)
        }
    }

    private fun lookupWord(input: String) {
        lifecycleScope.launch {
            val exact = repository.getExactWord(input)
            if (exact != null) {
                binding.textDefinition.text = "${exact.word}: ${exact.definition}"
                adapter.submitList(emptyList())
            } else {
                binding.textDefinition.text = getString(com.example.lab04combined.R.string.label_no_matches)
                val matches = repository.searchWords(input)
                adapter.submitList(matches)
            }
        }
    }
}

