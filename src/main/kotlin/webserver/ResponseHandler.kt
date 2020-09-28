package webserver

import model.HttpResponse
import mu.KLogging
import java.io.DataOutputStream
import java.io.IOException

class ResponseHandler(private val dos: DataOutputStream, private var response: HttpResponse) {
    fun responseHeader() {
        try {
            dos.writeBytes("${RequestHandler.HTTP} ${response.status.code} ${response.status}\r\n")
            response.headers.forEach {
                dos.writeBytes("${it.key}: ${it.value}\r\n")
            }
            if (response.cookies.isNotEmpty()) {
                dos.writeBytes("Set-Cookie: ")
                response.cookies.forEach {
                    dos.writeBytes("${it.key}=${it.value} ")
                }
                dos.writeBytes("\r\n")
            }
            if (response.status.code.toString().startsWith("3")) {
                dos.writeBytes("Location: ${response.redirectURL}\r\n")
            }
            if (response.body.isNotEmpty()) {
                dos.writeBytes("Content-Length: ${response.body.size}\r\n")
            }
            dos.writeBytes("\r\n")
        } catch (e: IOException) {
            RequestHandler.logger.error(e.message)
        }
    }


    fun responseBody() {
        try {
            dos.write(response.body, 0, response.body.size)
            dos.flush()
        } catch (e: IOException) {
            RequestHandler.logger.error(e.message)
        }
        RequestHandler.logger.info("responseBody end")
    }

    companion object : KLogging()

}