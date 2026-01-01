package personal.jp.vocabapp.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun apiModule() = module{
    single { UserSession() }
    single{
        val session = get<UserSession>()
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
            defaultRequest {
                // This header is added to every request
                session.idToken?.let {
                    header("Authorization", "Bearer $it")
                }
            }
        }
    }
}

class UserSession {
    var idToken: String? = null
    var accessToken: String? = null
    var name: String? = null
    var email: String? = null
    var phoneNumber: String? = null
    var profilePicUrl: String? = null
}