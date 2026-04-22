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
import co.touchlab.kermit.Logger
class JvmLoginHandler : LoginHandler {
    private var server: io.ktor.server.engine.EmbeddedServer<*, *>? = null

    override fun login(onCodeReceived: (String) -> Unit) {
        val port = 8080
        val redirectUri = "http://localhost:$port/callback"

        stop()

        // Start the local server to listen for the callback
        server = embeddedServer(Netty, port = port) {
            routing {
                get("/callback") {
                    val code = call.parameters["code"]
                    if (code != null) {
                        call.respondText("Login successful! You can close this tab.")
                        onCodeReceived(code)
                    } else {
                        call.respondText("Authorization failed.")
                    }
                }
            }
        }.start(wait = false) // Use wait = false so the code continues to open the browser

        // Prepare the Google Auth URL
        val encodedRedirectUri = URLEncoder.encode(redirectUri, "UTF-8")
        val scope = "openid https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email"
        val encodedScope = URLEncoder.encode(scope, "UTF-8")

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

    override fun stop() {
        val currentServer = server
        server = null // Clear reference immediately to prevent multiple stop calls

        if (currentServer != null) {
            // Run shutdown in a separate thread to avoid blocking the current execution context
            Thread {
                try {
                    // Grace period of 1s, timeout of 3s
                    currentServer.stop(2000, 4000)
                } catch (e: Exception) {
                    // Netty often throws RejectedExecutionException during shutdown;
                    // since we are closing the app's login server, we ignore it.
                    if (e.toString().contains("RejectedExecutionException")) {
                        Logger.d("Netty shutdown ignored: ${e.message}")
                    } else {
                        Logger.e("Server stop error: ${e.message}")
                    }
                }
            }.start()
        }
    }
}