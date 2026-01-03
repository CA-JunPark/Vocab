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
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
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
import personal.jp.vocabapp.internet.isNetworkAvailable
import personal.jp.vocabapp.sql.SerializableWord
import personal.jp.vocabapp.sql.createWord
import personal.jp.vocabapp.sql.toSerializable

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
    println("isNetworkAvailable: $isNetworkAvailable")

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
                println("DB Pull")
                println(backendPull(client))
            }}){
                Text("DB pull")
            }
            Button(onClick = {scope.launch {
                service.deleteAllWords()
                println("Add word")
                println("Count: ${service.countWords()}")
                try{
                    service.addWord(
                        createWord(
                            name = "potato",
                            meaningKr = "감자",
                            example = "I had potato",
                        )
                    )
                    service.addWord(
                        createWord(
                            name = "Sweet potato",
                            meaningKr = "고구마",
                            example = "I had sweet potato",
                        )
                    )
                } catch (e: Exception){
                    println("Error: ${e.message}")
                }
                println("Count: ${service.countWords()}")
            }}){
                Text("Add Word")
            }
            Button(onClick = {scope.launch {
                println("DB Push ALL")
                val words = toSerializable(service.getAllWords())
                println(backendPush(client, words))
            }}){
                Text("DB push")
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

suspend fun backendPush(
    client: HttpClient,
    tasks: List<SerializableWord>,
    api: String = "sync/push"
): Data {
    println("tasks $tasks")
    return try {
        val response = client.post("${Secrets.LOCAL}/$api") {
            // Set content type so the server knows it's JSON
            contentType(ContentType.Application.Json)
            // Ktor automatically serializes the list because of ContentNegotiation
            setBody(tasks)
        }

        // Directly return the deserialized body
        response.body<Data>()
    } catch (e: Exception) {
        // Log the actual error for better debugging
        println("Network or Serialization Error: ${e.message}")
        // Return a default object or handle error state
        Data()
    }
}
