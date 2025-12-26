package personal.jp.vocabapp.di

interface WordService {
    fun getWordOrNull(name: String): Word?
    fun getAllWords(): List<Word>
    fun addWord(word: Word): Boolean
    fun updateWord(word: Word): Boolean
    fun deleteWord(name: String): Boolean
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
        return wordRepo.addWord(word)

    }

    override fun updateWord(word: Word): Boolean {
        return wordRepo.updateWord(word)
    }

    override fun deleteWord(name: String): Boolean {
        return wordRepo.deleteWord(name)
    }
}