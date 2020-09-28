package webserver

import db.DataBase
import model.HttpRequest
import model.HttpResponse
import model.ResponseCode
import model.User
import mu.KLogging
import webserver.ServiceHandler.Companion.getContentType
import java.io.File
import java.lang.StringBuilder
import java.nio.file.Files

class UserServiceHandler : ServiceHandler {
    lateinit var response: HttpResponse
    override fun handle(request: HttpRequest): HttpResponse {
        when (request.uri) {
            USER_CREATE -> {
                userCreate(request)
            }
            USER_LOGIN -> {
                userLogin(request)
            }
            USER_LIST -> {
                getUserList(request)
            }
            USER_LOGOUT -> {
                response = HttpResponse(
                    status = ResponseCode.Redirect,
                    redirectURL = "$INDEX.html",
                    cookies = mapOf("logined" to "false")
                )
            }
            else -> {
                response = getUriResponse(request.uri!!)
            }
        }
        return response
    }

    private fun getUriResponse(url: String): HttpResponse {
        val body = Files.readAllBytes(File("./webapp/$url").toPath())
        val contentType = getContentType(url)
        val headers = mapOf("Content-Type" to "$contentType;charset=utf-8")
        return HttpResponse(status = ResponseCode.OK, headers = headers, body = body)
    }

    private fun userLogin(request: HttpRequest) {
        val user = DataBase.findUserById(request.queryStrings?.get("userId"))
        response = if (user == null) {
            HttpResponse(status = ResponseCode.Redirect, redirectURL = "$USER_LOGIN_FAILED.html")
        } else {
            if (user.password.equals(request.queryStrings?.get("password"))) {
                HttpResponse(
                    status = ResponseCode.Redirect,
                    redirectURL = "$INDEX.html",
                    cookies = mapOf("logined" to "true")
                )
            } else {
                getUriResponse("$USER_LOGIN_FAILED.html")
            }
        }
    }

    private fun getUserList(request: HttpRequest) {
        if (request.cookies?.get("logined") == "true") {
            val users = DataBase.findAll()
            val htmlCode = StringBuilder()
            val result = StringBuilder()
            users.forEachIndexed { idx, user ->
                htmlCode.append(
                    """<tr> 
                                <th scope="row">${idx + 1}</th> <td>${user.userId}</td> 
                                <td>${user.name}</td> <td>${user.email!!.replace("%40", "@")}</td>
                                <td><a href="#" class="btn btn-success" role="button">수정</a></td>
                            </tr>""".trimIndent()
                )
            }
            Files.readAllLines(File("./webapp/$USER_LIST.html").toPath()).forEach {
                result.append(
                    it.replace(
                        "<tbody id=\"userInfo\"></tbody>",
                        "<tbody id=\"userInfo\">${htmlCode}</tbody>"
                    )
                )
            }
            response = HttpResponse(status = ResponseCode.OK, body = result.toString().toByteArray())
        } else {
            response = getUriResponse("$USER_LOGIN.html")
        }
    }

    private fun userCreate(request: HttpRequest) {
        if (!request.queryStrings.isNullOrEmpty()) {
            val user =
                User(
                    request.queryStrings!!["userId"],
                    request.queryStrings!!["password"],
                    request.queryStrings!!["name"],
                    request.queryStrings!!["email"]
                )
            DataBase.addUser(user)
            response = HttpResponse(status = ResponseCode.Redirect, redirectURL = "$INDEX.html")
        }
    }

    companion object : KLogging() {
        const val USER_LOGIN_FAILED = "/user/login_failed"
        const val USER_LOGIN = "/user/login"
        const val USER_LIST = "/user/list"
        const val USER_CREATE = "/user/create"
        const val USER_LOGOUT = "/user/logout"
        const val INDEX = "/index"
    }
}