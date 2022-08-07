package com.darley.unifound.printer.data.model

data class UploadInfo(
    var paperId: String = "-1",
    var color: String = "1",
    var duplex: String = "1",
    var from: String = "1",
    var to: String = "1",
    var copies: String = "1",
    var backUrl: String = "",
) {
    operator fun set(name: String, value: String) {
        when (name) {
            "paperId" -> paperId = value
            "color" -> color = value
            "duplex" -> duplex = value
            "from" -> from = value
            "to" -> to = value
            "copies" -> copies = value
        }
    }
}
