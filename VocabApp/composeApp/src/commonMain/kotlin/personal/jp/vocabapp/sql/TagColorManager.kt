package personal.jp.vocabapp.sql

import db.Tag

class TagColorManager(
    private val palette: List<String> = listOf(
        // Warmer Tones
        "#FF8A80", "#FF5252", "#FF1744", // Reds
        "#FFAB40", "#FF9100",           // Oranges
        "#FFD740", "#FFC400",           // Yellows

        // Cooler Tones
        "#CCFF90", "#B2FF59", "#76FF03", // Greens
        "#A7FFEB", "#64FFDA", "#1DE9B6", // Teals
        "#80D8FF", "#40C4FF", "#00B0FF", // Sky Blues
        "#82B1FF", "#448AFF", "#2979FF", // Royal Blues

        // Purple & Pinks
        "#B388FF", "#7C4DFF",           // Purples
        "#F8BBD0", "#FF80AB", "#F50057", // Pinks

        // Neutrals
        "#CFD8DC", "#90A4AE"            // Greys
    )
) {
    /**
     * Get Tag List
     * Remove duplicate colors
     * assign random color from palette
     */
    fun assignColors(tags: List<Tag>): List<Tag> {
        // get used Colors in given tags
        val usedColors = tags
            .filter { it.color.isNotBlank() && it.color != "#808080" }
            .map { it.color }
            .toSet()

        // shuffle colors in palette
        val availableColors = palette.filter { it !in usedColors }
            .ifEmpty { palette } // if all colors are used, use original palette
            .shuffled()

        var newTagIndex = 0

        return tags.map { tag ->
            if (tag.color.isBlank() || tag.color == "#808080") {
                val color = availableColors[newTagIndex % availableColors.size]
                newTagIndex++
                tag.copy(color = color)
            } else {
                tag
            }
        }
    }

    fun getFullPalette(): List<String> = palette
}