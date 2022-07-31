package com.darley.unifound.printer.data.model

/*{
    "szAuthType": "password, qr",
    "szInfo": null,
    "szToken": "8b13d09245184bcab9421c4d051e36bd",
    "szStaSessionId": null,
    "code": 0,
    "message": "",
    "result": null
}*/
data class AuthTokenResponse(
    val szAuthType: String,
    val szInfo: Any,
    val szToken: String,
    val szStaSessionId: Any,
    val code: Int,
    val message: String,
    val result: Any
)


