package personal.jp.vocabapp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sunildhiman90.kmauth.core.KMAuthConfig
import com.sunildhiman90.kmauth.core.KMAuthInitializer
import com.sunildhiman90.kmauth.google.KMAuthGoogle
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
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
        println(g.backend())
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "VocabApp",
    ) {
        App()
    }
}

class Greet {
//    val jsonInstance = Json {
//        ignoreUnknownKeys = true
//        isLenient = true
//        encodeDefaults = true
//        coerceInputValues = false
//        explicitNulls = false
//    }

    val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun greet(): String {
        return try {
            val response = client.get("https://ktor.io/docs/"){
                headers {
                    // TODO add Google Token
                    append("Authorization", "Bearer {your_token_here}")
                    append("Accept", "application/json")
                }
            }
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