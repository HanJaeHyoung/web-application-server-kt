package webserver

import model.HttpMethod
import model.HttpRequest
import model.HttpResponse
import model.ResponseCode
import mu.KLogging
import util.HttpRequestUtils
import util.IOUtils
import webserver.ServiceHandler.Companion.getContentType
import java.io.*
import java.net.Socket
import java.nio.file.Files

class RequestHandler(private val connection: Socket) : Thread() {
    override fun run() {
        logger.debug(
            "New Client Connect! Connected IP : {}, Port : {}", connection.inetAddress,
            connection.port
        )
        try {
            connection.getInputStream().use { `in` ->
                connection.getOutputStream().use { out ->
                    // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
                    val request = getHttpRequest(`in`)
                    val response: HttpResponse = if (request.uri.startsWith("/user")) {
                        val serviceHandler = UserServiceHandler()
                        serviceHandler.handle(request)
                    } else {
                        val body = getBodyContent(request.uri)
                        val contentType = getContentType(request.uri)
                        val headers = mapOf("Content-Type" to "$contentType;charset=utf-8")

                        HttpResponse(status = ResponseCode.OK, headers = headers, body = body)
                    }

                    val dos = DataOutputStream(out)

                    ResponseHandler(dos, response).responseHeader()
                    if (response.body.isNotEmpty()) {
                        ResponseHandler(dos, response).responseBody()
                    }
                }
            }
        } catch (e: IOException) {
            logger.error(e.message)
        }
    }

    private fun getHttpRequest(`in`: InputStream): HttpRequest {
        val br = BufferedReader(InputStreamReader(`in`, "UTF-8"))
        var line = br.readLine()
        val tokens = line!!.split(" ").toTypedArray()
        var contentLength = 0
        var cookie = mutableMapOf<String, String>()
        val url = tokens[1]
        val headers = mutableMapOf<String, String>()


        while ("" != line) {
            logger.debug("header : {} ", line)
            line = br.readLine()
            if (line.contains("Content-Length")) {
                contentLength = getContentLength(line)
            }
            if (line.contains("Cookie")) {
                val cookies = line.split(": ")[1].split(";")
                cookies.forEach {
                    val cookieInfo = it.split("=")
                    cookie[cookieInfo[0]] = cookieInfo[1]
                }
                logger.info("cookies :: $cookies")
            }
            if ("" != line) {
                val header = line.split(": ")
                headers[header[0]] = header[1]
            }
        }
        val body = IOUtils.readData(br, contentLength)
        val request = HttpRequest(
            method = HttpMethod.valueOf(tokens[0]),
            uri = url,
            body = body,
            headers = headers,
            cookies = cookie
        )
        if ("" != body)
            request.queryStrings = HttpRequestUtils.parseQueryString(body)

        return request
    }

    private fun getBodyContent(url: String): ByteArray {
        return Files.readAllBytes(File("./webapp/$url").toPath())
    }

    private fun getContentLength(line: String?): Int {
        val headerTokens = line!!.split(":").toTypedArray()
        return headerTokens[1].trim { it <= ' ' }.toInt()
    }

    companion object : KLogging() {
        const val HTTP = "HTTP/1.1"
    }

}