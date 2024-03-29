package com.darley.unifound.printer.ui.login

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.darley.unifound.printer.databinding.ActivityLoginBinding
import com.darley.unifound.printer.ui.WebViewActivity
import com.darley.unifound.printer.utils.ActivityCollector
import com.darley.unifound.printer.utils.BaseActivity

class LoginActivity : BaseActivity() {


    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    //  开启存储权限
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "请手动开启存储权限，否则部分文件无法上传", Toast.LENGTH_LONG).show()
                    val b = shouldShowRequestPermissionRationale(permissions[0])
                    if (!b) {
                        showDialogTipUserGoToAppSetting()
                    } else {
                        finish()
                    }
                } else {
                    Toast.makeText(this, "存储权限获取成功", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDialogTipUserGoToAppSetting() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("存储权限未开启")
        dialog.setMessage("请在-应用设置-权限-中，允许使用存储权限来保存您的数据")
        dialog.setPositiveButton("立即开启") { _, _ ->
            val intent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 检查存储权限
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }


        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val username = binding.username
        val password = binding.password
        val login = binding.login
        val loading = binding.loading

        // 实时监听登录结果变化
        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer
            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                //Complete and destroy login activity once successful
                updateUi(loginResult.success)
            }
            setResult(RESULT_OK)
        })

        if (loginViewModel.hasLoginInfo()) {
            val loginInfo = loginViewModel.getLoginInfo()!!
            loginViewModel.login(loginInfo.username, loginInfo.password)
        } else {
            setContentView(binding.root)
        }


        // 监听输入框的变化，实时错误检查
        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer
            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid
            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })


        // 监听登录网络请求
        loginViewModel.loginLiveData.observe(this@LoginActivity) {
            if (it.isSuccess) {
                it.getOrNull()
                    ?.let { it1 -> loginViewModel.loginResult(it1) }
            } else {
                loading.visibility = View.GONE
                showLoginFailed("登陆失败，请检查输入或网络连接")
            }
        }

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        loading.visibility = View.VISIBLE
                        loginViewModel.login(
                            username.text.toString(),
                            password.text.toString()
                        )
                    }
                }
                false
            }
        }

        login.setOnClickListener {
            loading.visibility = View.VISIBLE
            loginViewModel.login(username.text.toString(), password.text.toString())
        }
    }

    override fun onBackPressed() {
        ActivityCollector.finishAll()
    }

    private fun updateUi(result: String) {
        Toast.makeText(
            applicationContext,
            result,
            Toast.LENGTH_LONG
        ).show()
        WebViewActivity.actionStart(this, WebViewActivity.INDEX_URL)
        finish()
    }

    private fun showLoginFailed(errorString: String) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_LONG).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}