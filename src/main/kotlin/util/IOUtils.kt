package util

import java.io.BufferedReader
import java.io.IOException

object IOUtils {
    /**
     * @param BufferedReader는
     * Request Body를 시작하는 시점이어야
     * @param contentLength는
     * Request Header의 Content-Length 값이다.
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun readData(br: BufferedReader, contentLength: Int): String {
        val body = CharArray(contentLength)
        br.read(body, 0, contentLength)
        return String(body)
    }
}