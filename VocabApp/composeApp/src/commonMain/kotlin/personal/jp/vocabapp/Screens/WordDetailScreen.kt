package personal.jp.vocabapp.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import db.Tag
import db.Word
import personal.jp.vocabapp.sql.WordServiceImpl

enum class DetailTab { DETAILS, NOTES }

@Composable
fun WordDetailScreen(
    wordName: String,
    wordService: WordServiceImpl,
    onEditClick: () -> Unit,
    onClose: () -> Unit
) {
    var word by remember { mutableStateOf<Word?>(null) }
    var tags by remember { mutableStateOf(emptyList<Tag>()) }
    var currentTab by remember { mutableStateOf(DetailTab.DETAILS) }

    // load data
    LaunchedEffect(wordName) {
        word = wordService.getWordOrNull(wordName)
        tags = wordService.getTagsForWord(wordName)
    }

    Scaffold(
        topBar = {
            WordDetailTopBar(onEditClick = onEditClick, onClose = onClose)
        },
        bottomBar = {
            WordDetailBottomNav(
                selectedTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        },
        containerColor = Color(0xFF0F1219)
    ) { padding ->
        word?.let { currentWord ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                WordDetailHeader(wordName = currentWord.name)

                Spacer(modifier = Modifier.height(32.dp))

                when (currentTab) {
                    DetailTab.DETAILS -> DetailsView(currentWord)
                    DetailTab.NOTES -> NotesView(currentWord, tags)
                }
            }
        }
    }
}

@Composable
fun DetailsView(word: Word) {
    val meanings = word.meaningKr.split("\n")
    val examples = word.example?.split("\n") ?: emptyList()
    val antonyms = word.antonymEn?.split("\n") ?: emptyList()

    val cardCount = maxOf(meanings.size, examples.size, antonyms.size)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        for (i in 0 until cardCount) {
            DefinitionDetailCard(
                index = i + 1,
                meaning = meanings.getOrNull(i) ?: "",
                example = examples.getOrNull(i) ?: "",
                antonym = antonyms.getOrNull(i) ?: "",
                targetWord = word.name
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun NotesView(word: Word, tags: List<Tag>) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Column {
            Text("PERSONAL NOTES", color = Color(0xFF626978), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B202D))
            ) {
                Text(
                    text = word.note ?: "",
                    color = Color.White,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(20.dp)
                )
            }
        }

        // TAGS 섹션
        Column {
            Text("TAGS", color = Color(0xFF626978), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tags.forEach { tag ->
                    DetailTagChip(tag)
                }
            }
        }
    }
}

@Composable
fun DefinitionDetailCard(index: Int, meaning: String, example: String, antonym: String, targetWord: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B202D))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("DEF $index", color = Color(0xFF626978), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(12.dp))

            Text(meaning, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)

            if (example.isNotBlank()) {
                Spacer(modifier = Modifier.height(20.dp))
                Row {
                    Icon(Icons.Outlined.Notes, contentDescription = null, tint = Color(0xFF626978), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EXAMPLE", color = Color(0xFF626978), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Emphasis the target word
                Text(
                    text = buildAnnotatedString {
                        val parts = example.split(targetWord, ignoreCase = true)
                        parts.forEachIndexed { idx, part ->
                            append(part)
                            if (idx < parts.size - 1) {
                                withStyle(SpanStyle(color = Color(0xFF2D65FF), fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold)) {
                                    append(targetWord)
                                }
                            }
                        }
                    },
                    color = Color.White,
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Italic
                )
            }

            if (antonym.isNotBlank()) {
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F1219).copy(alpha = 0.5f))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Block, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("OPPOSITE", color = Color(0xFF626978), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            Text(antonym, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WordDetailBottomNav(selectedTab: DetailTab, onTabSelected: (DetailTab) -> Unit) {
    NavigationBar(
        containerColor = Color(0xFF0F1219),
        tonalElevation = 0.dp,
        modifier = Modifier.height(80.dp)
    ) {
        NavigationBarItem(
            selected = selectedTab == DetailTab.DETAILS,
            onClick = { onTabSelected(DetailTab.DETAILS) },
            icon = { Icon(Icons.Outlined.Description, contentDescription = null) },
            label = { Text("DETAILS", fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF2D65FF),
                selectedTextColor = Color(0xFF2D65FF),
                unselectedIconColor = Color(0xFF626978),
                unselectedTextColor = Color(0xFF626978),
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selectedTab == DetailTab.NOTES,
            onClick = { onTabSelected(DetailTab.NOTES) },
            icon = { Icon(Icons.Outlined.Notes, contentDescription = null) },
            label = { Text("NOTES", fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF2D65FF),
                selectedTextColor = Color(0xFF2D65FF),
                unselectedIconColor = Color(0xFF626978),
                unselectedTextColor = Color(0xFF626978),
                indicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
fun WordDetailTopBar(onEditClick: () -> Unit, onClose: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(80.dp).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Word Detail", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = onEditClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D65FF)),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("EDIT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF1B202D))
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }
    }
}

@Composable
fun WordDetailHeader(wordName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = wordName,
            color = Color.White,
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.width(16.dp))
        IconButton(
            onClick = { /* TODO */ },
            modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF1B202D))
        ) {
            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Pronounce", tint = Color(0xFF2D65FF))
        }
    }
}

@Composable
fun DetailTagChip(tag: Tag) {
    val tagColor = remember(tag.color) {
        try {
            Color(tag.color.removePrefix("#").toLong(16) or 0xFF000000)
        } catch (e: Exception) {
            Color(0xFF2B313E)
        }
    }


    Surface(
        color = tagColor,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, tagColor)
    ) {
        Text(
            text = tag.tagName,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}