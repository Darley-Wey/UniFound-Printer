package com.darley.unifound.printer.data.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class UploadResponse(
    val code: Int,
    val message: String,
    val result: Result?,
) {
    data class Result(
        val szJobName: String,
    )
}

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


interface UploadService {
    @Multipart
    @POST("/api/client/CloudPrint/Upload")
    fun upload(
        @Part file: MultipartBody.Part,
//        @PartMap map: Map<String, RequestBody>
        @Part("dwPaperId") dwPaperId: RequestBody,
        @Part("dwDuplex") dwDuplex: RequestBody,
        @Part("dwColor") dwColor: RequestBody,
        @Part("dwFrom") dwFrom: RequestBody,
        @Part("dwCopies") dwCopies: RequestBody,
        @Part("BackURL") BackURL: RequestBody,
        @Part("dwTo") dwTo: RequestBody
    ): Call<UploadResponse>
}

