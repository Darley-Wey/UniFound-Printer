package com.darley.unifound.printer.data.network

import com.darley.unifound.printer.data.network.model.UploadRes
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


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
    ): Call<UploadRes>
}

