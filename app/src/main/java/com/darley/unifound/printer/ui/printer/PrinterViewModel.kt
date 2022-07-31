package com.darley.unifound.printer.ui.printer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.darley.unifound.printer.data.Repository
import com.darley.unifound.printer.data.network.UploadData

class PrinterViewModel : ViewModel() {

    /*var szToken = ""
    val szTokenLiveData = Transformations.switchMap(searchLiveData) {
        Repository.getSzToken()
    }
    private val loginUserLiveData = MutableLiveData<LogInUser>()
    var szTrueName = ""
    val loginResponseLiveData = Transformations.switchMap(loginUserLiveData) {
        Repository.login(it.username, it.password)
    }
    fun login(username: String, password: String) {
        loginUserLiveData.value = LogInUser(username, password)
    }
    */
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