package personal.jp.vocabapp.sql

// Logic for Service
// It only cares what to do with the data
// ex) validate the format of the data

import db.Tag
import db.Word as Word

interface WordService {
    suspend fun getWordOrNull(name: String): Word?
    suspend fun getActiveWordOrNull(name: String): Word?
    suspend fun getAllWords(): List<Word>
    suspend fun getActiveAllWords(): List<Word>
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
    suspend fun updateTag(oldName: String, newName: String, color: String): Boolean
    suspend fun getLatestModifiedTime(): String?
    suspend fun getUnusedTags(): List<Tag>
    suspend fun deleteUnusedTags(): Boolean
}

class WordServiceImpl(
    private val wordRepo: WordRepo,
    private val tagColorManager: TagColorManager
) : WordService {
    override suspend fun getWordOrNull(name: String): Word? {
       return wordRepo.findWordOrNull(name)
    }

    override suspend fun getActiveWordOrNull(name: String): Word? {
        return wordRepo.findActiveWordOrNull(name)
    }

    override suspend fun getAllWords(): List<Word> {
        return wordRepo.findAllWords()
    }

    override suspend fun getActiveAllWords(): List<Word> {
        return wordRepo.findAllActiveWords()
    }

    override suspend fun addWord(word: Word, tags: List<Tag>): Boolean {
        wordRepo.findWordOrNull(word.name)?.let { return false }
        // assign Color to tags
        val optimizedTags = tagColorManager.assignColors(tags)
        val cleanTags = optimizedTags.distinctBy { it.tagName.trim().lowercase() }
        return wordRepo.addWord(word, cleanTags)
    }

    override suspend fun updateWord(word: Word, tags: List<Tag>): Boolean {
        // assign Color to tags
        val optimizedTags = tagColorManager.assignColors(tags)
        val cleanTags = optimizedTags.distinctBy { it.tagName.trim().lowercase() }
        return wordRepo.updateWord(word, cleanTags)
    }

    override suspend fun upsertWord(word: Word, tags: List<Tag>): Boolean {
        val optimizedTags = tagColorManager.assignColors(tags)
        val cleanTags = optimizedTags.distinctBy { it.tagName.trim().lowercase() }
        return wordRepo.upsertWord(word, cleanTags)
    }

    override suspend fun deleteWord(name: String): Boolean {
        return wordRepo.deleteWord(name)
    }

    override suspend fun countWords(): Int {
        return wordRepo.countWords()
    }

    override suspend fun deleteAllWords(): Boolean {
        return wordRepo.deleteAllWords()
    }

    override suspend fun deletePermanently(): Boolean {
        return wordRepo.deletePermanently()
    }

    override suspend fun setSync(name: String): Boolean {
        return wordRepo.setSync(name)
    }

    override suspend fun getUnsyncedWords(lastSyncedTime: String): List<Word> {
        return wordRepo.getUnsyncedWords(lastSyncedTime)
    }

    override suspend fun getTagsForWord(wordName: String): List<Tag> {
        return wordRepo.getTagsForWord(wordName)
    }

    override suspend fun searchTags(query: String): List<Tag> {
        return wordRepo.searchTags(query)
    }

    override suspend fun updateTag(oldName: String, newName: String, color: String): Boolean {
        return wordRepo.updateTagInfo(oldName, newName, color)
    }

    override suspend fun getLatestModifiedTime(): String? {
        return wordRepo.getLatestModifiedTime()
    }

    override suspend fun getUnusedTags(): List<Tag> {
        return wordRepo.getUnusedTags()
    }

    override suspend fun deleteUnusedTags(): Boolean {
        return wordRepo.deleteUnusedTags()
    }
}