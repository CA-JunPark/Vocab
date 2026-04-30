package personal.jp.vocabapp.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import personal.jp.vocabapp.google.AndroidLoginHandler
import personal.jp.vocabapp.google.LoginHandler
import personal.jp.vocabapp.google.SecureStorage
import personal.jp.vocabapp.google.createDataStorage
import personal.jp.vocabapp.internet.isNetworkAvailable
import personal.jp.vocabapp.widget.WidgetSyncManager
import personal.jp.vocabapp.widget.AndroidWidgetSyncManager

actual val platformModule = module {
    single<LoginHandler> { AndroidLoginHandler(androidContext()) }
    single { createDataStorage(androidContext()) }
    single { SecureStorage(get(), androidContext()) }
    single { isNetworkAvailable(androidContext())}
    single<WidgetSyncManager> { AndroidWidgetSyncManager(androidContext()) }
}