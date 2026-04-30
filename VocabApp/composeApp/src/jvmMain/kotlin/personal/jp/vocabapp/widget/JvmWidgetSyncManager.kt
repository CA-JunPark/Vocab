package personal.jp.vocabapp.widget

import db.Word
import co.touchlab.kermit.Logger

class JvmWidgetSyncManager : WidgetSyncManager {
    override fun syncWord(word: Word?) {
        Logger.d("JVM Platform: Widget sync ignored for ${word?.name}")
    }
}