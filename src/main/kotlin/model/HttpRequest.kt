package model

data class HttpRequest(
    val method: HttpMethod = HttpMethod.GET,
    val uri: String = "",
    val headers: Map<String, String> = mutableMapOf(),
    val body: String? = "",
    val cookies: Map<String, String>? = mutableMapOf(),
    var queryStrings: Map<String, String>? = mutableMapOf()
)

enum class HttpMethod {
    GET,
    POST,
    DELETE
}