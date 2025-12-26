package personal.jp.vocabapp.di

data class Word(
    var name: String,
    var meaningKr: List<String>,
    var example: List<String>,
    var antonymEn: List<String>,
    var tags: List<String>,
    var createdTime: String,
    var modifiedTime: String,
    var isDeleted: Boolean
)
