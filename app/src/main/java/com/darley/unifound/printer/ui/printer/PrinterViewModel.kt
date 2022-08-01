package com.darley.unifound.printer.ui.printer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.darley.unifound.printer.data.Repository
import com.darley.unifound.printer.data.dao.LoginInfoDao
import com.darley.unifound.printer.data.model.LoginInfo
import com.darley.unifound.printer.data.network.UploadData

class PrinterViewModel : ViewModel() {

    fun hasLoginInfo() = Repository.hasLoginInfo()
    fun saveLoginInfo(loginInfo: LoginInfo) = Repository.saveLoginInfo(loginInfo)
    fun getLoginInfo() = Repository.getLoginInfo()


    var uploadSuccess = false

    private val uploadLiveData = MutableLiveData<UploadData>()
    val uploadResponseLiveData = Transformations.switchMap(uploadLiveData) {
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

    fun upload(data: UploadData) {
        uploadLiveData.value = data
    }
}