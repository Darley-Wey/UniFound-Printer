package com.darley.unifound.printer.ui.login

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.darley.unifound.printer.R
import com.darley.unifound.printer.data.Repository
import com.darley.unifound.printer.data.model.LoggedInUser
import com.darley.unifound.printer.data.model.LoginInfo
import com.darley.unifound.printer.data.network.LoginResponse

class LoginViewModel : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult
    private val loginInfoLiveData = MutableLiveData<LoginInfo>()

    fun saveUser(user: LoggedInUser) = Repository.saveUser(user)
    fun getSavedUser() = Repository.getSavedUser()
    fun isUserSaved() = Repository.isUserSaved()

    //    private val loginResult = MutableLiveData<LoginResponse>()
    val loginLiveData = Transformations.switchMap(loginInfoLiveData) {
        Repository.login(it.username, it.password)
//        loginRepository.login(it.username, it.password)
    }

    // value变化时触发对应的事件
    fun login(username: String, password: String) {
        loginInfoLiveData.value = LoginInfo(username, password)
    }

    fun loginResult(loginResponse: LoginResponse) {
        // can be launched in a separate asynchronous job
        val code = loginResponse.code
        val result = loginResponse.result
        if (code == 0) {
            _loginResult.value =
                LoginResult(success = LoggedInUserView(displayName = result.szTrueName))
        } else {
            _loginResult.value = LoginResult(error = "登陆失败")
        }
    }


    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}