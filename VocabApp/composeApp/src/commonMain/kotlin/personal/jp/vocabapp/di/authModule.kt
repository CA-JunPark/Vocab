package personal.jp.vocabapp.di

import androidx.lifecycle.ViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import personal.jp.vocabapp.getPlatform
import personal.jp.vocabapp.google.AuthFlowManager
import personal.jp.vocabapp.google.AuthRepository
import personal.jp.vocabapp.google.SecureStorage
import personal.jp.vocabapp.google.authClient
import java.util.prefs.Preferences

expect val loginModule: Module

val authModule = module {
    single { AuthFlowManager() }
    single { authClient()}
    single { AuthRepository(get(), get(), get(), getPlatform(), get()) }
    single<Preferences> { Preferences.userRoot().node("com/vocab/secure")}
    single { SecureStorage(get()) }
}
