package com.darley.unifound.printer.ui.printer

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.darley.unifound.printer.data.PlayState
import com.darley.unifound.printer.data.Repository
import com.darley.unifound.printer.data.model.LoginInfo
import com.darley.unifound.printer.data.model.UploadInfo
import com.darley.unifound.printer.data.network.UploadData
import com.darley.unifound.printer.data.network.UploadResponse
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class PrinterViewModel : ViewModel() {

    val playState = mutableStateOf<PlayState?>(null)
    var isParsing = MutableLiveData(false)
    var isUploading = mutableStateOf(false)
    var uploadResultInfo = mutableStateOf("")
    var uploadSuccess = MutableLiveData(-1)
    private val uploadInfo = mutableStateOf(UploadInfo())

    var uploadFile = mutableStateOf<File?>(null)
    private var _uploadFileType: String? = null

    fun hasLoginInfo() = Repository.hasLoginInfo()
    fun saveLoginInfo(loginInfo: LoginInfo) = Repository.saveLoginInfo(loginInfo)
    fun getLoginInfo() = Repository.getLoginInfo()
    fun saveDataPermission() = Repository.saveDataPermission()
    fun hasDataPermission() = Repository.hasDataPermission()


    private val _uploadLiveData = MutableLiveData<UploadData>()
    private var _uploadResponseLiveData = MutableLiveData<Result<UploadResponse>>()
    val uploadResponseLiveData: LiveData<Result<UploadResponse>>
        get() = _uploadResponseLiveData

    fun upload() {
        _uploadResponseLiveData = _uploadLiveData.value!!.let {
            Repository.upload(
                it.file,
                it.dwPaperId,
                it.dwDuplex,
                it.dwColor,
                it.dwFrom,
                it.dwCopies,
                it.BackURL,
                it.dwTo
            )
        } as MutableLiveData<Result<UploadResponse>>
    }

    fun setUploadInfo(name: String, value: String) {
        uploadInfo.value[name] = value
    }

    fun setFile(file: File?) {
        uploadFile.value = file
    }

    fun setFileType(type: String?) {
        _uploadFileType = type
    }

    fun setUploadData() {
        /*val requestBodyMap: Map<String, RequestBody> = mapOf(
            "dwPaperId" to dwPaperId,
            "dwDuplex" to dwDuplex,
            "dwColor" to dwColor,
            "dwFrom" to dwFrom,
            "dwCopies" to dwCopies,
            "BackURL" to backURL,
            "dwTo" to dwTo
        )*/

        _uploadLiveData.value = UploadData(
            file = MultipartBody.Part.createFormData(
                "szPath",
                uploadFile.value!!.name,
                RequestBody.create(MediaType.parse(_uploadFileType ?: ""), uploadFile.value!!)
            ),
            dwPaperId = RequestBody.create(null, uploadInfo.value.paperId),
            dwDuplex = RequestBody.create(null, uploadInfo.value.duplex),
            dwColor = RequestBody.create(null, uploadInfo.value.color),
            dwFrom = RequestBody.create(null, uploadInfo.value.from),
            dwCopies = RequestBody.create(null, uploadInfo.value.copies),
            BackURL = RequestBody.create(null, uploadInfo.value.backUrl),
            dwTo = RequestBody.create(null, uploadInfo.value.to)
        )
    }
}
