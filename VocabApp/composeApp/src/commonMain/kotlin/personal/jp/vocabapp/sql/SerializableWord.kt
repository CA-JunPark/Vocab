package personal.jp.vocabapp.sql

import db.Word
import kotlinx.serialization.Serializable

@Serializable
public data class SerializableWord(
    public val name: String,
    public val meaningKr: String,
    public val example: String?,
    public val antonymEn: String?,
    public val tags: String?,
    public val createdTime: String,
    public val modifiedTime: String,
    public val isDeleted: Boolean,
    public val synced: Boolean,
)

fun toSerializable(words:List<Word>): List<SerializableWord> {
    println("toSerializable $words")
    return words.map{
        SerializableWord(
            name = it.name,
            meaningKr = it.meaningKr,
            example = it.example,
            antonymEn = it.antonymEn,
            tags = it.tags,
            createdTime = it.createdTime,
            modifiedTime = it.modifiedTime,
            isDeleted = it.isDeleted,
            synced = it.synced
        )
    }
}

fun createWord(name: String, meaningKr: String, example: String?, antonymEn: String? = "", tags: String? = ""): Word{
    return Word(
        name = name,
        meaningKr = meaningKr,
        example = example,
        // default values
        antonymEn = antonymEn,
        tags = tags,
        createdTime = "",
        modifiedTime = "",
        isDeleted = false,
        synced = false
    )
}