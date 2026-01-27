package personal.jp.vocabapp.sql

// Data Repository
// CRUD concepts

import db.WordDatabase
import db.Word as Word

interface WordRepo {
    fun findWordOrNull(name: String): Word?
    fun findAllWords(): List<Word>
    fun addWord(word: Word): Boolean
    fun updateWord(word: Word): Boolean
    fun upsertWord(word: Word): Boolean
    fun deleteWord(name: String): Boolean
    fun countWords(): Int
    fun deleteAllWords(): Boolean
    fun setSync(name: String): Boolean
    fun getUnsyncedWords(lastSyncedTime: String): List<Word>
}

// TODO Exception handling
class WordRepoImpl(db: WordDatabase): WordRepo {
    private val _queries = db.wordDatabaseQueries

    override fun findWordOrNull(name: String): Word? {
        return try{
            _queries.selectWord(name).executeAsOneOrNull()
        } catch (e: Exception){
            null
        }
    }

    override fun findAllWords(): List<Word> {
        return try{
            _queries.selectAllWordsInfo().executeAsList()
        } catch (e: Exception){
            emptyList()
        }
    }

    override fun addWord(word: Word): Boolean {
        return try {
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
            println("Error: ${e.message}")
            false
        }
    }

    override fun deleteWord(name: String): Boolean {
        return try{
            _queries.deletedWord(name)
            true
        } catch (e: Exception){
            false
        }
    }

    override fun updateWord(word: Word): Boolean {
        return try{
            _queries.updateWord(word.meaningKr, word.example, word.antonymEn, word.tags, word.note,
                                word.name,)
            true
        } catch (e: Exception){
            false
        }
    }

    override fun upsertWord(word: Word): Boolean {
        return try {
            _queries.upsertWord(
                word.name, word.meaningKr, word.example, word.antonymEn, word.tags, word.note)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun countWords(): Int {
        return try{
            _queries.countWords().executeAsOne().toInt()
        } catch (e: Exception){
            0
        }
    }

    override fun deleteAllWords(): Boolean {
        return try {
            _queries.deleteAllWords()
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun setSync(name: String): Boolean {
        return try{
            _queries.setSync(name)
            true
        } catch (e: Exception){
            false
        }
    }

    override fun getUnsyncedWords(lastSyncedTime: String): List<Word> {
        return try {
            _queries.selectUnsyncedWord(lastSyncedTime).executeAsList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
