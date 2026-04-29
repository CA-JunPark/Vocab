package personal.jp.vocabapp

import androidx.compose.animation.AnimatedContent
import personal.jp.vocabapp.theme.VocabTheme
import androidx.compose.runtime.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.koin.compose.koinInject
import personal.jp.vocabapp.sql.WordServiceImpl
import kotlinx.coroutines.launch
import personal.jp.vocabapp.google.AuthRepository
import personal.jp.vocabapp.google.SecureStorage
import kotlinx.serialization.Serializable
import co.touchlab.kermit.Logger
import io.ktor.client.statement.bodyAsText
import personal.jp.vocabapp.screens.AddWordScreen
import personal.jp.vocabapp.screens.MainScreen
import personal.jp.vocabapp.screens.Screen
import personal.jp.vocabapp.sql.SerializableWord
import personal.jp.vocabapp.sql.KeyDataManager
import personal.jp.vocabapp.viewmodels.WordWithTags
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import personal.jp.vocabapp.screens.EditWordScreen
import personal.jp.vocabapp.screens.SettingsScreen
import personal.jp.vocabapp.screens.WordDetailScreen
import personal.jp.vocabapp.sql.requestCloudPurge
import personal.jp.vocabapp.sql.sync
import androidx.compose.ui.graphics.Color
import personal.jp.vocabapp.screens.PlatformBackHandler
import personal.jp.vocabapp.screens.desktopBackHandler

