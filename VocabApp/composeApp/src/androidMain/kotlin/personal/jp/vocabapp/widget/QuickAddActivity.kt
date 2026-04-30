package personal.jp.vocabapp.widget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import co.touchlab.kermit.Logger
import db.Word
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import personal.jp.vocabapp.sql.WordService

class QuickAddActivity : ComponentActivity(), KoinComponent {
    private val wordService: WordService by inject()
    private val widgetSyncManager: WidgetSyncManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var text by remember { mutableStateOf("") }
            val focusRequester = remember { FocusRequester() }

            Box(
                modifier = Modifier.fillMaxSize().clickable { finish() },
            contentAlignment = Alignment.BottomCenter
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1B202D), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .imePadding()
            .padding(16.dp)
            ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f).focusRequester(focusRequester),
                    placeholder = { Text("Add Word...", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (text.isNotBlank()) {
                            saveWord(text)
                        }
                    }),
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 20.sp
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF2D65FF),
                        focusedIndicatorColor = Color(0xFF2D65FF),
                        unfocusedIndicatorColor = Color(0xFF2D65FF)
                    )
                )

                //submit button
                IconButton(
                    onClick = {
                        if (text.isNotBlank()) {
                            saveWord(text)
                        }
                    },
                    enabled = text.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Submit",
                        tint = if (text.isNotBlank()) Color(0xFF2D65FF) else Color.Gray
                    )
                }
            }
        }
        }

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        }
    }

    private fun saveWord(name: String) {
        lifecycleScope.launch {
            val trimmedName = name.trim()
            val newWord = Word(
                name = trimmedName,
                meaningKr = "AI is Filling...",
                example = null,
                antonymEn = "",
                createdTime = "",
                modifiedTime = "",
                isDeleted = false,
                syncedTime = "",
                note = "",
            )

            wordService.addWord(newWord, emptyList())
            widgetSyncManager.syncWord(newWord)

            Logger.d("New Word Added from Widget: $trimmedName")
            val constraints = androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .build()

            val aiWorkRequest = androidx.work.OneTimeWorkRequestBuilder<AiFillWorker>()
                .setConstraints(constraints)
                .setInputData(androidx.work.workDataOf("WORD_NAME" to trimmedName))
                .build()

            androidx.work.WorkManager.getInstance(this@QuickAddActivity).enqueue(aiWorkRequest)

            finish()
        }
    }
}