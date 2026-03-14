package personal.jp.vocabapp

import personal.jp.vocabapp.theme.VocabTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import personal.jp.vocabapp.sql.WordServiceImpl
import vocabapp.composeapp.generated.resources.Res
import vocabapp.composeapp.generated.resources.compose_multiplatform
import kotlinx.coroutines.launch
import personal.jp.vocabapp.google.AuthRepository
import personal.jp.vocabapp.google.SecureStorage
import kotlinx.serialization.Serializable
import personal.jp.vocabapp.google.GeminiResponse
import personal.jp.vocabapp.google.enrichWordByGemini
import co.touchlab.kermit.Logger
import io.ktor.client.statement.bodyAsText
import personal.jp.vocabapp.google.ID_TOKEN
import personal.jp.vocabapp.sql.SerializableWord
import personal.jp.vocabapp.sql.sync
import personal.jp.vocabapp.sql.KeyDataManager
import personal.jp.vocabapp.sql.prepareWordData
import personal.jp.vocabapp.viewmodels.VocabularyCard
import personal.jp.vocabapp.viewmodels.WordScreen
import personal.jp.vocabapp.viewmodels.WordWithTags

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

    var allWordsWithTags by remember { mutableStateOf<List<WordWithTags>>(emptyList()) }
    LaunchedEffect(Unit) {
        val words = service.getAllWords()
        allWordsWithTags = words.map { word ->
            val tags = service.getTagsForWord(word.name)
            WordWithTags(word, tags)
        }
    }
    VocabTheme {
        var showContent by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { scope.launch{
                authRepository.startLogin()
            } }) {
                Text("Login with Google.")
            }
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
            allWordsWithTags.forEach {
                WordScreen(it.word.name)
            }
        }
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

