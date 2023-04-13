package com.darley.unifound.printer.data.network.model

data class CheckRes(
    val code: Int,
    val message: String,
    val result: Result?,
) {
    data class Result(
        val szTrueName: String,
    )
}
