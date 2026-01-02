package personal.jp.vocabapp.di

import org.koin.core.module.Module
import org.koin.dsl.module
import personal.jp.vocabapp.getPlatform
import personal.jp.vocabapp.google.AuthFlowManager
import personal.jp.vocabapp.google.AuthRepository
import personal.jp.vocabapp.google.SecureStorage
import personal.jp.vocabapp.google.authClient

import db.WordDatabase
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import personal.jp.vocabapp.sql.DriverFactory
import personal.jp.vocabapp.sql.WordRepo
import personal.jp.vocabapp.sql.WordRepoImpl
import personal.jp.vocabapp.sql.WordService
import personal.jp.vocabapp.sql.WordServiceImpl
import personal.jp.vocabapp.sql.WordViewModel


expect val platformModule: Module

val authModule = module {
    single { AuthFlowManager() }
    single { authClient(get())}
    single { AuthRepository(get(), get(), get(), getPlatform(), get()) }
}

fun wordModule(driverFactory: DriverFactory) = module{
    single { WordDatabase(driverFactory.createDriver()) }
    singleOf(::WordRepoImpl) { bind<WordRepo>() }
    singleOf(::WordServiceImpl) { bind<WordService>() }
    viewModelOf(::WordViewModel)
}

fun apiModule() = module{

}

