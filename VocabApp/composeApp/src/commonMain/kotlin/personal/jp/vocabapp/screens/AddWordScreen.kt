package personal.jp.vocabapp.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import personal.jp.vocabapp.sql.TagColorManager
import personal.jp.vocabapp.sql.WordServiceImpl
import personal.jp.vocabapp.sql.getContrastColor
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown

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
    var tagToDelete by remember { mutableStateOf<String?>(null) }
    var tagInputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var tagSuggestions by remember { mutableStateOf(emptyList<Tag>()) }
    var showSuggestions by remember { mutableStateOf(false) }

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val tagManager = TagColorManager()

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

    if (showErrorDialog) {
        GeminiErrorDialog(
            message = errorMessage,
            onDismiss = { showErrorDialog = false }
        )
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

    val moveDefinition = { index: Int, up: Boolean ->
        val newIndex = if (up) index - 1 else index + 1
        if (newIndex in definitions.indices) {
            val mutableList = definitions.toMutableList()
            val temp = mutableList[index]
            mutableList[index] = mutableList[newIndex]
            mutableList[newIndex] = temp
            definitions = mutableList
        }
    }

    // Tag delete dialog
    if (tagToDelete != null) {
        AlertDialog(
            onDismissRequest = { tagToDelete = null },
            containerColor = Color(0xFF1B202D),
            title = { Text("Remove Tag", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Remove '${tagToDelete}' from this word?", color = Color(0xFF9BA1B0)) },
            confirmButton = {
                TextButton(onClick = {
                    tagsList = tagsList.filter { it != tagToDelete }
                    tagToDelete = null
                }) {
                    Text("REMOVE", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { tagToDelete = null }) {
                    Text("CANCEL", color = Color.White)
                }
            }
        )
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
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    )
                    // AI Fill Button
                    AIFillButton(
                        targetWord = targetWord,
                        httpClient = httpClient,
                        isLoading = isLoading,
                        onLoadingChange = { loading ->
                            isLoading = loading
                            // clear all the data when AI fill starts
                            if (loading) {
                                definitions = listOf(Definition())
                                tagsList = emptyList()
                            }
                        },
                        onResult = { correctedName, newDefinitions, newTags ->
                            targetWord = correctedName
                            definitions = newDefinitions
                            tagsList = newTags
                        },
                        onError = { message ->
                            errorMessage = message
                            showErrorDialog = true
                        }
                    )
                }
            }

            // Definitions Section
            definitions.forEachIndexed { index, definition ->
                DefinitionCard(
                    definition = definition,
                    index = index,
                    enabled = !isLoading,
                    isFirst = index == 0,
                    isLast = index == definitions.size - 1,
                    onDefinitionChange = { updatedDef ->
                        definitions = definitions.mapIndexed { i, d -> if (i == index) updatedDef else d }
                    },
                    onDeleteClick = {
                        if (definitions.size > 1) {
                            definitions = definitions.filterIndexed { i, _ -> i != index }
                        }
                    },
                    onMoveUp = { moveDefinition(index, true) },
                    onMoveDown = { moveDefinition(index, false) }
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
                        DeletableTagChip(tag = tag, tagManager, onDeleteClick = { tagToDelete = tag })
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
    enabled: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onDefinitionChange: (Definition) -> Unit,
    onDeleteClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isFirst) {
                        IconButton(onClick = onMoveUp, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Move Up",
                                tint = Color(0xFF9BA1B0)
                            )
                        }
                    }
                    if (!isLast) {
                        IconButton(onClick = onMoveDown, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Move Down",
                                tint = Color(0xFF9BA1B0)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFF626978))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("MEANING (KR)", color = Color(0xFFA1A9BD), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            VocabTextField(
                value = definition.meaningKr,
                onValueChange = { onDefinitionChange(definition.copy(meaningKr = it)) },
                placeholder = "Meaning",
                enabled = enabled
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("EXAMPLE SENTENCE", color = Color(0xFFA1A9BD), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            VocabTextField(
                value = definition.exampleSentence,
                onValueChange = { onDefinitionChange(definition.copy(exampleSentence = it)) },
                placeholder = "Write an example sentence...",
                singleLine = false,
                modifier = Modifier.height(100.dp),
                enabled = enabled
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("OPPOSITE WORD", color = Color(0xFFA1A9BD), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            VocabTextField(
                value = definition.antonym,
                onValueChange = { onDefinitionChange(definition.copy(antonym = it)) },
                placeholder = "Antonym",
                enabled = enabled
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
    enabled: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
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
    isLoading: Boolean,
    onLoadingChange: (Boolean) -> Unit,
    onResult: (String, List<Definition>, List<String>) -> Unit,
    onError: (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    Button(
        onClick = {
            if (targetWord.isNotBlank() && !isLoading) {
                scope.launch {
                    onLoadingChange(true)
                    val result = enrichWordByGemini(httpClient, targetWord)
                    onLoadingChange(false)
                    if (result != null) {
                        val count = maxOf(
                            result.meaningKr.size,
                            result.example.size,
                            result.antonymEn.size
                        )

                        val newDefinitions = List(count) { index ->
                            Definition(
                                meaningKr = result.meaningKr.getOrNull(index) ?: "",
                                exampleSentence = result.example.getOrNull(index) ?: "",
                                antonym = result.antonymEn.getOrNull(index) ?: ""
                            )
                        }

                        onResult(result.name, newDefinitions, result.tags)
                    } else {
                        onError("Failed to fetch data from Gemini. Please check your internet connection or try again later.")
                    }
                }
            }
        },
        enabled = !isLoading && targetWord.isNotBlank(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2D65FF),
            disabledContainerColor = Color(0xFF2D65FF).copy(alpha = 0.5f)
        ),
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
fun DeletableTagChip(tag: String, tagManager: TagColorManager, onDeleteClick: () -> Unit) {
    val bgColor = tagManager.getTagColor(tag)
    val bgColorHex = tagManager.colorToHexString(bgColor)
    val textColor = getContrastColor(bgColorHex)
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, bgColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tag,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                tint = textColor,
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
            Text(
                "SUGGESTIONS",
                color = Color(0xFF626978),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

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
                }
            }
        }
    }
}


@Composable
fun GeminiErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1B202D),
        shape = RoundedCornerShape(28.dp),
        icon = {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = Color(0xFFFF5252),
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFFF5252).copy(alpha = 0.1f), CircleShape)
                    .padding(8.dp)
            )
        },
        title = {
            Text(
                text = "AI Fill Failed",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = message,
                color = Color(0xFF9BA1B0),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("OK", color = Color(0xFF2D65FF), fontWeight = FontWeight.Bold)
            }
        }
    )
}