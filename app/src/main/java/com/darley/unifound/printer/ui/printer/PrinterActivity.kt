package com.darley.unifound.printer.ui.printer

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toFile
import androidx.lifecycle.ViewModelProvider
import com.darley.unifound.printer.data.network.UploadData
import com.darley.unifound.printer.ui.printer.PrinterViewModel
import com.darley.unifound.printer.ui.theme.PrinterTheme
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


class PrinterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val action = intent.action
        val type = intent.type
        Log.d("PrinterActivity", "action: $action, type: $type")
        val uri: Uri = if (action == Intent.ACTION_VIEW) {
            intent.data!!
        } else {
            intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)!!
        }
//        val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        val schema = uri.scheme
        val query: UploadData
        val file: File
        if (schema == "file") {
            file = uri.toFile()
        } else {
            val cursor = contentResolver.query(uri, null, null, null, null)!!
//            if (cursor != null && cursor.moveToFirst()) {
            cursor.moveToFirst()
            val columnIndex = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
            val fileName = cursor.getString(columnIndex)
            val input = contentResolver.openInputStream(uri)
            val output = openFileOutput(fileName, MODE_PRIVATE)
            input?.copyTo(output)
            output.close()
            input?.close()
            file = File(filesDir, fileName)
            cursor.close()
        }
//                println("filePath: $filePath")
//                val file = File(filePath)
//                println("file: $file")
//                println("file.absolutePath: ${file.absolutePath}")
//                println("file.path: ${file.path}")
//                println("file.name: ${file.name}")}
//            val pfd = contentResolver.openFileDescriptor(uri, "r")
//            val fd = pfd?.fileDescriptor
//            val file = pfd.toString().toUri().toFile()
//            val uriString = uri.toString()
//            println("uriString: $uriString")
//            val path = uriString.substringAfter("://")
//            println("path: $path")
//            val file = File(path)


        val filePart = MultipartBody.Part.createFormData(
            "szPath", file.name,
            RequestBody.create(type?.let { MediaType.parse(it) }, file)
        )
        val dwPaperId: RequestBody = RequestBody.create(MediaType.parse(""), "-1")
        val dwDuplex: RequestBody = RequestBody.create(MediaType.parse(""), "1")
        val dwColor: RequestBody = RequestBody.create(MediaType.parse(""), "1")
        val dwFrom: RequestBody = RequestBody.create(MediaType.parse(""), "0")
        val dwCopies: RequestBody = RequestBody.create(MediaType.parse(""), "1")
        val backURL: RequestBody = RequestBody.create(MediaType.parse(""), "")
        val dwTo: RequestBody = RequestBody.create(MediaType.parse(""), "0")
        val requestBodyMap: Map<String, RequestBody> = mapOf(
            "dwPaperId" to dwPaperId,
            "dwDuplex" to dwDuplex,
            "dwColor" to dwColor,
            "dwFrom" to dwFrom,
            "dwCopies" to dwCopies,
            "BackURL" to backURL,
            "dwTo" to dwTo
        )

        query = UploadData(
            filePart, dwPaperId, dwDuplex, dwColor, dwFrom, dwCopies, backURL, dwTo
        )
        val viewModel by lazy {
            ViewModelProvider(this)[PrinterViewModel::class.java]
        }
        if (viewModel.hasLoginInfo()) {
            viewModel.upload(query)
            viewModel.uploadResponseLiveData.observe(this)
            {
                if (it.isSuccess) {
                    viewModel.uploadSuccess = true
                    Toast.makeText(
                        applicationContext,
                        "上传成功",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Log.d("PrinterActivity", "upload failed")
                }
                val upload = it.getOrNull()
//                    if (upload != null) {
//                        viewModel.
//                    }
                println("place: $upload")
            }
        }

        setContent {
            PrinterTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
//                    Greeting("Android")
                    Dialog(!viewModel.hasLoginInfo(), "未登录")
//                    Dialog(viewModel.uploadSuccess, "上传成功")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Composable
fun Dialog(state: Boolean, title: String) {
    val openDialog = remember { mutableStateOf(state) }
    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = {
                // Dismiss the dialog when the user clicks outside the dialog or on the back
                // button. If you want to disable that functionality, simply use an empty
                // onCloseRequest.
                openDialog.value = false
            },
            title = {
                Text(text = title)
            },
            /*text = {
                Text(
                    "This area typically contains the supportive text " +
                            "which presents the details regarding the Dialog's purpose."
                )
            },*/
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog.value = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            /*dismissButton = {
                TextButton(
                    onClick = {
                        openDialog.value = false
                    }
                ) {
                    Text("Dismiss")
                }
            }*/
        )
    }
}

