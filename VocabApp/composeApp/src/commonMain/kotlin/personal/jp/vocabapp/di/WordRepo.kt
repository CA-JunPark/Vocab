package personal.jp.vocabapp.di

// CRUD for Word

interface WordRepo {
    fun findWordOrNull(name: String): Word?
    fun findAllWords(): List<Word>
    fun addWord(word: Word): Boolean
    fun updateWord(word: Word): Boolean
    fun deleteWord(name: String): Boolean
}

class WordRepoImpl : WordRepo {
    private val _words = mutableListOf<Word>()
    override fun findWordOrNull(name: String): Word? {
        return _words.firstOrNull { it.name == name }
    }

    override fun findAllWords(): List<Word> {
        return _words
    }

    override fun addWord(word: Word): Boolean {
        _words.add(word)
        return true
    }

    override fun updateWord(word: Word): Boolean {
        var index = _words.indexOfFirst { it.name == word.name }
        if (index == -1) return false
        _words[index] = word
        return true
    }

    override fun deleteWord(name: String): Boolean {
        var index = _words.indexOfFirst { it.name == name }
        if (index != -1){
            _words[index] = _words[index].copy(isDeleted = true)
            return true
        }
        return false
    }

}