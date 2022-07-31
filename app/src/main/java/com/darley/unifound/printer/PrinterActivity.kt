package com.darley.unifound.printer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
        if (schema == "file") {
            val file = uri.toFile()
            println("file: $file")
            println("file.absolutePath: ${file.absolutePath}")
            println("file.path: ${file.path}")
            println("file.name: ${file.name}")
//            val printJob = PrintJob(file)
//            PrintJobManager.addPrintJob(printJob)
        } else {

//            val printJob = PrintJob(File(filesDir, "output.pdf"))
//            val proj = arrayOf(MediaStore.Files.getContentUri("external").pathSegments[1])
            val cursor = contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                val fileName = cursor.getString(columnIndex)
                println("fileName: $fileName")
                val input = contentResolver.openInputStream(uri)
                val output = openFileOutput(fileName, MODE_PRIVATE)
                input?.copyTo(output)
                output.close()
                input?.close()
                val file = File(filesDir, fileName)
                val filePart = MultipartBody.Part.createFormData(
//                    "filename",
                    "szPath",
                    file.name,
                    RequestBody.create(type?.let { MediaType.parse(it) }, file)
                )
                println("type: $type")
//                val requestBodyMap: Map<String, RequestBody>
//                RequestBody.create(MediaType.parse(""), "-1")
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
                /*requestBodyMap["dwPaperId"] = dwPaperId
                requestBodyMap["dwDuplex"] = dwDuplex
                requestBodyMap["dwColor"] = dwColor
                requestBodyMap["dwFrom"] = dwFrom
                requestBodyMap["dwCopies"] = dwCopies
                requestBodyMap["BackURL"] = BackURL
                requestBodyMap["dwTo"] = dwTo*/

                val upFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
                query = UploadData(
                    filePart,
//                    ,
//                    requestBodyMap
//                    file,
//                    "-1", "1", "1", "0", "1", "", "0"
                    dwPaperId,
                    dwDuplex,
                    dwColor,
                    dwFrom,
                    dwCopies,
                    backURL,
                    dwTo
                )
                val viewModel by lazy {
                    ViewModelProvider(this)[PrinterViewModel::class.java]
                }
                /*viewModel.getSzToken()
                viewModel.szTokenLiveData.observe(this) {
                    val szToken = it.getOrNull()
                    if (szToken != null) {
                        viewModel.szToken = szToken
                    }
                    println("place: $szToken")
                }*/
                /*viewModel.login("21218247", "130952")
                viewModel.loginLiveData.observe(this) {
                    val login = it.getOrNull()
                    if (login != null) {
                        viewModel.szTrueName = login.result.szTrueName
                    }
                    println("place: $login")
                }*/
                viewModel.upload(query)
                viewModel.uploadResponseLiveData.observe(this) {
                    val upload = it.getOrNull()
//                    if (upload != null) {
//                        viewModel.
//                    }
                    println("place: $upload")
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
                println("file: $file")
                println("file.absolutePath: ${file.absolutePath}")
                println("file.path: ${file.path}")
                println("file.name: ${file.name}")

//            val printJob = PrintJob(uri.toUri())
//            PrintJobManager.addPrintJob(printJob)
            }
            cursor?.close()
        }


        setContent {
            PrinterTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Greeting("Android")
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
fun DefaultPreview() {
    PrinterTheme {
        Greeting("Android")
    }
}