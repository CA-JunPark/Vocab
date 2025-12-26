package personal.jp.vocabapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform