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
        val scope = "openid https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email"
        val encodedScope = URLEncoder.encode(scope, "UTF-8")

        val authUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=$clientId&" +
                "redirect_uri=$encodedRedirectUri&" +
                "scope=$encodedScope&" +
                "response_type=code"
        
        // launch custom browser tab
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(context, authUrl.toUri())
    }

    override fun stop(){

    }
}