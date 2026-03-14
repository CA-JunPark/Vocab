package personal.jp.vocabapp.Screens

sealed class Screen {
    object Home : Screen()
    object AddWord : Screen()
    object Settings : Screen()
    data class WordDetail(val wordName: String) : Screen()
}
