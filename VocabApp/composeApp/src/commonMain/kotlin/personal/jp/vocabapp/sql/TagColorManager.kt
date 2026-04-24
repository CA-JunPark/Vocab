package personal.jp.vocabapp.sql

import androidx.compose.ui.graphics.Color
import db.Tag
import kotlin.math.abs

class TagColorManager {
    fun getTagColor(tagName: String): Color {
        val name = tagName.trim().lowercase()
        var hash = 0
        for (char in name) {
            hash = 31 * hash + char.code
        }
        val positiveHash = abs(hash)

        val hue = (positiveHash % 360).toFloat()

        val saturation = 0.85f

        val lightness = 0.52f + (positiveHash % 14) / 100f

        return Color.hsl(hue = hue, saturation = saturation, lightness = lightness)
    }

    fun assignColors(tags: List<Tag>): List<Tag> {
        return tags.map { tag ->
            val colorStr = tag.color.uppercase()
            if (tag.color.isBlank() || colorStr == "#808080" || colorStr == "#FF808080") {
                val color = getTagColor(tag.tagName)
                tag.copy(color = colorToHexString(color))
            } else {
                tag
            }
        }
    }

    fun colorToHexString(color: Color): String {
        val a = (color.alpha * 255).toInt().coerceIn(0, 255)
        val r = (color.red * 255).toInt().coerceIn(0, 255)
        val g = (color.green * 255).toInt().coerceIn(0, 255)
        val b = (color.blue * 255).toInt().coerceIn(0, 255)

        return "#" +
                a.toString(16).padStart(2, '0').uppercase() +
                r.toString(16).padStart(2, '0').uppercase() +
                g.toString(16).padStart(2, '0').uppercase() +
                b.toString(16).padStart(2, '0').uppercase()
    }
}

// for readability
fun getContrastColor(hexColor: String): Color {
    return try {
        val r = hexColor.substring(1, 3).toInt(16)
        val g = hexColor.substring(3, 5).toInt(16)
        val b = hexColor.substring(5, 7).toInt(16)
        // (YIQ equation)
        val yiq = ((r * 299) + (g * 587) + (b * 114)) / 1000
        if (yiq >= 128) Color.Black else Color.White
    } catch (e: Exception) {
        Color.White
    }
}