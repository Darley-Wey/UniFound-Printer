package com.darley.unifound.printer.ui.printer

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.darley.unifound.printer.data.Repository
import com.darley.unifound.printer.data.model.UploadInfo
import com.darley.unifound.printer.data.network.model.UploadData
import com.darley.unifound.printer.data.network.model.UploadRes
import me.jessyan.progressmanager.ProgressListener
import me.jessyan.progressmanager.body.ProgressInfo
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class UploadViewModel : ViewModel() {
    private var mLastPreloadingInfo: ProgressInfo? = null
    var isParsing = MutableLiveData(true)
    var isUploading = mutableStateOf(false)
    var uploadProgress by mutableStateOf(0.0f)
    var uploadState by mutableStateOf("上传中，0%")
    var uploadFile = mutableStateOf<File?>(null)
    var isWrongFileType by mutableStateOf(false)
    private var uploadFileType: String? = null
    private val uploadInfo = mutableStateOf(UploadInfo())

    fun hasLoginInfo() = Repository.hasLoginInfo()


    private val _uploadLiveData = MutableLiveData<UploadData>()
    private var _uploadResponseLiveData = MutableLiveData<Result<UploadRes>>()
    val uploadResponseLiveData: LiveData<Result<UploadRes>>
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
        } as MutableLiveData<Result<UploadRes>>
    }

    fun setUploadInfo(name: String, value: String) {
        uploadInfo.value[name] = value
    }

    fun setFile(file: File?) {
        uploadFile.value = file
    }

    fun setFileType(type: String?) {
        uploadFileType = type
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
                RequestBody.create(MediaType.parse(uploadFileType ?: ""), uploadFile.value!!)
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

    fun getUploadListener(): ProgressListener {
        return object : ProgressListener {
            override fun onProgress(progressInfo: ProgressInfo) {
                // 如果你不屏蔽用户重复点击上传或下载按钮,就可能存在同一个 Url 地址,上一次的上传或下载操作都还没结束,
                // 又开始了新的上传或下载操作,那现在就需要用到 id(请求开始时的时间) 来区分正在执行的进度信息
                // 这里我就取最新的上传进度用来展示,顺便展示下 id 的用法
                if (mLastPreloadingInfo == null) {
                    mLastPreloadingInfo = progressInfo
                }

                //因为是以请求开始时的时间作为 Id ,所以值越大,说明该请求越新
                if (progressInfo.id < mLastPreloadingInfo!!.id) {
                    return
                } else if (progressInfo.id > mLastPreloadingInfo!!.id) {
                    mLastPreloadingInfo = progressInfo
                }
                uploadProgress = mLastPreloadingInfo!!.percent / 100f
                uploadState = "上传中，" + mLastPreloadingInfo!!.percent + "%"
//                mUploadProgress.setProgress(progress)
//                mUploadProgressText.setText("$progress%")
                Log.d(
                    "UploadActivity",
                    "--Upload-- " + uploadProgress * 100 + " %  " + mLastPreloadingInfo!!.speed / 1000000 + " MB/s  " + mLastPreloadingInfo.toString()
                )
                if (mLastPreloadingInfo!!.isFinish) {
                    //说明已经上传完成
                    Log.d("UploadActivity", "--Upload-- finish")
                    uploadState = "上传完成，等待服务端响应"
                }
            }

            override fun onError(id: Long, e: Exception?) {
//                mHandler?.post(Runnable {
//                    mUploadProgress.setProgress(0)
//                    mUploadProgressText.setText("error")
//                })
            }
        }
    }
}
