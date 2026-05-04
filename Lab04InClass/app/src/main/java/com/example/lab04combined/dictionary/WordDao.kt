package com.example.lab04combined.dictionary

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WordDao {
    @Query("SELECT * FROM words WHERE word = :input LIMIT 1")
    suspend fun getExactWord(input: String): WordEntity?

    @Query("SELECT * FROM words WHERE word LIKE '%' || :query || '%' ORDER BY word ASC")
    suspend fun searchWords(query: String): List<WordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<WordEntity>)
}

