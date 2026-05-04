package com.example.lab04combined.dictionary

class DictionaryRepository(private val dao: WordDao) {
    suspend fun getExactWord(input: String): WordEntity? {
        return dao.getExactWord(input.lowercase())
    }

    suspend fun searchWords(query: String): List<WordEntity> {
        return dao.searchWords(query.lowercase())
    }
}

