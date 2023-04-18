package com.darley.unifound.printer.data.network.model

data class LoginRes(
    val code: Int,
    val message: String,
    val result: Result?,
) {
    data class Result(
        val szLogonName: String,
        val szTrueName: String,
    )
}
