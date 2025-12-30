package personal.jp.vocabapp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sunildhiman90.kmauth.core.KMAuthConfig
import com.sunildhiman90.kmauth.core.KMAuthInitializer
import com.sunildhiman90.kmauth.google.KMAuthGoogle
import org.koin.core.context.startKoin
import personal.jp.vocabapp.di.wordModule
import personal.jp.vocabapp.sql.getDriverFactory

fun main() = application {

    KMAuthInitializer.initialize(KMAuthConfig.forGoogle(webClientId = Secrets.WEB_CLIENT_ID))
    KMAuthInitializer.initClientSecret(
        clientSecret = Secrets.WEB_CLIENT_SECRET,
    )
    KMAuthGoogle.googleAuthManager

    startKoin{
        modules(wordModule(getDriverFactory()))
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "VocabApp",
    ) {
        App()
    }
}
