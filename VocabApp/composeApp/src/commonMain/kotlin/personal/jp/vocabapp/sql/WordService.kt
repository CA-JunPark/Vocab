package personal.jp.vocabapp.sql

// Logic for Service
// It only cares what to do with the data
// ex) validate the format of the data

import db.Word as Word

interface WordService {
    suspend fun getWordOrNull(name: String): Word?
    suspend fun getAllWords(): List<Word>
    suspend fun addWord(word: Word): Boolean
    suspend fun updateWord(word: Word): Boolean
    suspend fun upsertWord(word: Word): Boolean
    suspend fun deleteWord(name: String): Boolean
    suspend fun countWords(): Int
    suspend fun deleteAllWords(): Boolean
    suspend fun setSync(name: String): Boolean
    suspend fun getUnsyncedWords(lastSyncedTime: String): List<Word>
}

class WordServiceImpl(
    private val wordRepo: WordRepo
) : WordService {
    override suspend fun getWordOrNull(name: String): Word? {
       return wordRepo.findWordOrNull(name)
    }

    override suspend fun getAllWords(): List<Word> {
        return wordRepo.findAllWords()
    }

    override suspend fun addWord(word: Word): Boolean {
        // check duplicates
        wordRepo.findWordOrNull(word.name)?.let {
            return false
        }
        return wordRepo.addWord(word)
    }

    override suspend fun updateWord(word: Word): Boolean {
        return wordRepo.updateWord(word)
    }

    override suspend fun upsertWord(word: Word): Boolean {
        return wordRepo.upsertWord(word)
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

    override suspend fun setSync(name: String): Boolean {
        return wordRepo.setSync(name)

    }

    override suspend fun getUnsyncedWords(lastSyncedTime: String): List<Word> {
        return wordRepo.getUnsyncedWords(lastSyncedTime)
    }
}