package personal.jp.vocabapp.screens

import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent

@Composable
actual fun PlatformBackHandler(onBack: () -> Unit) {
}

@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.desktopBackHandler(onBack: () -> Unit): Modifier =
    this.onPointerEvent(PointerEventType.Press) { event ->
        if (event.button == PointerButton.Back) {
            onBack()
        }
    }