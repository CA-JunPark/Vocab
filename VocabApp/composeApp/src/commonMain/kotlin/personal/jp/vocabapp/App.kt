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
import com.sunildhiman90.kmauth.google.compose.GoogleSignInButton
import db.Word
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import personal.jp.vocabapp.di.WordServiceImpl
import vocabapp.composeapp.generated.resources.Res
import vocabapp.composeapp.generated.resources.compose_multiplatform
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import personal.jp.vocabapp.google.AuthRepository
import personal.jp.vocabapp.google.GoogleProfile
import personal.jp.vocabapp.google.authClient

@Composable
@Preview
fun App() {
    val service: WordServiceImpl = koinInject()

    val words by produceState<List<Word>>(initialValue = emptyList(), service) {
        value = service.getAllWords()
    }
    MyScreen(data=words)
}

@Composable
fun MyScreen(data: List<Word> = emptyList()) {
    val authRepository: AuthRepository = koinInject()
    val tokenPreview by authRepository.accessTokenPreview.collectAsState()
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
            Text("Token: ${tokenPreview ?: "Not Logged In"}")
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
                    Text("Koin: $data")
                }
            }
        }
    }
}

