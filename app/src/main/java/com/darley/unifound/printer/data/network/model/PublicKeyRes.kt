package com.darley.unifound.printer.data.network.model

data class PublicKeyRes(
    val code: Int,
    val message: String,
    val result: Result,
) {
    data class Result(
        val publicKey: String,
        val nonceStr: String,
    )
}
