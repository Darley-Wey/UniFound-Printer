package com.darley.unifound.printer.ui.printer

import android.content.Intent
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toFile
import androidx.lifecycle.viewmodel.compose.viewModel
import com.darley.unifound.printer.ui.printer.ui.theme.PrinterTheme
import com.darley.unifound.printer.utils.FileUtil
import java.io.File

class ReceiverActivity : ComponentActivity() {

    private fun parseIntent(intent: Intent) {
        val viewModel by viewModels<PrinterViewModel>()
        viewModel.isParsing.value = true
        val action = intent.action!!
        val type = intent.type!!
        Log.d("ReceiverActivity", "action: $action, type: $type")
        val uri =
            if (action == Intent.ACTION_VIEW) intent.data!! else intent.getParcelableExtra(Intent.EXTRA_STREAM)!!
        val schema = uri.scheme!!
        val file: File? = if (schema == "file"
        ) uri.toFile() else {
            // schema == "content"
            FileUtil.getFile(uri)
        }
        viewModel.setFile(file)
        viewModel.setFileType(type)
        viewModel.isParsing.value = false
        Log.d("ReceiverActivity", "file: ${viewModel.uploadFile.value?.name}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseIntent(intent)

        setContent {
            PrinterTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background) {
                    val viewModel: PrinterViewModel = viewModel()
                    val isParsing by viewModel.isParsing.observeAsState()
                    if (isParsing == true) {
                        Loading(state = "处理中")
                    } else {
                        PrinterActivity.actionStart(this@ReceiverActivity, "receiver")
//                        finish()
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview2() {
    PrinterTheme {
        Greeting("Android")
    }
}