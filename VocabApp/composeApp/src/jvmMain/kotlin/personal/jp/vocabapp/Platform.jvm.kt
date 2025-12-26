package personal.jp.vocabapp

class JvmPlatform : Platform {
    override val name: String = "JVM"
}
actual fun getPlatform(): Platform = JvmPlatform()
