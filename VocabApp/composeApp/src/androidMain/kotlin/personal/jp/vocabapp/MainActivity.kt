package personal.jp.vocabapp

import android.content.Intent
import android.net.Uri
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
import org.koin.android.ext.android.inject
import org.koin.core.context.GlobalContext
import personal.jp.vocabapp.di.apiModule
import personal.jp.vocabapp.di.authModule
import personal.jp.vocabapp.di.platformModule
import personal.jp.vocabapp.google.AuthFlowManager
import personal.jp.vocabapp.google.AuthRepository

// Android
class MainActivity : ComponentActivity() {
    private val authFlowManager: AuthFlowManager by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)


        if (GlobalContext.getOrNull() == null) {
            startKoin{
                androidContext(this@MainActivity)
                modules(wordModule(getDriverFactory(this@MainActivity)), apiModule(),
                    platformModule, authModule)
            }
        }

        setContent {
            App()
        }
        handleIntent(intent)
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val data: Uri? = intent?.data

        if (data != null && data.scheme == "personal.jp.vocabapp") {
            val code = data.getQueryParameter("code")
            code?.let { authFlowManager.onCodeReceived(it) }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}