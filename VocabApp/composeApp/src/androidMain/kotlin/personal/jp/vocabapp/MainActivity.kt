package personal.jp.vocabapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.cash.sqldelight.db.SqlDriver
import db.Word
import db.WordDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import personal.jp.vocabapp.di.WordServiceImpl
import personal.jp.vocabapp.di.appModule
import personal.jp.vocabapp.sql.DriverFactory
import co.touchlab.kermit.Logger
import com.sunildhiman90.kmauth.core.KMAuthConfig
import com.sunildhiman90.kmauth.core.KMAuthInitializer
import com.sunildhiman90.kmauth.core.KMAuthPlatformContext
import com.sunildhiman90.kmauth.google.KMAuthGoogle
import personal.jp.vocabapp.sql.getDriverFactory

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
            modules(appModule(getDriverFactory(this@MainActivity)))
        }

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}