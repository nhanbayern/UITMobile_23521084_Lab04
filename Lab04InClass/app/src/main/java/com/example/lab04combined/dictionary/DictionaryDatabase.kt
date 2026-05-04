package com.example.lab04combined.dictionary

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(entities = [WordEntity::class], version = 1, exportSchema = false)
abstract class DictionaryDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao

    companion object {
        @Volatile
        private var INSTANCE: DictionaryDatabase? = null
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun getInstance(context: Context): DictionaryDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    DictionaryDatabase::class.java,
                    "dictionary.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            scope.launch {
                                getInstance(context).wordDao().insertAll(sampleWords())
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private fun sampleWords(): List<WordEntity> {
            return listOf(
                WordEntity(word = "apple", definition = "A round fruit with red, green, or yellow skin and a crisp, sweet flesh."),
                WordEntity(word = "banana", definition = "A long curved fruit with a yellow skin and soft sweet flesh inside."),
                WordEntity(word = "computer", definition = "An electronic device that processes data and performs tasks according to instructions."),
                WordEntity(word = "dictionary", definition = "A reference book or database that contains words and their meanings."),
                WordEntity(word = "database", definition = "An organized collection of structured information stored electronically."),
                WordEntity(word = "algorithm", definition = "A step-by-step procedure used to solve a problem or perform a computation."),
                WordEntity(word = "network", definition = "A system of interconnected computers or devices that share data."),
                WordEntity(word = "software", definition = "Programs and applications that run on a computer."),
                WordEntity(word = "hardware", definition = "The physical components of a computer system."),
                WordEntity(word = "application", definition = "A software program designed to perform specific tasks for users."),
                WordEntity(word = "artificial intelligence", definition = "The simulation of human intelligence in machines that can learn and make decisions."),
                WordEntity(word = "machine learning", definition = "A branch of AI that enables systems to learn from data and improve over time."),
                WordEntity(word = "blockchain", definition = "A decentralized digital ledger used to record transactions securely."),
                WordEntity(word = "cybersecurity", definition = "The practice of protecting systems and data from digital attacks."),
                WordEntity(word = "cloud computing", definition = "The delivery of computing services over the internet instead of local servers."),
                WordEntity(word = "version", definition = "A particular form or variant of something."),
                WordEntity(word = "vertical", definition = "Positioned up and down; perpendicular to the horizon."),
                WordEntity(word = "reverse", definition = "To move or turn in the opposite direction."),
                WordEntity(word = "verify", definition = "To check or confirm the accuracy of something."),
                WordEntity(word = "universal", definition = "Applicable to all cases or situations.")
            )
        }
    }
}

