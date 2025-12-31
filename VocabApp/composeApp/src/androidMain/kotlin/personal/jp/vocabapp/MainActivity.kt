package personal.jp.vocabapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import personal.jp.vocabapp.di.wordModule
import com.sunildhiman90.kmauth.core.KMAuthConfig
import com.sunildhiman90.kmauth.core.KMAuthInitializer
import com.sunildhiman90.kmauth.core.KMAuthPlatformContext
import com.sunildhiman90.kmauth.google.KMAuthGoogle
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import personal.jp.vocabapp.sql.getDriverFactory
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable

// Android
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        KMAuthInitializer.initContext(
            kmAuthPlatformContext = KMAuthPlatformContext(this)
        )
        KMAuthInitializer.initialize(KMAuthConfig.forGoogle(webClientId = Secrets.WEB_CLIENT_ID))

        KMAuthGoogle.googleAuthManager

        startKoin{
            androidContext(this@MainActivity)
            modules(wordModule(getDriverFactory(this@MainActivity)))
        }

        setContent {
            App()
//            GreetingScreen()
        }

    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}

class Greet {
    val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }
    suspend fun greet(): String {
        return try {
            val response = client.get("https://ktor.io/docs/")
            response.bodyAsText()
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
    suspend fun backend(): String{
        return try {
            val response: Data = client.get("${Secrets.BACKEND_API}/").body()
            response.message
        } catch (e: Exception) {
            e.printStackTrace()
            "Error: ${e.message}"
        }
    }
}

@Serializable
data class Data(
    val message:String = "No"
)
class MainViewModel : ViewModel() {
    private val greeter = Greet()

    // Use a State to hold the result for Compose
    private val _greetingText = mutableStateOf("Loading...")
    val greetingText: State<String> = _greetingText

    fun fetchGreeting() {
        // 'launch' starts the coroutine without blocking the main thread
        viewModelScope.launch {
            val result = greeter.backend()
            _greetingText.value = result // UI updates automatically
        }
    }
}

@Composable
fun GreetingScreen(viewModel: MainViewModel = viewModel()) {
    // This runs ONCE when the Composable enters the screen
    LaunchedEffect(Unit) {
        viewModel.fetchGreeting()
    }

    val text by viewModel.greetingText
    Text(text = text)
}