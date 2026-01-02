package personal.jp.vocabapp.sql

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