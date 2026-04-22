package personal.jp.vocabapp.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import db.Word
import personal.jp.vocabapp.viewmodels.WordWithTags

@Composable
fun AddWordScreen(
    onClose: () -> Unit,
    onSave: (word: Word, wordWithTags: WordWithTags) -> Unit
) {
    var word by remember { mutableStateOf("") }
    var meaning by remember { mutableStateOf("") }
    var example by remember { mutableStateOf("") }
    var antonym by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            AddWordTopBar(onClose = onClose)
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { /*TODO onSave*/ },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            VocabTextField(label = "Word", value = word, onValueChange = { word = it }, placeholder = "e.g. Ephemeral")

            GeminiAutoFillBanner()

            VocabTextField(label = "Meaning (KR)", value = meaning, onValueChange = { meaning = it }, placeholder = "Korean translation")

            VocabTextField(
                label = "Example Sentence",
                value = example,
                onValueChange = { example = it },
                placeholder = "Enter a sentence using the word...",
                singleLine = false,
                modifier = Modifier.height(100.dp) // 높이를 키움
            )

            VocabTextField(
                label = "Opposite Word",
                value = antonym,
                onValueChange = { antonym = it },
                placeholder = "Antonym",
                leadingIcon = { Icon(Icons.Rounded.SwapHoriz, contentDescription = null, tint = Color.Gray) }
            )

            VocabTextField(
                label = "Tags",
                value = tags,
                onValueChange = { tags = it },
                placeholder = "e.g. Verbs, N5",
                leadingIcon = { Icon(Icons.Rounded.Label, contentDescription = null, tint = Color.Gray) }
            )

            VocabTextField(
                label = "Notes",
                value = notes,
                onValueChange = { notes = it },
                placeholder = "Add any personal notes...",
                singleLine = false,
                modifier = Modifier.height(100.dp)
            )

            Spacer(modifier = Modifier.height(32.dp)) // 하단 버튼이 가리지 않도록 여백
        }
    }
}

@Composable
fun AddWordTopBar(onClose: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
            Text(
                text = "Add Word",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f).padding(end = 48.dp), // 중앙 정렬을 위해 우측 패딩 조정
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        HorizontalDivider(color = Color(0xFF2B3040), thickness = 1.dp)
    }
}

@Composable
fun VocabTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    Column {
        Text(
            text = label,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color.Gray) },
            leadingIcon = leadingIcon,
            singleLine = singleLine,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1B202D),
                unfocusedContainerColor = Color(0xFF1B202D),
                focusedBorderColor = Color(0xFF2D65FF),
                unfocusedBorderColor = Color(0xFF2B3040),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF2D65FF)
            )
        )
    }
}

@Composable
fun GeminiAutoFillBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1B202D))
            .border(1.dp, Color(0xFF2B3040), RoundedCornerShape(12.dp))
            .clickable { /* TODO: Gemini API 연결 */ }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text("✨ Gemini", color = Color(0xFFA1A9BD), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text("Auto-fill (Coming Soon)", color = Color(0xFFA1A9BD), fontSize = 14.sp)
    }
}