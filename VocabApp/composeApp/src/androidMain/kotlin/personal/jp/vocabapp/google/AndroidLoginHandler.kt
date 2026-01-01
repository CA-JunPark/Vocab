package personal.jp.vocabapp.google

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import personal.jp.vocabapp.Secrets
import java.net.URLEncoder

class AndroidLoginHandler(private val context: Context) : LoginHandler {
    override fun login(onCodeReceived: (String) -> Unit) {
        val clientId = Secrets.ANDROID_CLIENT_ID
        val redirectUri = "personal.jp.vocabapp:/oauth2redirect"
        val encodedRedirectUri = URLEncoder.encode(redirectUri, "UTF-8")
        val encodedScope = URLEncoder.encode("https://www.googleapis.com/auth/userinfo.profile", "UTF-8")

        // 2. Build URL - Ensure no spaces or hidden characters
        val authUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=$clientId&" +
                "redirect_uri=$encodedRedirectUri&" +
                "scope=$encodedScope&" +
                "response_type=code"
        // 2. Launch the browser/Custom Tab
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(context, authUrl.toUri())

        // Note: 'onCodeReceived' can't be called directly here.
        // You must catch the result in your Activity's onNewIntent or via a Deep Link.
    }
}