package personal.jp.vocabapp.widget

import db.Word

interface WidgetSyncManager {
    fun syncWord(word: Word?)
}