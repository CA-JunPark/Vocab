package personal.jp.vocabapp.viewmodels

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import db.Word
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import personal.jp.vocabapp.sql.WordService

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

            if (result != null) {
                _uiState.value = WordUiState.Success(result)
                // Log here! It only runs once per fetch.
                println("Meaning fetched successfully: $result")
            } else {
                _uiState.value = WordUiState.Error("Failed to fetch word details.")
            }
        }
    }

}

@Composable
fun WordScreen(word: String) {
    val viewModel: WordViewModel = koinViewModel()
    val state = viewModel.uiState.collectAsState().value
    LaunchedEffect(word) {
        viewModel.fetchWord(word)
    }
    LaunchedEffect(state) {
        if (state is WordUiState.Success) {
            println("UI received success: ${state.data}")
        }
    }
    Column {

        Button(onClick = {
            println("Potato Click")
        }) {
            when (state) {
                is WordUiState.Loading -> CircularProgressIndicator()
                is WordUiState.Success -> {
                    Column {
                        Text(
                            text = state.data.name,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = "Meaning: ${state.data.meaningKr}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                is WordUiState.Error -> Text("Error: ${state.message}", color = Color.Red)
                else -> Text("No Data")
            }
        }
    }
}
