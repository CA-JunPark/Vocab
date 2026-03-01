package personal.jp.vocabapp.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import vocabapp.composeapp.generated.resources.Res
import vocabapp.composeapp.generated.resources.lexend_bold
import vocabapp.composeapp.generated.resources.lexend_medium
import vocabapp.composeapp.generated.resources.lexend_regular
import vocabapp.composeapp.generated.resources.lexend_semibold

@Composable
fun getLexendFontFamily() = FontFamily(
    Font(Res.font.lexend_regular, FontWeight.Normal),
    Font(Res.font.lexend_medium, FontWeight.Medium),
    Font(Res.font.lexend_semibold, FontWeight.SemiBold),
    Font(Res.font.lexend_bold, FontWeight.Bold)
)

// 2. Wrap the Typography in a Composable function
@Composable
fun getAppTypography(): Typography {
    val lexend = getLexendFontFamily()

    return Typography(
        displayMedium = TextStyle(
            fontFamily = lexend,
            fontWeight = FontWeight.Bold,
            fontSize = 36.sp,
            lineHeight = 40.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = lexend,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 32.sp
        ),
        titleLarge = TextStyle(
            fontFamily = lexend,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 28.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = lexend,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = lexend,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        labelSmall = TextStyle(
            fontFamily = lexend,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    )
}