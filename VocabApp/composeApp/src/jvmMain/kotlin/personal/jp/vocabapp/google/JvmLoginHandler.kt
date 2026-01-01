package personal.jp.vocabapp.google

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import personal.jp.vocabapp.Secrets
import java.awt.Desktop
import java.net.URI
import java.net.URLEncoder

class JvmLoginHandler : LoginHandler {
    override fun login(onCodeReceived: (String) -> Unit) {
        val port = 8080
        val redirectUri = "http://localhost:$port/callback"

        // Start the local server to listen for the callback
        val server = embeddedServer(Netty, port = port) {
            routing {
                get("/callback") {
                    val code = call.parameters["code"]
                    if (code != null) {
                        onCodeReceived(code)
                        call.respondText("Login successful! You can close this tab.")

                        // Optional: Stop the server after receiving the code
                        // server.stop(1000, 5000)
                    } else {
                        call.respondText("Authorization failed.")
                    }
                }
            }
        }.start(wait = false) // Use wait = false so the code continues to open the browser

        // Prepare the Google Auth URL
        val encodedRedirectUri = URLEncoder.encode(redirectUri, "UTF-8")
        val encodedScope = URLEncoder.encode("https://www.googleapis.com/auth/userinfo.profile", "UTF-8")

        val authUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=${Secrets.WEB_CLIENT_ID}&" +
                "redirect_uri=$encodedRedirectUri&" +
                "scope=$encodedScope&" +
                "access_type=offline&" +
                "prompt=consent&" +
                "response_type=code"

        // Open the browser
        Desktop.getDesktop().browse(URI(authUrl))
    }
}