@Composable
fun App(onExit: () -> Unit) {
    MyScreen(onExit = onExit)
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MyScreen(onExit: () -> Unit) {
    val authRepository: AuthRepository = koinInject()
    val secureStorage: SecureStorage = koinInject()
    val scope = rememberCoroutineScope()
    val client: HttpClient = koinInject()
    val service: WordServiceImpl = koinInject()
    val isNetworkAvailable: Boolean = koinInject()
    val keyDataManager : KeyDataManager = koinInject()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    var allWordsWithTags by remember { mutableStateOf<List<WordWithTags>>(emptyList()) }

    var hasPendingChanges by remember { mutableStateOf(false) }

    val checkPendingChanges = {
        scope.launch {
            val lastSync = keyDataManager.getLastSync() ?: "1970-01-01 00:00:00"
            val latestLocal = service.getLatestModifiedTime()

            hasPendingChanges = if (latestLocal != null) {
                latestLocal > lastSync
            } else {
                false
            }
        }
    }

    val userProfile by authRepository.currentUser.collectAsState()
    val isLoginInProgress by authRepository.isLoginInProgress.collectAsState()
    var isSyncing by remember { mutableStateOf(false) }
    var syncErrorMessage by remember { mutableStateOf<String?>(null) }

    // refresh Words
    val refreshWords = {
        scope.launch {
            val rawWords = service.getActiveAllWords()
            allWordsWithTags = rawWords
                .filter { !it.isDeleted }
                .map { word ->
                    WordWithTags(word, service.getTagsForWord(word.name))
                }

            checkPendingChanges()
        }
    }

    var showExitDialog by remember { mutableStateOf(false) }

    val handleBack = {
        when (val screen = currentScreen) {
            is Screen.Home -> showExitDialog = true
            is Screen.AddWord -> currentScreen = Screen.Home
            is Screen.WordDetail -> currentScreen = Screen.Home
            is Screen.EditWord -> currentScreen = Screen.WordDetail(screen.wordName)
            is Screen.Settings -> currentScreen = Screen.Home
        }
    }

    PlatformBackHandler(onBack = handleBack)

    LaunchedEffect(Unit) {
        refreshWords()
    }

    VocabTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .desktopBackHandler(onBack = handleBack)
        ) {
            if (showExitDialog) {
                ExitConfirmDialog(
                    onDismiss = { showExitDialog = false },
                    onConfirm = {
                        showExitDialog = false
                        onExit()
                    }
                )
            }

            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    if (targetState is Screen.AddWord) {
                        (slideInVertically { it } + fadeIn()).togetherWith(fadeOut())
                    } else {
                        fadeIn().togetherWith(slideOutVertically { it } + fadeOut())
                    }
                }
            ) { screen ->
                when (screen) {
                    is Screen.Home -> {
                        MainScreen(
                            userProfile,
                            wordsList = allWordsWithTags,
                            onAddClick = { currentScreen = Screen.AddWord },
                            onSettingsClick = { currentScreen = Screen.Settings },
                            onWordClick = { name ->
                                currentScreen = Screen.WordDetail(name)
                            }
                        )
                    }

                    is Screen.AddWord -> {
                        AddWordScreen(
                            service,
                            client,
                            onClose = { currentScreen = Screen.Home },
                            onSaveSuccess = {
                                scope.launch {
                                    refreshWords()
                                    currentScreen = Screen.Home
                                }
                            }
                        )
                    }

                    is Screen.WordDetail -> {
                        WordDetailScreen(
                            wordName = screen.wordName,
                            wordService = service,
                            onEditClick = {
                                currentScreen = Screen.EditWord(screen.wordName)
                            },
                            onDeleteClick = {
                                scope.launch {
                                    service.deleteWord(screen.wordName)
                                    refreshWords()
                                    currentScreen = Screen.Home
                                }
                            },
                            onClose = {
                                currentScreen = Screen.Home
                            }
                        )
                    }

                    is Screen.EditWord -> {
                        EditWordScreen(
                            wordName = screen.wordName,
                            wordService = service,
                            onClose = {
                                currentScreen = Screen.WordDetail(screen.wordName)
                            },
                            onUpdateSuccess = {
                                scope.launch {
                                    refreshWords()
                                    currentScreen = Screen.WordDetail(screen.wordName)
                                }
                            },
                            onDeleteClick = {
                                scope.launch {
                                    service.deleteWord(screen.wordName)
                                    refreshWords()
                                    currentScreen = Screen.Home
                                }
                            },
                        )
                    }

                    is Screen.Settings -> {
                        SettingsScreen(
                            userProfile = userProfile,
                            isLoginInProgress = isLoginInProgress,
                            hasPendingChanges = hasPendingChanges,
                            isSyncing = isSyncing,
                            wordService = service,
                            onBackClick = { currentScreen = Screen.Home },
                            onLoginClick = {
                                scope.launch {
                                    authRepository.startLogin()
                                }
                            },
                            onLogoutClick = {
                                scope.launch {
                                    authRepository.logout()
                                }
                            },
                            onSyncClick = {
                                scope.launch {
                                    isSyncing = true
                                    syncErrorMessage = null

                                    try {
                                        sync(client, service, keyDataManager)
                                        refreshWords()
                                    } catch (e: Exception) {
                                        syncErrorMessage = e.message ?: "Sync failed"
                                    } finally {
                                        isSyncing = false
                                    }
                                }
                            },
                            onCancelLogin = {
                                scope.launch { authRepository.cancelLogin() }
                            },
                            syncErrorMessage = syncErrorMessage,
                            onDismissSyncError = {},
                            onResetSyncClick = {
                                scope.launch {
                                    keyDataManager.resetSyncTime()
                                }
                            },
                            onDeleteTagsComplete = {
                                refreshWords()
                            },
                            onPurgeDeletedClick = {
                                scope.launch {
                                    isSyncing = true
                                    syncErrorMessage = null

                                    try {
                                        requestCloudPurge(client)

                                        service.deletePermanently()
                                        refreshWords()
                                        Logger.i { "Data purge completed successfully." }
                                    } catch (e: Exception) {
                                        syncErrorMessage = "Purge failed: ${e.message}"
                                    } finally {
                                        isSyncing = false
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
//        var showContent by remember { mutableStateOf(false) }
//        Column(
//            modifier = Modifier
//                .background(MaterialTheme.colorScheme.background)
//                .safeContentPadding()
//                .fillMaxSize(),
//            horizontalAlignment = Alignment.CenterHorizontally,
//        ) {
//            Button(onClick = { scope.launch{
//                authRepository.startLogin()
//            } }) {
//                Text("Login with Google.")
//            }
//            Button(onClick = { showContent = !showContent }) {
//                Text("Click meee!")
//            }
//            AnimatedVisibility(showContent) {
//                val greeting = remember { Greeting().greet() }
//                Column(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                ) {
//                    Image(painterResource(Res.drawable.compose_multiplatform), null)
//                    Text("Compose: $greeting")
//                }
//            }
//            Button(onClick = {scope.launch {
//                val token = secureStorage.getToken(ID_TOKEN)
//                Logger.d { "ID Token is: $token" }
//            }}){
//                Text("Check Tokens")
//            }
//            Button(onClick = {scope.launch {
//                secureStorage.deleteToken(ID_TOKEN)
//                Logger.d { "ID Token deleted" }
//            }}){
//                Text("Clear ID Tokens")
//            }
//            Button(onClick = {scope.launch {
//                Logger.d { "DB Pull" }
//                Logger.d { "${backendPull(client)}" }
//            }}){
//                Text("DB pull")
//            }
//            Button(onClick = {scope.launch {
//                service.deleteAllWords()
//                Logger.d { "Add word" }
//                Logger.d { "Count: ${service.countWords()}" }
//                try{
//                    val (testWord, testTags) =
//                        prepareWordData(
//                            name = "computer",
//                            meaning = "컴퓨터",
//                            example = "I fixed computer",
//                            tagNames = listOf("IT", "electronics")
//                        )
//
//                    service.addWord(testWord, testTags)
//                } catch (e: Exception){
//                    Logger.e { "Error: ${e.message}" }
//                }
//                Logger.d { "Count: ${service.countWords()}" }
//            }}){
//                Text("Add Word")
//            }
//            Button(onClick = {scope.launch {
//                Logger.d { "Sync" }
//                sync(client, service, keyDataManager)
//                Logger.d { "Local Words Count: ${service.countWords()}" }
//            }}){
//                Text("Sync")
//            }
//            Button(onClick = {scope.launch {
//                keyDataManager.saveLastSync()
//                Logger.d { keyDataManager.getLastSync() }
//            }}){
//                Text("saveLastSync")
//            }
//            Button(onClick = {scope.launch {
//                keyDataManager.resetSyncTime()
//                Logger.d { keyDataManager.getLastSync() }
//            }}){
//                Text("resetSyncTime")
//            }
//            Button(onClick = {scope.launch {
//                val gemini : GeminiResponse? = enrichWordByGemini(client, "paper")
//                println(gemini?.antonymEn)
//            }}){
//                Text("Gemini paper")
//            }
//            Button(onClick = {scope.launch {
//                val gemini : GeminiResponse? = enrichWordByGemini(client, "computer")
//                println(gemini?.antonymEn)
//            }}){
//                Text("Gemini com")
//            }
//            allWordsWithTags.forEach {
//                WordScreen(it.word.name)
//            }
//        }
    }
}

suspend fun backend(client: HttpClient, api:String = ""): String{
    return try {
        val response: Data = client.get("${Secrets.LOCAL}/" + api).body()
        response.message
    } catch (e: Exception) {
        e.printStackTrace()
        "Error: ${e.message}"
    }
}

@Serializable
data class Data(
    val message:String = "No"
)

suspend fun backendPull(client: HttpClient, api: String = "sync/pullAll"): List<SerializableWord>? {
    return try {
        val response = client.get("${Secrets.BACKEND_API}/$api")

        Logger.d { "Server Status: ${response.status}" }

        val responseText = response.bodyAsText()
        Logger.d { "Server Response: $responseText" }

        return response.body<List<SerializableWord>>()
    } catch (e: Exception) {
        Logger.e { "API Error: ${e.message}" }
        e.printStackTrace()
        null
    }
}

@Composable
fun ExitConfirmDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1B202D),
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                text = "Exit App",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Text(
                text = "Are you sure you want to close the application?",
                color = Color(0xFF9BA1B0),
                fontSize = 14.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.padding(horizontal = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D65FF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("EXIT", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.White)
            }
        }
    )
}