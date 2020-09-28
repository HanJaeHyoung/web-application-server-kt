package webserver

import model.HttpRequest
import model.HttpResponse

interface ServiceHandler {
    fun handle(request: HttpRequest): HttpResponse

    companion object{
        fun getContentType(url: String): String {
            return when {
                url.contains("/js/") -> "application/javascript"
                url.contains("/images/") -> "image/jpeg"
                url.contains("/css/") -> "text/css"
                else -> "text/html"
            }
        }
    }
}