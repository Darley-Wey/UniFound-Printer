package com.darley.unifound.printer.data.model

data class LoginResponse(
    val code: Int,
    val result: Result
) {
    data class Result(
        val szLogonName: String
    )
}