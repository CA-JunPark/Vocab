package personal.jp.vocabapp.sql

// Logic for Service
// It only cares what to do with the data
// ex) validate the format of the data

import db.Word as Word

interface WordService {
    fun getWordOrNull(name: String): Word?
    fun getAllWords(): List<Word>
    fun addWord(word: Word): Boolean
    fun updateWord(word: Word): Boolean
    fun upsertWord(word: Word): Boolean
    fun deleteWord(name: String): Boolean
    fun countWords(): Int
    fun deleteAllWords(): Boolean
    fun setSync(name: String): Boolean
    fun getUnsyncedWords(lastSyncedTime: String): List<Word>
}

class WordServiceImpl(
    private val wordRepo: WordRepo
) : WordService {
    override fun getWordOrNull(name: String): Word? {
       return wordRepo.findWordOrNull(name)
    }

    override fun getAllWords(): List<Word> {
        return wordRepo.findAllWords()
    }

    override fun addWord(word: Word): Boolean {
        // check duplicates
        wordRepo.findWordOrNull(word.name)?.let {
            return false
        }
        return wordRepo.addWord(word)
    }

    override fun updateWord(word: Word): Boolean {
        return wordRepo.updateWord(word)
    }

    override fun upsertWord(word: Word): Boolean {
        return wordRepo.upsertWord(word)
    }

    override fun deleteWord(name: String): Boolean {
        return wordRepo.deleteWord(name)
    }

    override fun countWords(): Int {
        return wordRepo.countWords()
    }

    override fun deleteAllWords(): Boolean {
        return wordRepo.deleteAllWords()
    }

    override fun setSync(name: String): Boolean {
        return wordRepo.setSync(name)

    }

    override fun getUnsyncedWords(lastSyncedTime: String): List<Word> {
        return wordRepo.getUnsyncedWords(lastSyncedTime)
    }
}