@Composable
fun Main() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Hello World!")
    }

}

@Composable
fun checks() {
// We have two radio buttons and only one can be selected
    var state by remember { mutableStateOf(true) }
// Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
    Row(Modifier.selectableGroup()) {
        RadioButton(
            selected = state,
            onClick = { state = true }
        )
        RadioButton(
            selected = !state,
            onClick = { state = false }
        )
    }
}

@Composable
fun checks2() {
    val radioOptions = listOf("Calls", "Missed", "Friends")
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }
// Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
    Row(Modifier.selectableGroup()) {
        Text(
//            modifier = Modifier.height(56.dp),
            "纸型：",
//            lineHeight = 56.dp,
            style = MaterialTheme.typography.body1.merge(),
            modifier = Modifier
                .padding(start = 16.dp, top = 16.dp),


//                .height(56.dp)
        )
        radioOptions.forEach { text ->
            Row(
                Modifier
//                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (text == selectedOption),
                        onClick = { onOptionSelected(text) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (text == selectedOption),
                    onClick = null // null recommended for accessibility with screenreaders
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.body1.merge(),
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
    }
}


@Composable
fun Pages() {
    @Composable
    fun Page(start: String, end: String) {
        val s = remember { mutableStateOf(start) }
        val e = remember { mutableStateOf(start) }
        Row(
            Modifier
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = s.value,
                onValueChange = { s.value = it },
            )
            Text(
                text = " ~ ",
                style = MaterialTheme.typography.body1.merge(),
                modifier = Modifier.padding(start = 6.dp)
            )
            OutlinedTextField(
                value = e.value,
                onValueChange = { e.value = it },
            )
            Text(
                text = "页",
                style = MaterialTheme.typography.body1.merge(),
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
//    val radioOptions = listOf("全部", "部分")
    var isAllin by remember { mutableStateOf(true) }
    var start by remember { mutableStateOf(1) }
    var end by remember { mutableStateOf(1) }
//    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }
// Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
    Row(Modifier.selectableGroup()) {
        Text(
//            modifier = Modifier.height(56.dp),
            "页数：",
//            lineHeight = 56.dp,
            style = MaterialTheme.typography.body1.merge(),
            modifier = Modifier
                .padding(start = 16.dp, top = 16.dp),
        )
        Row(
            Modifier
                .height(56.dp)
                /*.selectable(
                    selected = (isAllin == selectedOption),
                    onClick = { onOptionSelected(isAllin) },
                    role = Role.RadioButton
                )*/
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = (isAllin),
                onClick = { isAllin = true }
            )
            Text(
                text = "全部",
                style = MaterialTheme.typography.body1.merge(),
                modifier = Modifier.padding(start = 6.dp)
            )
            RadioButton(
                selected = (!isAllin),
                onClick = { isAllin = false }
            )
            Text(
                text = "部分",
                style = MaterialTheme.typography.body1.merge(),
                modifier = Modifier.padding(start = 6.dp)
            )
            Page("1", "1")
        }
    }
}


@Composable
fun Counter() {
    var count by remember { mutableStateOf(1) }

    Row(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "份数：",
            modifier = Modifier.size(45.dp, 25.dp)
        )
        OutlinedButton(
            onClick = { if (count > 1) count-- else count = 1 },
            // count最小为1
//            enabled = count > 1,
            // 调整按钮的大小
            modifier = Modifier.size(25.dp, 25.dp)
        ) {
            Text(
                text = "-",
                modifier = Modifier.size(25.dp, 25.dp)
            )
        }
        BasicTextField(
//            text = "$count",
            value = count.toString(),
            onValueChange = { count = it.toInt() },
            modifier = Modifier.size(35.dp, 25.dp)
        )
        OutlinedButton(
            onClick = { count++ },
            modifier = Modifier.size(25.dp, 25.dp)
        ) {
            /*Icon(
                Icons.Filled.Favorite,
                contentDescription = null,

            )*/

//            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("+")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PrinterTheme {
//        checks2()
//        Greeting("Android")
        Counter()
    }
}