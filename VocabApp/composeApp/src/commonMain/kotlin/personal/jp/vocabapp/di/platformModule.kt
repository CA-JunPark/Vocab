package personal.jp.vocabapp.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import personal.jp.vocabapp.google.AuthFlowManager
import personal.jp.vocabapp.google.AuthRepository
import personal.jp.vocabapp.google.authClient

expect val platformModule: Module

val commonModule = module {
    single { AuthFlowManager() }
    single { authClient()}
    single { AuthRepository(get(), get(), get()) }
}