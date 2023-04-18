package com.darley.unifound.printer.data.network.model

import okhttp3.MultipartBody
import okhttp3.RequestBody

data class UploadData(
    val file: MultipartBody.Part,
    val dwPaperId: RequestBody,
    val dwDuplex: RequestBody,
    val dwColor: RequestBody,
    val dwFrom: RequestBody,
    val dwCopies: RequestBody,
    val BackURL: RequestBody,
    val dwTo: RequestBody,
)
