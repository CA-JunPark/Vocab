package personal.jp.vocabapp.Screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.AddCircleOutline
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
import db.Tag
import db.Word
import io.ktor.client.HttpClient
import kotlinx.coroutines.launch
import personal.jp.vocabapp.sql.TagColorManager
import personal.jp.vocabapp.sql.WordServiceImpl
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import kotlinx.coroutines.delay

@Composable
fun EditWordScreen(
    wordName: String,
    wordService: WordServiceImpl,
    onClose: () -> Unit,
    onUpdateSuccess: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var targetWord by remember { mutableStateOf(wordName) }
    var definitions by remember { mutableStateOf(listOf(Definition())) }
    var note by remember { mutableStateOf("") }
    var tagsList by remember { mutableStateOf(emptyList<String>()) }
    var isLoading by remember { mutableStateOf(false) }

    var tagInputText by remember { mutableStateOf("") }
    var tagSuggestions by remember { mutableStateOf(emptyList<Tag>()) }
    var showSuggestions by remember { mutableStateOf(false) }
    val tagManager = TagColorManager()

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var showDeleteDialog by remember { mutableStateOf(false) }

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

    LaunchedEffect(wordName) {
        val existingWord = wordService.getActiveWordOrNull(wordName)
        val existingTags = wordService.getTagsForWord(wordName)

        existingWord?.let { word ->
            targetWord = word.name
            note = word.note ?: ""

            val meanings = word.meaningKr.split("\n")
            val examples = word.example?.split("\n") ?: emptyList()
            val antonyms = word.antonymEn?.split("\n") ?: emptyList()

            val count = maxOf(meanings.size, examples.size, antonyms.size)
            definitions = List(count) { i ->
                Definition(
                    meaningKr = meanings.getOrNull(i) ?: "",
                    exampleSentence = examples.getOrNull(i) ?: "",
                    antonym = antonyms.getOrNull(i) ?: ""
                )
            }
            tagsList = existingTags.map { it.tagName }
        }
    }

    if (showErrorDialog) {
        GeminiErrorDialog(
            message = errorMessage,
            onDismiss = { showErrorDialog = false }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color(0xFF1B202D),
            title = { Text("Delete Word", color = Color.White) },
            text = { Text("Are you sure you want to delete '$wordName'?", color = Color(0xFF9BA1B0)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteClick()
                }) {
                    Text("DELETE", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("CANCEL", color = Color.White)
                }
            }
        )
    }

    val handleUpdate = {
        scope.launch {
            if (targetWord.isBlank()) return@launch

            val meaningText = definitions.joinToString("\n") { it.meaningKr }
            val exampleText = definitions.joinToString("\n") { it.exampleSentence }
            val antonymText = definitions.joinToString("\n") { it.antonym }

            val updatedWord = Word(
                name = targetWord.trim(),
                meaningKr = meaningText,
                example = exampleText,
                antonymEn = antonymText,
                note = note,
                createdTime = "",
                modifiedTime = "",
                isDeleted = false,
                syncedTime = null
            )

            val tags = tagsList.map { Tag(tagName = it, color = "") }

            val success = wordService.upsertWord(updatedWord, tags)

            if (success) {
                onUpdateSuccess()
            }
        }
    }

    Scaffold(
        topBar = {
            EditWordTopBar(
                onClose = onClose,
                onUpdate = { handleUpdate() },
                onDeleteClick = { showDeleteDialog = true }
            )
        },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(16.dp)) {
                Button(
                    onClick = { handleUpdate() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D65FF)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Edit", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Column {
                Text("TARGET WORD", color = Color(0xFFA1A9BD), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VocabTextField(
                        value = targetWord,
                        onValueChange = { targetWord = it },
                        placeholder = "Word",
                        enabled = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            definitions.forEachIndexed { index, definition ->
                DefinitionCard(
                    definition = definition,
                    index = index,
                    enabled = !isLoading,
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

            Column {
                Text("TAGS", color = Color(0xFFA1A9BD), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
                
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tagsList.forEach { tag ->
                        DeletableTagChip(
                            tag = tag,
                            tagManager = tagManager,
                            onDeleteClick = { tagsList = tagsList.filter { it != tag } }
                        )
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

            Column {
                Text(
                    text = "NOTE",
                    color = Color(0xFFA1A9BD),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                VocabTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = "Write a personal note...",
                    singleLine = false,
                    modifier = Modifier.height(240.dp),
                    enabled = !isLoading
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun EditWordTopBar(
    onClose: () -> Unit,
    onUpdate: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(80.dp).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Edit Word",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF5252).copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = Color(0xFFFF5252)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1B202D))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
    }
}