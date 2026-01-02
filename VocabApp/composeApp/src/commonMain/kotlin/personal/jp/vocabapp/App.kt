package personal.jp.vocabapp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import db.Word
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
import personal.jp.vocabapp.google.ACCESS_TOKEN
import personal.jp.vocabapp.google.AuthRepository
import personal.jp.vocabapp.google.SecureStorage
import kotlinx.serialization.Serializable
import personal.jp.vocabapp.sql.SerializableWord

@Composable
@Preview
fun App() {
//    val service: WordServiceImpl = koinInject()

//    val words by produceState<List<Word>>(initialValue = emptyList(), service) {
//        value = service.getAllWords()
//    }
    MyScreen()
}

@Composable
fun MyScreen() {
    val authRepository: AuthRepository = koinInject()
    val secureStorage: SecureStorage = koinInject()
    val scope = rememberCoroutineScope()
    val client: HttpClient = koinInject()
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { authRepository.startLogin() }) {
                Text("Login with Google.")
            }
            Button(onClick = { showContent = !showContent }) {
                Text("Click meee!")
            }
            AnimatedVisibility(showContent) {
                val greeting = remember { Greeting().greet() }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Compose: $greeting")
                }
            }
            Button(onClick = {scope.launch {
                val token = secureStorage.getToken(ACCESS_TOKEN)
                println("Token is: $token")
            }}){
                Text("Check Tokens")
            }
            Button(onClick = {scope.launch {
                secureStorage.deleteToken(ACCESS_TOKEN)
                println("Token deleted")
            }}){
                Text("Clear Tokens")
            }

            Button(onClick = {scope.launch {
                println("Backend Test")
//                var words : List<Word> = backendPull(client)

                println(backendPull(client))
            }}){
                Text("Local hello")
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
        // Specify the type inside the body<...> brackets
        val response = client.get("${Secrets.LOCAL}/$api")
        return response.body<List<SerializableWord>>()
    } catch (e: Exception) {
        println("Serialization Error: ${e.message}")
        null
    }
}

