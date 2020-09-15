package webserver

import mu.KLogging
import java.net.ServerSocket
import java.net.Socket

object WebServer: KLogging() {
    private const val DEFAULT_PORT = 8080

    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        var port = 0
        port = if (args == null || args.size == 0) {
            DEFAULT_PORT
        } else {
            args[0].toInt()
        }
        ServerSocket(port).use { listenSocket ->
            logger.info("Web Application Server started {} port.", port)

            // 클라이언트가 연결될때까지 대기한다.
            var connection: Socket?
            while (listenSocket.accept().also { connection = it } != null) {
                val requestHandler = RequestHandler(connection!!)
                requestHandler.start()
            }
        }
    }
}