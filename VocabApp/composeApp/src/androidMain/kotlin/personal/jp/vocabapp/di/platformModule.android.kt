package personal.jp.vocabapp.di

import org.koin.core.module.Module
import org.koin.dsl.module
import personal.jp.vocabapp.google.AndroidLoginHandler
import personal.jp.vocabapp.google.LoginHandler

actual val loginModule = module {
    single<LoginHandler> { AndroidLoginHandler(get()) }
}