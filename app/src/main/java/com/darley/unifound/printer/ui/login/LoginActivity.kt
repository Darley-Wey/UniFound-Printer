package com.darley.unifound.printer.ui.login

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.darley.unifound.printer.APP
import com.darley.unifound.printer.R
import com.darley.unifound.printer.data.model.LoggedInUser
import com.darley.unifound.printer.databinding.ActivityLoginBinding
import com.darley.unifound.printer.ui.printer.PrinterActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding


    /*@Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int, resultCode: Int, resultData: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == 0
            && resultCode == Activity.RESULT_OK
        ) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            resultData?.also {
                contentResolver.takePersistableUriPermission(
                    it.data!!, Intent.FLAG_GRANT_READ_URI_PERMISSION
                            or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                // Perform operations on the document using its URI.
            }
        }
    }*/


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
                    Toast.makeText(this, "请手动开启存储权限，否则无法使用", Toast.LENGTH_SHORT).show()
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
        val dialog = android.app.AlertDialog.Builder(this)
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
//      检查存储权限
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 0)
        }

        /*val webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.loadUrl("http://10.135.0.139:9130/client/new/cprintMobile/login.html")
        setContentView(webView)*/

        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        if (loginViewModel.hasLoginInfo() && loginViewModel.isUserSaved()) {
            updateUiWithUser(loginViewModel.getSavedUser()!!)
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val username = binding.username
        val password = binding.password
        val login = binding.login
        val loading = binding.loading

        //        loginViewModel =
        //            ViewModelProvider(this, LoginViewModelFactory())[LoginViewModel::class.java]


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

        // 实时监听登录结果变化
        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer
            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
            }
            setResult(Activity.RESULT_OK)
            //Complete and destroy login activity once successful
//            finish()
        })

        // 监听登录网络请求
        loginViewModel.loginLiveData.observe(this@LoginActivity) {
            if (it.isSuccess) {
                it.getOrNull()
                    ?.let { it1 -> loginViewModel.loginResult(it1) }
//                        ?.let { it2 -> updateUiWithUser(it2) }
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

    private fun updateUiWithUser(model: LoggedInUser) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
        PrinterActivity.actionStart(this, "login")
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