package personal.jp.vocabapp

import androidx.compose.animation.AnimatedContent
import personal.jp.vocabapp.theme.VocabTheme
import androidx.compose.runtime.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import personal.jp.vocabapp.sql.WordServiceImpl
import kotlinx.coroutines.launch
import personal.jp.vocabapp.google.AuthRepository
import personal.jp.vocabapp.google.SecureStorage
import kotlinx.serialization.Serializable
import co.touchlab.kermit.Logger
import io.ktor.client.statement.bodyAsText
import personal.jp.vocabapp.Screens.AddWordScreen
import personal.jp.vocabapp.Screens.MainScreen
import personal.jp.vocabapp.Screens.Screen
import personal.jp.vocabapp.sql.SerializableWord
import personal.jp.vocabapp.sql.KeyDataManager
import personal.jp.vocabapp.viewmodels.WordWithTags
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import personal.jp.vocabapp.Screens.SettingsScreen
import personal.jp.vocabapp.sql.sync

@Composable
@Preview
fun App() {
    MyScreen()
}

@Composable
fun MyScreen() {
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

    // refresh Words
    val refreshWords = {
        scope.launch {
            val rawWords = service.getAllWords()
            allWordsWithTags = rawWords.map { word ->
                WordWithTags(word, service.getTagsForWord(word.name))
            }
            checkPendingChanges()
        }
    }
    LaunchedEffect(Unit) {
        refreshWords()
    }

    VocabTheme {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                if (targetState is Screen.AddWord) {
                    (slideInVertically { it } + fadeIn()).togetherWith(fadeOut())
                } else {
                    fadeIn().togetherWith(slideOutVertically { it } + fadeOut())
                }
            }
        ){ screen ->
            when (screen) {
                is Screen.Home -> {
                    MainScreen(
                        userProfile,
                        wordsList = allWordsWithTags,
                        onAddClick = { currentScreen = Screen.AddWord },
                        onSettingsClick = { currentScreen = Screen.Settings }
                    )
                }

                is Screen.AddWord -> {
                    AddWordScreen(
                        service,
                        client,
                        onClose = { currentScreen = Screen.Home },
                        onSave = { targetWord, definitions, tags ->
                            scope.launch {
                                // TODO: DB Save logic
                                // ex: wordService.saveWordWithMultipleDefinitions(targetWord, definitions, tags)
                                refreshWords()
                                currentScreen = Screen.Home
                            }
                        }
                    )
                }

                is Screen.WordDetail -> {
                    //TODO
                }

                is Screen.Settings -> {
                    SettingsScreen(
                        userProfile = userProfile,
                        isLoginInProgress = isLoginInProgress,
                        hasPendingChanges = hasPendingChanges,
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
                                sync(client, service, keyDataManager)
                                refreshWords()
                            }
                        },
                        onCancelLogin = {
                            scope.launch { authRepository.cancelLogin() }
                        },
                        onDeleteTagsComplete = {
                            refreshWords()
                        }
                    )
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

