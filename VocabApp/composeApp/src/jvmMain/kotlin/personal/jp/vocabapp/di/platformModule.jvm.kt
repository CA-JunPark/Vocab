package personal.jp.vocabapp.di

import org.koin.core.module.Module
import org.koin.dsl.module
import personal.jp.vocabapp.google.JvmLoginHandler
import personal.jp.vocabapp.google.LoginHandler
import personal.jp.vocabapp.google.SecureStorage
import personal.jp.vocabapp.google.createDataStorage
import personal.jp.vocabapp.internet.isNetworkAvailable

actual val platformModule = module {
    single<LoginHandler> { JvmLoginHandler() }
    single {createDataStorage(null)}
    single { SecureStorage(get()) }
    single {isNetworkAvailable(null)}
}