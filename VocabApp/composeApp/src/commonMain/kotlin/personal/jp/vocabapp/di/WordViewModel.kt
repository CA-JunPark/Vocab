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
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import personal.jp.vocabapp.google.enrichWordByGemini

sealed class WordUiState {
    object Idle : WordUiState()
    object Loading : WordUiState()
    data class Success(val data: GeminiResponse) : WordUiState()
    data class Error(val message: String) : WordUiState()
}

class WordEnrichmentViewModel(private val client: HttpClient) : ViewModel() {

    private val _uiState = MutableStateFlow<WordUiState>(WordUiState.Idle)
    val uiState: StateFlow<WordUiState> = _uiState

    fun enrichWord(word: String) {
        viewModelScope.launch {
            _uiState.value = WordUiState.Loading

            val result = enrichWordByGemini(client, word)

            if (result != null) {
                _uiState.value = WordUiState.Success(result)
            } else {
                _uiState.value = WordUiState.Error("Failed to fetch word details.")
            }
        }
    }
}

@Composable
fun WordEnrichmentScreen(word: String) {
    val viewModel: WordEnrichmentViewModel = koinViewModel()
    val state = viewModel.uiState.collectAsState().value

    Column {
        Button(onClick = { viewModel.enrichWord(word) }) {
            Text("Enrich Word")
        }

        when (state) {
            is WordUiState.Loading -> CircularProgressIndicator()
            is WordUiState.Success -> {
                Text("Meaning: ${state.data.meaningKr.joinToString()}")
                Text("Example: ${state.data.example.firstOrNull() ?: ""}")
            }
            is WordUiState.Error -> Text("Error: ${state.message}", color = Color.Red)
            else -> Text("Enter a word to start")
        }
    }
}