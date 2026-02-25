package personal.jp.vocabapp.di

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import personal.jp.vocabapp.google.GeminiResponse
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import db.Word
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import personal.jp.vocabapp.google.enrichWordByGemini
import kotlinx.coroutines.delay
import personal.jp.vocabapp.sql.WordService
import personal.jp.vocabapp.sql.WordServiceImpl

sealed class WordUiState {
    object Idle : WordUiState()
    object Loading : WordUiState()
    data class Success(val data: Word) : WordUiState()
    data class Error(val message: String) : WordUiState()
}

class WordViewModel(private val wordService: WordService) : ViewModel() {
    private val _uiState = MutableStateFlow<WordUiState>(WordUiState.Idle)
    val uiState: StateFlow<WordUiState> = _uiState

    fun fetchWord(word: String){
        viewModelScope.launch {
            _uiState.value = WordUiState.Loading

            val result = wordService.getWordOrNull(word)

            if (result != null){
                _uiState.value = WordUiState.Success(result)
            }
            else {
                _uiState.value = WordUiState.Error("Failed to fetch word details.")
            }
        }
    }
}

@Composable
fun WordScreen(word: String) {
    val viewModel: WordViewModel = koinViewModel()
    val state = viewModel.uiState.collectAsState().value

    Column {

        Button(onClick = {
            viewModel.fetchWord(word)
        }) {
            Text("Get Potato")
        }

        when (state) {
            is WordUiState.Loading -> CircularProgressIndicator()
            is WordUiState.Success -> {
                Text("Meaning: ${state.data}")
            }

            is WordUiState.Error -> Text("Error: ${state.message}", color = Color.Red)
            else -> Text("Enter a word to start")
        }
    }
}
