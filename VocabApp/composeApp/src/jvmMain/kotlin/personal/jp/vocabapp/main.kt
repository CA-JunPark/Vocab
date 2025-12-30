package personal.jp.vocabapp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import co.touchlab.kermit.Logger
import com.sunildhiman90.kmauth.core.KMAuthConfig
import com.sunildhiman90.kmauth.core.KMAuthInitializer
import com.sunildhiman90.kmauth.google.KMAuthGoogle
import db.Word
import org.koin.core.context.startKoin
import personal.jp.vocabapp.di.WordServiceImpl
import personal.jp.vocabapp.di.appModule
import personal.jp.vocabapp.sql.getDriverFactory

fun main() = application {

    KMAuthInitializer.initialize(KMAuthConfig.forGoogle(webClientId = Secrets.WEB_CLIENT_ID))
    KMAuthInitializer.initClientSecret(
        clientSecret = Secrets.WEB_CLIENT_SECRET,
    )
    KMAuthGoogle.googleAuthManager

    startKoin{
        modules(appModule(getDriverFactory()))
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "VocabApp",
    ) {
        App()
    }
}
