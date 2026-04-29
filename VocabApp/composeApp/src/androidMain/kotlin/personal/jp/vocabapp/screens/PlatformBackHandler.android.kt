package personal.jp.vocabapp.screens

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun PlatformBackHandler(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
}

actual fun Modifier.desktopBackHandler(onBack: () -> Unit): Modifier = this