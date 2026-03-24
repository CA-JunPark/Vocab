package personal.jp.vocabapp.sql

import androidx.compose.ui.graphics.Color
import db.Tag
import kotlin.math.abs

class TagColorManager {

    /**
     * Assigns Compose Colors based on the tag's name hash.
     * Use this if your Tag model can hold a Compose Color or if
     * you are mapping them in your ViewModel.
     */
    fun getTagColor(tagName: String): Color {
        // Stable hash for consistency
        val hash = abs(tagName.hashCode())

        // Hue (0.0 to 360.0)
        val hue = (hash % 360).toFloat()

        // Saturation (0.0 to 1.0) - 65% for vibrant but readable tags
        val saturation = 0.65f

        // Lightness (0.6 to 0.8)
        // (hash % 21) gives 0-20. Dividing by 100f gives 0.0 to 0.2.
        val lightness = 0.6f + ((hash % 21) / 100f)

        // Return native Compose Color
        return Color.hsl(
            hue = hue,
            saturation = saturation,
            lightness = lightness
        )
    }

    fun assignColors(tags: List<Tag>): List<Tag> {
        return tags.map { tag ->
            if (tag.color.isBlank() || tag.color == "#808080") {
                // Convert the Compose color to a Hex String for DB storage
                val color = getTagColor(tag.tagName)
                tag.copy(color = colorToHexString(color))
            } else {
                tag
            }
        }
    }

    private fun colorToHexString(color: Color): String {
        return String.format("#%08X", color.value.toLong())
    }
}