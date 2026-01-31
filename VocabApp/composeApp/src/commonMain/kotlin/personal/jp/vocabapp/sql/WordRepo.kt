package personal.jp.vocabapp.sql

// Data Repository
// CRUD concepts

import db.WordDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import co.touchlab.kermit.Logger
import db.Word as Word

interface WordRepo {
    suspend fun findWordOrNull(name: String): Word?
    suspend fun findAllWords(): List<Word>
    suspend fun addWord(word: Word): Boolean
    suspend fun updateWord(word: Word): Boolean
    suspend fun upsertWord(word: Word): Boolean
    suspend fun deleteWord(name: String): Boolean
    suspend fun countWords(): Int
    suspend fun deleteAllWords(): Boolean
    suspend fun setSync(name: String): Boolean
    suspend fun getUnsyncedWords(lastSyncedTime: String): List<Word>
}

// TODO Exception handling
class WordRepoImpl(db: WordDatabase): WordRepo {
    private val _queries = db.wordDatabaseQueries

    override suspend fun findWordOrNull(name: String): Word? = withContext(Dispatchers.IO) {
        try{
            _queries.selectWord(name).executeAsOneOrNull()
        } catch (e: Exception){
            null
        }
    }

    override suspend fun findAllWords(): List<Word> = withContext(Dispatchers.IO) {
        try{
            _queries.selectAllWordsInfo().executeAsList()
        } catch (e: Exception){
            emptyList()
        }
    }

    override suspend fun addWord(word: Word): Boolean = withContext(Dispatchers.IO) {
        try {
            _queries.insertWord(
                name = word.name,
                meaningKr = word.meaningKr,
                example = word.example,
                antonymEn = word.antonymEn,
                tags = word.tags,
                note = word.note
            )
            true
        } catch (e: Exception) {
            Logger.e { "Error: ${e.message}" }
            false
        }
    }

    override suspend fun deleteWord(name: String): Boolean = withContext(Dispatchers.IO) {
        try{
            _queries.deletedWord(name)
            true
        } catch (e: Exception){
            false
        }
    }

    override suspend fun updateWord(word: Word): Boolean = withContext(Dispatchers.IO) {
        try{
            _queries.updateWord(word.meaningKr, word.example, word.antonymEn, word.tags, word.note,
                                word.name,)
            true
        } catch (e: Exception){
            false
        }
    }

    override suspend fun upsertWord(word: Word): Boolean = withContext(Dispatchers.IO) {
        try {
            _queries.upsertWord(
                word.name, word.meaningKr, word.example, word.antonymEn, word.tags, word.note)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun countWords(): Int = withContext(Dispatchers.IO) {
        try{
            _queries.countWords().executeAsOne().toInt()
        } catch (e: Exception){
            0
        }
    }

    override suspend fun deleteAllWords(): Boolean = withContext(Dispatchers.IO) {
        try {
            _queries.deleteAllWords()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun setSync(name: String): Boolean = withContext(Dispatchers.IO) {
        try{
            _queries.setSync(name)
            true
        } catch (e: Exception){
            false
        }
    }

    override suspend fun getUnsyncedWords(lastSyncedTime: String): List<Word> = withContext(Dispatchers.IO) {
        try {
            _queries.selectUnsyncedWord(lastSyncedTime).executeAsList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
