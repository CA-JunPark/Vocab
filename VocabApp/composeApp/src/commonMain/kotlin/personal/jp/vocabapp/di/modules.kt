package personal.jp.vocabapp.di

import db.WordDatabase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import personal.jp.vocabapp.getPlatform
import personal.jp.vocabapp.google.AuthFlowManager
import personal.jp.vocabapp.google.AuthRepository
import personal.jp.vocabapp.google.authClient
import personal.jp.vocabapp.sql.DriverFactory
import personal.jp.vocabapp.sql.KeyDataManager
import personal.jp.vocabapp.sql.TagColorManager
import personal.jp.vocabapp.sql.WordRepo
import personal.jp.vocabapp.sql.WordRepoImpl
import personal.jp.vocabapp.sql.WordService
import personal.jp.vocabapp.sql.WordServiceImpl
import personal.jp.vocabapp.viewmodels.WordViewModel


expect val platformModule: Module

val authModule = module {
    single { AuthFlowManager() }
    single { authClient(get())}
    single { AuthRepository(get(), get(), get(), getPlatform(), get()) }
}

fun wordModule(driverFactory: DriverFactory) = module{
    single { WordDatabase(driverFactory.createDriver()) }
    single { TagColorManager() }
    single<WordRepo> { WordRepoImpl(get()) }
    single { WordServiceImpl(get(), get()) } bind WordService::class
    viewModel { WordViewModel(get<WordServiceImpl>()) }
}

fun apiModule() = module{
    single { KeyDataManager(get()) }
}

