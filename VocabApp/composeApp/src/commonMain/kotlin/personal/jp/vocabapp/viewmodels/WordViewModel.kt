package personal.jp.vocabapp.viewmodels

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import db.Word
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import personal.jp.vocabapp.sql.WordService
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.ui.draw.clip

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
    val viewModel: WordViewModel = koinViewModel(key = word)
    val state = viewModel.uiState.collectAsState().value
    LaunchedEffect(word) {
        viewModel.fetchWord(word)
    }
    LaunchedEffect(state) {
        if (state is WordUiState.Success) {
            println("UI received success: ${state.data}")
        }
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp)
    ) {

        Button(onClick = {
            println("Potato Click")
            // open detail
        }) {
            when (state) {
                is WordUiState.Loading -> CircularProgressIndicator()
                is WordUiState.Success -> {
                    VocabularyCard(state.data)
                }
                is WordUiState.Error -> Text("Error: ${state.message}", color = Color.Red)
                else -> Text("No Data")
            }
        }
    }
}

@Composable
fun VocabularyCard(data: Word, modifier: Modifier = Modifier) {
    val tags = data.tags?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotBlank() }
        ?: emptyList()
    // Main Card Container
    Box(
        modifier = modifier
            .fillMaxWidth()
//            .background(
//                color = Color(0xFF1B202D), // Dark blue/grey background
//                shape = RoundedCornerShape(16.dp)
//            )
            .padding(16.dp)
    ) {
        Column {
            // English Word
            Text(
                text = data.name,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Korean Translation and Speaker Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = data.meaningKr,
                    color = Color(0xFF9BA1B0),
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = {
                    println("Sound Click")
                    // TODO Add Sound
                }){
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = "Pronounce",
                        tint = Color(0xFF9BA1B0),
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            // Tags Row

            if (tags.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)

                ) {
                    for (tag in tags) {
                        TagChip(
                            text = tag,
                            backgroundColor = Color(0xFF222C47),
                            textColor = Color(0xFF8AA1E3)
                        )
                    }
                }
            } else {
                // Invisible placeholder to maintain the exact same card height
                TagChip(
                    text = " ", // A single space so the text engine calculates height
                    backgroundColor = Color.Transparent,
                    textColor = Color.Transparent
                )
            }
        }
    }
}

// Reusable component for the colored tags
@Composable
fun TagChip(text: String, backgroundColor: Color, textColor: Color) {Box(
    modifier = Modifier
        .padding(end = 8.dp)
        .clip(RoundedCornerShape(6.dp))
        // Make it clickable
        .clickable {
            println("Tag Click") // Fixed the comma typo here too!
            // TODO Open Search Tag
        }
        .background(color = backgroundColor)
        .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
}

}