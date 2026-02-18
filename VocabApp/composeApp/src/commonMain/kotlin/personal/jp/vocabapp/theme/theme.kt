import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// --- Color Constants (Zen Learner Palette) ---

// Dark Mode Colors
val ZenDarkBackground = Color(0xFF1A1C19)
val ZenDarkSurface = Color(0xFF232621)
val ZenDarkSurfaceVariant = Color(0xFF3F493E)
val ZenDarkPrimary = Color(0xFFB1D1A8)
val ZenDarkPrimaryContainer = Color(0xFF384B34)
val ZenDarkOnPrimaryContainer = Color(0xFFD1E7D1)
val ZenDarkOnSurface = Color(0xFFE2E3DE)
val ZenDarkOnSurfaceVariant = Color(0xFFA0A39D)

private val ColorScheme = darkColorScheme(
    primary = ZenDarkPrimary,
    onPrimary = ZenDarkBackground,
    primaryContainer = ZenDarkPrimaryContainer,
    onPrimaryContainer = ZenDarkOnPrimaryContainer,
    background = ZenDarkBackground,
    onBackground = ZenDarkOnSurface,
    surface = ZenDarkSurface,
    onSurface = ZenDarkOnSurface,
    surfaceVariant = ZenDarkSurfaceVariant,
    onSurfaceVariant = ZenDarkOnSurfaceVariant,
    outline = ZenDarkSurfaceVariant
)

// --- Theme Composable ---

@Composable
fun VocabAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ColorScheme,
        content = content
    )
}