package webserver

import db.DataBase
import model.User
import mu.KLogging
import util.HttpRequestUtils
import util.IOUtils
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
                    val br = BufferedReader(InputStreamReader(`in`, "UTF-8"))
                    var line = br.readLine()
                    val tokens = line!!.split(" ").toTypedArray()
                    var contentLength = 0
                    val url = tokens[1]
                    if (line == null) {
                        return
                    }
                    while ("" != line) {
                        logger.debug("header : {} ", line)
                        line = br.readLine()
                        if (line.contains("Content-Length")) {
                            contentLength = getContentLength(line)
                        }
                    }
                    if ("/user/create" == url) {
                        val body: String = IOUtils.readData(br, contentLength)
                        val params: Map<String, String> =
                            HttpRequestUtils.parseQueryString(body)
                        val user =
                            User(params["userId"], params["password"], params["name"], params["email"])
                        val dos = DataOutputStream(out)
                        response302Header(dos, "/index.html")
                        DataBase.addUser(user)
                    } else if ("/user/login" == url) {
                        val body: String = IOUtils.readData(br, contentLength)
                        val params: Map<String, String> =
                            HttpRequestUtils.parseQueryString(body)
                        val user = DataBase.findUserById(params["userId"])
                        if (user == null) {
                            responseResource(out, "/user/login_failed.html")
                            return
                        }
                        if (user.password.equals(params["password"])) {
                            val dos = DataOutputStream(out)
                            response302LoginSuccessHeader(dos)
                        } else {
                            responseResource(out, "/user/login_failed.html")
                        }
                    } else {
                        responseResource(out, url)
                    }
                    val dos = DataOutputStream(out)
                    val body = Files.readAllBytes(File("./webapp$url").toPath())
                    response200Header(dos, body.size)
                    responseBody(dos, body)
                }
            }
        } catch (e: IOException) {
            logger.error(e.message)
        }
    }

    private fun response302Header(dos: DataOutputStream, url: String) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n")
            dos.writeBytes("Location: $url \r\n")
            dos.writeBytes("\r\n")
        } catch (e: IOException) {
            logger.error(e.message)
        }
    }

    private fun response302LoginSuccessHeader(dos: DataOutputStream) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n")
            dos.writeBytes("Set-Cookie : logined=true \r\n")
            dos.writeBytes("Location: /index.html \r\n")
            dos.writeBytes("\r\n")
        } catch (e: IOException) {
            logger.error(e.message)
        }
    }

    @Throws(IOException::class)
    private fun responseResource(out: OutputStream, url: String) {
        val dos = DataOutputStream(out)
        val body = Files.readAllBytes(File("./webapp$url").toPath())
        response200Header(dos, body.size)
        responseBody(dos, body)
    }

    private fun getContentLength(line: String?): Int {
        val headerTokens = line!!.split(":").toTypedArray()
        return headerTokens[1].trim { it <= ' ' }.toInt()
    }

    private fun response200Header(dos: DataOutputStream, lengthOfBodyContent: Int) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n")
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n")
            dos.writeBytes("Content-Length: $lengthOfBodyContent\r\n")
            dos.writeBytes("\r\n")
        } catch (e: IOException) {
            logger.error(e.message)
        }
    }

    private fun responseBody(dos: DataOutputStream, body: ByteArray) {
        try {
            dos.write(body, 0, body.size)
            dos.flush()
        } catch (e: IOException) {
            logger.error(e.message)
        }
    }

    companion object : KLogging()

}