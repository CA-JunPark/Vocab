package personal.jp.vocabapp.sql
import com.russhwolf.settings.Settings
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class KeyDataManager(private val settings: Settings) {
    // Save the time
    fun saveLastSync() {
        settings.putString("lastSyncedTime", getSqlTimestamp())
    }

    // Retrieve the time (default to 0 if not found)
    fun getLastSync(): String {
        return settings.getString("lastSyncedTime", "")
    }

    fun getSqlTimestamp(): String {
        val now = Clock.System.now()
        // use TimeZone.UTC for consistency
        val dateTime = now.toLocalDateTime(TimeZone.UTC)

        return buildString {
            append(dateTime.year)
            append("-")
            append(dateTime.monthNumber.toString().padStart(2, '0'))
            append("-")
            append(dateTime.dayOfMonth.toString().padStart(2, '0'))
            append(" ")
            append(dateTime.hour.toString().padStart(2, '0'))
            append(":")
            append(dateTime.minute.toString().padStart(2, '0'))
            append(":")
            append(dateTime.second.toString().padStart(2, '0'))
        }
    }
}

