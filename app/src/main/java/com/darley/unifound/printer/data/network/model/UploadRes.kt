package com.darley.unifound.printer.data.network.model

data class UploadRes(
    val code: Int,
    val message: String,
    val result: Result?,
) {
    data class Result(
        val szJobName: String,
    )
}
