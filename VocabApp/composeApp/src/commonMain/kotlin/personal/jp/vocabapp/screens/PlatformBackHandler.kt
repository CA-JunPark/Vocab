package personal.jp.vocabapp.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PlatformBackHandler(onBack: () -> Unit)

expect fun Modifier.desktopBackHandler(onBack: () -> Unit): Modifier