package personal.jp.vocabapp.viewmodels

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import db.Tag
import personal.jp.vocabapp.sql.TagColorManager
import personal.jp.vocabapp.sql.getContrastColor
import personal.jp.vocabapp.theme.VocabTheme

data class WordWithTags(
    val word: Word,
    val tags: List<Tag>
)
sealed class WordUiState {
    object Idle : WordUiState()
    object Loading : WordUiState()
    data class Success(val data: WordWithTags) : WordUiState()
    data class Error(val message: String) : WordUiState()
}

class WordViewModel(private val wordService: WordService) : ViewModel() {
    private val _uiState = MutableStateFlow<WordUiState>(WordUiState.Idle)
    val uiState: StateFlow<WordUiState> = _uiState

    fun fetchWord(wordName: String) {
        viewModelScope.launch {
            _uiState.value = WordUiState.Loading

            val word = wordService.getWordOrNull(wordName)
            val tags = wordService.getTagsForWord(wordName)

            if (word != null) {
                _uiState.value = WordUiState.Success(WordWithTags(word, tags))
            } else {
                _uiState.value = WordUiState.Error("Word not found")
            }
        }
    }
}

@Composable
fun WordCard(word: String, tagManager: TagColorManager, onClick: () -> Unit) {
    val viewModel: WordViewModel = koinViewModel(key = word)
    val state = viewModel.uiState.collectAsState().value
    LaunchedEffect(word) {
        viewModel.fetchWord(word)
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {

        Button(
            onClick = {
                onClick()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            when (state) {
                is WordUiState.Loading -> CircularProgressIndicator()
                is WordUiState.Success -> {
                    VocabularyCard(state.data, tagManager)
                }
                is WordUiState.Error -> Text("Error: ${state.message}", color = Color.Red)
                else -> Text("No Data")
            }
        }
    }
}

@Composable
fun VocabularyCard(data: WordWithTags,tagManager: TagColorManager, modifier: Modifier = Modifier) {
    val firstMeaning = remember(data.word.meaningKr) {
        data.word.meaningKr.split("\n").firstOrNull() ?: ""
    }
    Box(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Column {
            Text(
                text = data.word.name,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = firstMeaning,
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

            Spacer(modifier = Modifier.height(8.dp))

            if (data.tags.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    data.tags.forEach { tag ->
                        TagChip(tag = tag, tagManager)
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(26.dp))
            }
        }
    }
}

@Composable
fun TagChip(tag: Tag, tagManager: TagColorManager) {
    val bgColor = tagManager.getTagColor(tag.tagName)
    val bgColorHex = tagManager.colorToHexString(bgColor)
    val textColor = getContrastColor(bgColorHex)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color = bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = tag.tagName,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}