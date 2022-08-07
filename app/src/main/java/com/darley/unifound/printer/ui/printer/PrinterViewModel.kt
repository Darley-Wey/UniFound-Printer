package com.darley.unifound.printer.ui.printer

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.darley.unifound.printer.data.PlayState
import com.darley.unifound.printer.data.Repository
import com.darley.unifound.printer.data.dao.LoginInfoDao
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
    var isUploading = mutableStateOf(false)
    var uploadResultInfo = mutableStateOf("")
    var uploadSuccess = MutableLiveData(-1)
    val uploadInfo = mutableStateOf(UploadInfo())

    var uploadFile = mutableStateOf<File?>(null)
    private var _fileType: String? = null
    val fileType: String?
        get() = _fileType

    fun hasLoginInfo() = Repository.hasLoginInfo()
    fun saveLoginInfo(loginInfo: LoginInfo) = Repository.saveLoginInfo(loginInfo)
    fun getLoginInfo() = Repository.getLoginInfo()


    private val _uploadLiveData = MutableLiveData<UploadData>()
    /*val uploadLiveData: LiveData<UploadData>
        get() = _uploadLiveData*/

    /* var uploadResponseLiveData = Transformations.switchMap(_uploadLiveData) {
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
     }*/

    fun upload(): LiveData<Result<UploadResponse>> {
//        _uploadLiveData.value = data
        return _uploadLiveData.value!!.let {
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
        }
    }

    fun setUploadInfo(name: String, value: String) {
        uploadInfo.value[name] = value
    }

    fun setFile(file: File?) {
        uploadFile.value = file
    }

    fun setFileType(type: String?) {
        _fileType = type
    }

    fun setUploadData(file: File?, type: String?, uploadInfo: UploadInfo) {
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
                file!!.name,
                RequestBody.create(MediaType.parse(type ?: ""), file)
            ),
            dwPaperId = RequestBody.create(null, uploadInfo.paperId),
            dwDuplex = RequestBody.create(null, uploadInfo.duplex),
            dwColor = RequestBody.create(null, uploadInfo.color),
            dwFrom = RequestBody.create(null, uploadInfo.from),
            dwCopies = RequestBody.create(null, uploadInfo.copies),
            BackURL = RequestBody.create(null, uploadInfo.backUrl),
            dwTo = RequestBody.create(null, uploadInfo.to)
        )
    }
}
