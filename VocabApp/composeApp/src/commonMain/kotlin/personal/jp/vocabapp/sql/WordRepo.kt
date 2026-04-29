package personal.jp.vocabapp.sql

// Data Repository
// CRUD concepts

import db.WordDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import co.touchlab.kermit.Logger
import db.Tag
import db.Word as Word

interface WordRepo {
    suspend fun findWordOrNull(name: String): Word?
    suspend fun findActiveWordOrNull(name: String): Word?
    suspend fun findAllWords(): List<Word>
    suspend fun findAllActiveWords(): List<Word>
    suspend fun addWord(word: Word, tags: List<Tag>): Boolean
    suspend fun updateWord(word: Word, tags: List<Tag>): Boolean
    suspend fun upsertWord(word: Word, tags: List<Tag>): Boolean
    suspend fun deleteWord(name: String): Boolean
    suspend fun countWords(): Int
    suspend fun deleteAllWords(): Boolean
    suspend fun deletePermanently(): Boolean
    suspend fun setSync(name: String): Boolean
    suspend fun getUnsyncedWords(lastSyncedTime: String): List<Word>
    suspend fun getTagsForWord(wordName: String): List<Tag>
    suspend fun searchTags(query: String): List<Tag>
    suspend fun updateTagInfo(oldName: String, newName: String, color: String): Boolean
    suspend fun getLatestModifiedTime(): String?
    suspend fun getUnusedTags(): List<Tag>
    suspend fun deleteUnusedTags(): Boolean
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

    override suspend fun findActiveWordOrNull(name: String): Word? = withContext(Dispatchers.IO) {
        try{
            _queries.selectActiveWordByName(name).executeAsOneOrNull()
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

    override suspend fun findAllActiveWords(): List<Word> = withContext(Dispatchers.IO)  {
        try{
            _queries.selectAllActiveWords().executeAsList()
        } catch (e: Exception){
            emptyList()
        }
    }

    override suspend fun addWord(word: Word, tags: List<Tag>): Boolean = withContext(Dispatchers.IO) {
        try {
            _queries.transaction {
                _queries.insertWord(
                    name = word.name,
                    meaningKr = word.meaningKr,
                    example = word.example,
                    antonymEn = word.antonymEn,
                    note = word.note
                )

                tags.forEach { tag ->
                    _queries.insertTag(tag.tagName, tag.color)
                    _queries.insertWordTag(word.name, tag.tagName)
                }
            }
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

    override suspend fun updateWord(word: Word, tags: List<Tag>): Boolean = withContext(Dispatchers.IO) {
        try{
            _queries.transaction {
                _queries.updateWord(
                    meaningKr = word.meaningKr,
                    example = word.example,
                    antonymEn = word.antonymEn,
                    note = word.note,
                    name = word.name
                )
                // delete previous tags
                _queries.deleteWordTagsByWord(word.name)
                // add new tags
                tags.forEach { tag ->
                    _queries.insertTag(tag.tagName, tag.color)
                    _queries.insertWordTag(word.name, tag.tagName)
                }
            }
            true
        } catch (e: Exception){
            false
        }
    }

    override suspend fun upsertWord(word: Word, tags: List<Tag>): Boolean = withContext(Dispatchers.IO) {
        try {
            _queries.transaction {
                _queries.upsertWord(
                    name = word.name,
                    meaningKr = word.meaningKr,
                    example = word.example,
                    antonymEn = word.antonymEn,
                    isDeleted = word.isDeleted,
                    createdTime = word.createdTime,
                    modifiedTime = word.modifiedTime,
                    syncedTime = word.syncedTime,
                    note = word.note
                )

                _queries.deleteWordTagsByWord(word.name)
                tags.forEach { tag ->
                    _queries.insertTag(tag.tagName, tag.color)
                    _queries.insertWordTag(word.name, tag.tagName)
                }
            }
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

    override suspend fun deletePermanently(): Boolean = withContext(Dispatchers.IO) {
        try{
            _queries.deletePermanently()
            true
        }catch (e: Exception) {
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

    override suspend fun getTagsForWord(wordName: String): List<Tag> = withContext(Dispatchers.IO) {
        _queries.selectTagsForWord(wordName).executeAsList()
    }

    override suspend fun searchTags(query: String): List<Tag> = withContext(Dispatchers.IO) {
        _queries.searchTags(query).executeAsList()
    }

    override suspend fun updateTagInfo(oldName: String, newName: String, color: String): Boolean = withContext(Dispatchers.IO) {
        try {
            _queries.updateTag(newName, color, oldName)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getLatestModifiedTime(): String? = withContext(Dispatchers.IO) {
       try {
           _queries.getLastModifiedTime().executeAsOneOrNull()
       } catch (e: Exception) {
           null
       }
    }

    override suspend fun getUnusedTags(): List<Tag> = withContext(Dispatchers.IO) {
        try {
            _queries.getUnusedTags().executeAsList()
        } catch (e: Exception) {
            Logger.e { "Error fetching unused tags: ${e.message}" }
            emptyList()
        }
    }

    override suspend fun deleteUnusedTags(): Boolean = withContext(Dispatchers.IO) {
        try {
            _queries.deleteUnusedTags()
            true
        } catch (e: Exception) {
            Logger.e { "Error deleting unused tags: ${e.message}" }
            false
        }
    }
}
