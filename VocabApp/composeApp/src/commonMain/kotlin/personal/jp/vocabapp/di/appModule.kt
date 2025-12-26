package personal.jp.vocabapp.di

import org.koin.core.module.dsl.bind
import org.koin.dsl.module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf

val appModule = module{
    singleOf(::WordRepoImpl) { bind<WordRepo>() }
    singleOf(::WordServiceImpl) { bind<WordService>() }
    viewModelOf(::WordViewModel)
}