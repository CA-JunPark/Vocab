package personal.jp.vocabapp.Screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.touchlab.kermit.Logger
import db.Tag
import db.Word
import io.ktor.client.HttpClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import personal.jp.vocabapp.google.enrichWordByGemini
import personal.jp.vocabapp.sql.WordServiceImpl
import personal.jp.vocabapp.viewmodels.WordWithTags

// Data class for managing multiple definitions
data class Definition(
    val meaningKr: String = "",
    val exampleSentence: String = "",
    val antonym: String = ""
)
@Composable
fun AddWordScreen(
    wordService: WordServiceImpl,
    httpClient: HttpClient,
    onClose: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var targetWord by remember { mutableStateOf("") }
    var definitions by remember { mutableStateOf(listOf(Definition())) }
    var tagsList by remember { mutableStateOf(emptyList<String>()) }
    var tagInputText by remember { mutableStateOf("") }

    var tagSuggestions by remember { mutableStateOf(emptyList<Tag>()) }
    var showSuggestions by remember { mutableStateOf(false) }

    LaunchedEffect(tagInputText) {
        if (tagInputText.isNotBlank()) {
            delay(300)
            tagSuggestions = wordService.searchTags(tagInputText)
            showSuggestions = tagSuggestions.isNotEmpty()
        } else {
            tagSuggestions = emptyList()
            showSuggestions = false
        }
    }


    val handleSave = {
        scope.launch {
            if (targetWord.isBlank()) return@launch

            val meaningText = definitions.joinToString("\n") { it.meaningKr }
            val exampleText = definitions.joinToString("\n") { it.exampleSentence }
            val antonymText = definitions.joinToString("\n") { it.antonym }

            val newWord = Word(
                name = targetWord.trim(),
                meaningKr = meaningText,
                example = exampleText,
                antonymEn = antonymText,
                note = "",
                createdTime = "",
                modifiedTime = "",
                isDeleted = false,
                syncedTime = null
            )

            // Tags Colors are assigned automatically in WordService
            val tags = tagsList.map { tagName ->
                Tag(tagName = tagName, color = "")
            }

            val success = wordService.addWord(newWord, tags)

            if (success) {
                onSaveSuccess()
            } else {
                Logger.e("Failed to save word: $targetWord")
            }
        }
    }


    Scaffold(
        topBar = {
            AddWordTopBar(onClose = onClose, onSave = { handleSave() })
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { handleSave() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D65FF)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Word", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Target Word Section
            Column {
                Text("TARGET WORD", color = Color(0xFFA1A9BD), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VocabTextField(
                        value = targetWord,
                        onValueChange = { targetWord = it },
                        placeholder = "Enter word...",
                        modifier = Modifier.weight(1f)
                    )
                    // AI Fill Button
                    AIFillButton(
                        targetWord = targetWord,
                        httpClient = httpClient,
                        onResult = { newDefinitions, newTags ->
                            definitions = newDefinitions
                            tagsList = newTags
                        }
                    )
                }
            }

            // Definitions Section
            definitions.forEachIndexed { index, definition ->
                DefinitionCard(
                    definition = definition,
                    index = index,
                    onDefinitionChange = { updatedDef ->
                        definitions = definitions.mapIndexed { i, d -> if (i == index) updatedDef else d }
                    },
                    onDeleteClick = {
                        if (definitions.size > 1) {
                            definitions = definitions.filterIndexed { i, _ -> i != index }
                        }
                    }
                )
            }

            // Add Another Definition Button
            OutlinedButton(
                onClick = { definitions = definitions + Definition() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF2B3040)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Outlined.AddCircleOutline, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ADD ANOTHER DEFINITION", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            // Tags Section
            Column {
                Text("TAGS", color = Color(0xFFA1A9BD), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tagsList.forEach { tag ->
                        TagChip(tag = tag, onDeleteClick = { tagsList = tagsList.filter { it != tag } })
                    }
                }

                if (tagsList.isNotEmpty()) Spacer(modifier = Modifier.height(12.dp))

                Box {
                    VocabTextField(
                        value = tagInputText,
                        onValueChange = { tagInputText = it },
                        placeholder = "ADD TAG...",
                        trailingIcon = {
                            if (tagInputText.isNotBlank()) {
                                IconButton(onClick = {
                                    if (!tagsList.contains(tagInputText.trim())) {
                                        tagsList = tagsList + tagInputText.trim()
                                    }
                                    tagInputText = ""
                                    showSuggestions = false
                                }) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray)
                                }
                            }
                        }
                    )

                    // Tag Suggestions
                    if (showSuggestions) {
                        TagSuggestionsMenu(
                            suggestions = tagSuggestions,
                            onSuggestionClick = { selectedTag ->
                                if (!tagsList.contains(selectedTag.tagName)) {
                                    tagsList = tagsList + selectedTag.tagName
                                }
                                tagInputText = ""
                                showSuggestions = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun AddWordTopBar(onClose: () -> Unit, onSave: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }
        Text(
            text = "Add Word",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onSave) {
            Icon(Icons.Default.Check, contentDescription = "Save", tint = Color(0xFF2D65FF))
        }
    }
}

@Composable
fun DefinitionCard(
    definition: Definition,
    index: Int,
    onDefinitionChange: (Definition) -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B202D).copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, Color(0xFF2B3040))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("DEFINITION ${index + 1}", color = Color(0xFF2D65FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFF626978))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("MEANING (KR)", color = Color(0xFFA1A9BD), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            VocabTextField(
                value = definition.meaningKr,
                onValueChange = { onDefinitionChange(definition.copy(meaningKr = it)) },
                placeholder = "Meaning"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("EXAMPLE SENTENCE", color = Color(0xFFA1A9BD), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            VocabTextField(
                value = definition.exampleSentence,
                onValueChange = { onDefinitionChange(definition.copy(exampleSentence = it)) },
                placeholder = "Write an example sentence...",
                singleLine = false,
                modifier = Modifier.height(100.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("OPPOSITE WORD", color = Color(0xFFA1A9BD), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            VocabTextField(
                value = definition.antonym,
                onValueChange = { onDefinitionChange(definition.copy(antonym = it)) },
                placeholder = "Antonym"
            )
        }
    }
}

@Composable
fun VocabTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = Color(0xFF626978), fontSize = 14.sp) },
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1B202D),
            unfocusedContainerColor = Color(0xFF1B202D),
            focusedBorderColor = Color(0xFF2D65FF),
            unfocusedBorderColor = Color(0xFF2B3040),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )
}

@Composable
fun AIFillButton(
    targetWord: String,
    httpClient: HttpClient,
    onResult: (List<Definition>, List<String>) -> Unit
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    Button(
        onClick = {
            if (targetWord.isNotBlank() && !isLoading) {
                scope.launch {
                    isLoading = true
                    val result = enrichWordByGemini(httpClient, targetWord)

                    result?.let { gemini ->
                        val count = maxOf(
                            gemini.meaningKr.size,
                            gemini.example.size,
                            gemini.antonymEn.size
                        )

                        val newDefinitions = List(count) { index ->
                            Definition(
                                meaningKr = gemini.meaningKr.getOrNull(index) ?: "",
                                exampleSentence = gemini.example.getOrNull(index) ?: "",
                                antonym = gemini.antonymEn.getOrNull(index) ?: ""
                            )
                        }

                        onResult(newDefinitions, gemini.tags)
                    }
                    isLoading = false
                }
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2D65FF),
            disabledContainerColor = Color(0xFF2D65FF).copy(alpha = 0.5f)
        ),
        enabled = !isLoading,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        modifier = Modifier.height(56.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (isLoading) "Filling..." else "AI Fill",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TagChip(tag: String, onDeleteClick: () -> Unit) {
    Surface(
        color = Color(0xFF2D65FF).copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFF2D65FF).copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tag.uppercase(),
                color = Color(0xFF2D65FF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                tint = Color(0xFF2D65FF),
                modifier = Modifier.size(14.dp).clickable { onDeleteClick() }
            )
        }
    }
}

@Composable
fun TagSuggestionsMenu(
    suggestions: List<Tag>,
    onSuggestionClick: (Tag) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(top = 60.dp)
            .fillMaxWidth(0.6f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF232938)),
        border = BorderStroke(1.dp, Color(0xFF2B3040))
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            // 헤더 섹션
            Text(
                "SUGGESTIONS",
                color = Color(0xFF626978),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // 추천 리스트
            suggestions.take(5).forEach { tag ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSuggestionClick(tag) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        tag.tagName,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    // 현재 입력 중인 태그와 정확히 일치하면 체크 표시 (옵션)
                    // if (isExactMatch) Icon(...)
                }
            }
        }
    }
}