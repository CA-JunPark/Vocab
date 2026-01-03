package personal.jp.vocabapp.internet
import java.net.InetSocketAddress
import java.net.Socket


actual fun isNetworkAvailable(context:Any?): Boolean {
    return try {
        val timeoutMs = 1500
        val socket = Socket()
        // Connect to Google DNS (8.8.8.8) on port 53
        socket.connect(InetSocketAddress("8.8.8.8", 53), timeoutMs)
        socket.close()
        true
    } catch (e: Exception) {
        false
    }
}