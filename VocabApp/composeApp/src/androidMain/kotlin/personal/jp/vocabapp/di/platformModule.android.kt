package personal.jp.vocabapp.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import personal.jp.vocabapp.google.AndroidLoginHandler
import personal.jp.vocabapp.google.LoginHandler
import personal.jp.vocabapp.google.SecureStorage
import personal.jp.vocabapp.google.createDataStorage
import personal.jp.vocabapp.internet.isNetworkAvailable

actual val platformModule = module {
    single<LoginHandler> { AndroidLoginHandler(get()) }
    single { createDataStorage(androidContext()) }
    single { SecureStorage(get(), androidContext()) }
    single { isNetworkAvailable(androidContext())}
}