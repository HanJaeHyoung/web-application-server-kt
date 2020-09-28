package model

data class HttpResponse(
    val status: ResponseCode,
    val contentType: String = "text/html",
    val cookies: Map<String, String> = mutableMapOf(),
    val headers: Map<String, String> = mutableMapOf(),
    val redirectURL: String = "",
    val body: ByteArray = ByteArray(0)

)
enum class ResponseCode(val code: Int) {
    OK(200),
    Created(201),
    Redirect(302)
}