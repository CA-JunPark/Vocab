package personal.jp.vocabapp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sunildhiman90.kmauth.core.KMAuthConfig
import com.sunildhiman90.kmauth.core.KMAuthInitializer
import com.sunildhiman90.kmauth.google.KMAuthGoogle
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin
import personal.jp.vocabapp.di.wordModule
import personal.jp.vocabapp.sql.getDriverFactory

// JVM
fun main() = application {

    KMAuthInitializer.initialize(KMAuthConfig.forGoogle(webClientId = Secrets.WEB_CLIENT_ID))
    KMAuthInitializer.initClientSecret(
        clientSecret = Secrets.WEB_CLIENT_SECRET,
    )
    KMAuthGoogle.googleAuthManager

    startKoin{
        modules(wordModule(getDriverFactory()))
    }

    val g = Greet()
    runBlocking {
        println(g.greet())
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "VocabApp",
    ) {
        App()
    }
}

class Greet {
    private val client = HttpClient()

    suspend fun greet(): String {
        return try {
            val response = client.get("https://ktor.io/docs/")
            response.bodyAsText()
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}