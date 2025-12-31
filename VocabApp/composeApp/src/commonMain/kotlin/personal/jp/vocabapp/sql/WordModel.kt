package personal.jp.vocabapp.sql

import kotlinx.serialization.Serializable

@Serializable
data class WordModel(
    val name: String,
    val meaningKr: String,
    val example: String,
    val antonymEn: String,
    val tags: String? = null,
    val createdTime: String? = null,
    val modifiedTime: String? = null,
    val isDeleted: Boolean = false,
    val synced: Boolean = false
)