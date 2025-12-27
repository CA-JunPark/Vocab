package personal.jp.vocabapp.di

import db.WordDatabase
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import personal.jp.vocabapp.sql.DriverFactory

fun appModule(driverFactory: DriverFactory) = module{
    single { WordDatabase(driverFactory.createDriver()) }
    singleOf(::WordRepoImpl) { bind<WordRepo>() }
    singleOf(::WordServiceImpl) { bind<WordService>() }
    viewModelOf(::WordViewModel)
}