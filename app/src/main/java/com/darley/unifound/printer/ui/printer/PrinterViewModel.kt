package com.darley.unifound.printer.ui.printer

import androidx.compose.ui.input.key.Key.Companion.Search
import androidx.compose.ui.text.input.ImeAction.Companion.Search
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.darley.unifound.printer.data.Repository
import com.darley.unifound.printer.data.network.UploadData
import com.darley.unifound.printer.data.network.User

class PrinterViewModel : ViewModel() {
    private val searchLiveData = MutableLiveData<String>()

    /*var szToken = ""
    val szTokenLiveData = Transformations.switchMap(searchLiveData) {
        Repository.getSzToken()
    }

    fun getSzToken() {
        searchLiveData.value = ""
    }*/
    private val userLiveData = MutableLiveData<User>()
    private val uploadLiveData = MutableLiveData<UploadData>()
    var szTrueName = ""
    val loginLiveData = Transformations.switchMap(userLiveData) {
        Repository.login(it.username, it.password)
    }
    val uploadResponseLiveData = Transformations.switchMap(uploadLiveData) {
        Repository.upload(
            it.file,
//            it.map
//            /*it.file,
            it.dwPaperId,
            it.dwDuplex,
            it.dwColor,
            it.dwFrom,
            it.dwCopies,
            it.BackURL,
            it.dwTo
        )
    }

    fun login(username: String, password: String) {
        userLiveData.value = User(username, password)
    }

    fun upload(data: UploadData) {
        uploadLiveData.value = data
    }
}