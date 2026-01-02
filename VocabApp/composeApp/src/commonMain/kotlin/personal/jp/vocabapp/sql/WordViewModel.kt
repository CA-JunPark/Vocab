package personal.jp.vocabapp.sql

import androidx.lifecycle.ViewModel

class WordViewModel(private val wordService: WordService) : ViewModel() {

    fun sayHello(name: String): String {
//        val user = wordService.getWordOrNull(name)
        val message = "Hi"
        return "[UserViewModel] $message"
    }
}