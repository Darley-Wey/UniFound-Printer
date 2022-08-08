package com.darley.unifound.printer.ui.printer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ArrowBackIos
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toFile
import androidx.core.text.isDigitsOnly
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewmodel.compose.viewModel
import com.darley.unifound.printer.APP.Companion.context
import com.darley.unifound.printer.R
import com.darley.unifound.printer.ui.theme.PrinterTheme
import com.darley.unifound.printer.utils.FileUtil
import kotlinx.coroutines.launch
import java.io.File


// Compose Activity 使用 ComponentActivity 来实现
class PrinterActivity : ComponentActivity() {

    companion object {
        fun actionStart(context: Context, data: String) {
            val intent = Intent(context, PrinterActivity::class.java)
            intent.putExtra("data", data)
            context.startActivity(intent)
        }
    }

    private fun openDirectory() {
        val intent: Intent
        val viewModel by viewModels<UploadViewModel>()
        val uri =
            Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !viewModel.hasDataPermission()) {
            // Choose a directory using the system's file picker.

            intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                // Optionally, specify a URI for the directory that should be opened in
                // the system file picker when it loads.
                flags = (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                        or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
                putExtra(
                    DocumentsContract.EXTRA_INITIAL_URI,
                    DocumentFile.fromTreeUri(context, uri)?.uri
                )
            }
            startActivityForResult(intent, 0)
        } else {
            intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                // Optionally, specify a URI for the file that should appear in the
                // system file picker when it loads.
                /*putExtra(DocumentsContract.EXTRA_INITIAL_URI,
                    DocumentFile.fromTreeUri(context, uri)?.uri)*/
            }
            startActivityForResult(intent, 1)
        }

    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int, resultCode: Int, resultData: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, resultData)
        val viewModel by viewModels<UploadViewModel>()
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
            viewModel.saveDataPermission()
        }
        if (requestCode == 1
            && resultCode == Activity.RESULT_OK
        ) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            resultData?.also { intent ->
                // Get the file path from the URI.
                // intent.schema == "content"
                println("intent.data: ${intent.data}")
//                println("intent.schema: ${intent.data.schema}")
                val file = FileUtil.getFile(intent.data!!)
                viewModel.setFile(file)
                viewModel.setFileType(intent.type)
                // Perform operations on the document using its URI.
            }
        }

    }

    private fun parseIntent(intent: Intent) {
        val viewModel by viewModels<UploadViewModel>()
        viewModel.isParsing.value = true
        val action = intent.action!!
        val type = intent.type!!
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
        Log.d("UploadActivity", "uri: $uri action: $action, type: $type, schema: $schema")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vM by viewModels<UploadViewModel>()
        val action = intent.action
        if (action == Intent.ACTION_VIEW || action == Intent.ACTION_SEND) {
            parseIntent(intent)
        }
        val type = intent.type
        Log.d("PrinterActivity", "action: ${vM.uploadFile.value?.name}, type: $type")
        /*

        val uri =
            if (action == Intent.ACTION_VIEW) intent.data!! else intent.getParcelableExtra(Intent.EXTRA_STREAM)!!
//        val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        val schema = uri.scheme!!

        val file: File
        if (schema == "file"
        ) file = uri.toFile() else {
            // schema == "content"
            val cursor = contentResolver.query(uri, null, null, null, null)!!
            cursor.moveToFirst()
            val columnIndex = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
            val fileName = cursor.getString(columnIndex)
            val input = contentResolver.openInputStream(uri)!!
            val output = openFileOutput(fileName, MODE_PRIVATE)
            input.copyTo(output)
            input.close()
            output.close()
            file = File(filesDir, fileName)
            cursor.close()
        }
        val viewModel by viewModels<PrinterViewModel>()
        viewModel.setFile(file)
        viewModel.setFileType(type)*/


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

        setContent {
            PrinterTheme {
                // A surface container using the 'background' color from the theme
                val scaffoldState = rememberScaffoldState()
                val scope = rememberCoroutineScope()
                val viewModel by viewModels<UploadViewModel>()
                var isUploading by viewModel.isUploading
                Box(
                    contentAlignment = Alignment.Center,
                ) {
                    Scaffold(
                        scaffoldState = scaffoldState,
                        topBar = { UploadTopBar() },
                    )
//                    color = MaterialTheme.colors.background
                    { innerPadding ->
                        val paperOptions =
                            listOf(listOf("按原文档纸型打印", "-1"), listOf("A3", "8"), listOf("A4", "9"))
                        val duplexOptions =
                            listOf(listOf("单面", "1"), listOf("双面长边", "2"), listOf("双面短边", "3"))
                        val colorOptions = listOf(listOf("黑白", "1"), listOf("彩色", "2"))
                        val paper = listOf("纸型", "paperId")
                        val duplex = listOf("单双面", "duplex")
                        val color = listOf("颜色", "color")
//                    BoxWithConstraints(
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            contentAlignment = Alignment.TopCenter,
                        ) {
//                        println("height $maxHeight")
                            Column(
                                modifier = Modifier
                                    .width(350.dp)
                                    .padding(top = 16.dp),
                                /*  verticalArrangement = if (maxHeight > 800.dp) Arrangement.spacedBy(
                                      16.dp
                                  ) else Arrangement.spacedBy(
                                      0.dp
                                  ),*/
                            ) {
                                FileSelector({ openDirectory() })
                                UploadRadioOption(paper, paperOptions)
                                UploadDivider()
                                UploadRadioOption(duplex, duplexOptions)
                                UploadDivider()
                                UploadRadioOption(color, colorOptions)
                                UploadDivider()
                                Pages()
                                UploadDivider()
                                Copies()
                                UploadDivider()
                                UploadButton()
                            }
                        }
                    }
                    if (isUploading) {
                        val uploadResponse by viewModel.uploadResponseLiveData.observeAsState()
                        Log.d("UploadActivity", "上传中")
                        Loading(state = "上传中")
                        uploadResponse?.onSuccess { response ->
                            if (response.code == 0) {
                                isUploading = false
                                Log.d("UploadActivity", "上传成功${response.result.szJobName}")
                                scope.launch { scaffoldState.snackbarHostState.showSnackbar("上传成功") }
                            } else {
                                response.message.let { message ->
                                    isUploading = false
                                    Log.d("UploadActivity", "上传失败")
                                    scope.launch {
                                        scaffoldState.snackbarHostState.showSnackbar(message.ifBlank { "上传失败，请检查网络或文件" })
                                    }
                                }
                            }
                        }
                        uploadResponse?.onFailure {
                            Log.d("UploadActivity", "上传失败")
                            isUploading = false
//                            viewModel.setFile(null)
                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar(message = "上传失败，请检查网络或文件",
                                    duration = SnackbarDuration.Short)
                            }
                        }
                    }
                }
            }
        }
    }
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
fun UploadTopBar() {
    TopAppBar(
        modifier = Modifier.height(48.dp),
        backgroundColor = Color.White,
        navigationIcon = {
            IconButton(
                onClick = {
                    // TODO: handle click
                }) {
                Icon(Icons.Sharp.ArrowBackIos, "")
            }
        },
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "云打印",
                textAlign = TextAlign.Center,
            )
        },
        actions = {
            IconButton(
                modifier = Modifier.width(68.dp),
                onClick = {
                    // TODO: handle click
                },
            ) {
                val context = LocalContext.current
                val bitmap = BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.file
                )
                Image(
                    bitmap = bitmap.asImageBitmap(), "",
                    modifier = Modifier
                        .size(width = 24.dp, height = 24.dp)
                )
            }
        },
    )
}

@Composable
fun FileSelector(
    selectFile: () -> Unit,
    viewModel: UploadViewModel = viewModel(),
) {
    val file by viewModel.uploadFile
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.size(350.dp, 160.dp),
            contentAlignment = Alignment.Center,
        ) {
            val context = LocalContext.current
            val bitmap =
                BitmapFactory.decodeResource(context.resources, R.drawable.upload)
            Image(
                bitmap = bitmap.asImageBitmap(), "",
                modifier = Modifier.size(300.dp, 150.dp)
            )
            Button(
                onClick = { selectFile() },
                colors = buttonColors(
                    contentColor = Color.White
                ),
            )
            {
                Text(text = if (file == null) "选择文件" else "重新选择")
            }
            Text(
                text = file?.name ?: "",
                fontSize = 14.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 110.dp)
                    .width(280.dp)
            )
        }
        Text(
            text = "支持：jpg，png，word，excel，pdf，txt",
            modifier = Modifier.padding(bottom = 20.dp),
            fontSize = 12.sp,
        )
    }

}


@Composable
fun UploadRadioOption(
    radioName: List<String>,
//    uploadName: String,
    radioOptions: List<List<String>>,
    viewModel: UploadViewModel = viewModel(),
) {

    val (selectedOption, onOptionSelected) = rememberSaveable { mutableStateOf(radioOptions[0]) }
//    val uploadInfo by viewModel.uploadInfo
// Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
    Row(
        Modifier
            .selectableGroup()
            .height(50.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${radioName[0]}:",
            modifier = Modifier
                .padding(start = 16.dp)
                .width(60.dp),
        )
        // 每个选项是一个row包含按钮和文字，然后整体是外层row的一项
        radioOptions.forEach { option ->
            Row(
                modifier = Modifier.selectable(
                    selected = (option == selectedOption),
                    onClick = {
                        onOptionSelected(option)
                        viewModel.setUploadInfo(radioName[1], option[1])
                    },
                    role = Role.RadioButton
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (option == selectedOption),
                    onClick = null // null recommended for accessibility with screenreaders
                )
                Text(
                    text = option[0],
                    modifier = Modifier.padding(start = 5.dp, end = 10.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}


@Composable
fun Pages(
    viewModel: UploadViewModel = viewModel(),
) {
    @Composable
    fun Page(
    ) {
        var from by rememberSaveable { mutableStateOf("1") }
        var to by rememberSaveable { mutableStateOf("1") }
        Row(
            // 垂直居中
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp, 25.dp)
                    .padding(start = 8.dp)
                    .border(1.dp, Color.Gray),
                contentAlignment = Alignment.CenterStart,
            ) {
                BasicTextField(
                    value = from,
                    singleLine = true,
                    onValueChange =
                    {
                        from =
                            if (it.isBlank() || (it.isDigitsOnly() && it.toInt() > 0 && it.toInt() < 9999)) it else "1"
                        // it为空时设置为1，否则设置为输入的数字，若输入的不是数字，则设置为1
                        viewModel.setUploadInfo("from", it.ifBlank { "1" })
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
            Text(
                text = " ~",
            )
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .size(40.dp, 25.dp)
                    .padding(start = 5.dp)
                    .border(1.dp, Color.Gray),
            ) {
                BasicTextField(
                    value = to,
                    singleLine = true,
                    onValueChange = {
                        to =
                            if (it.isBlank() || (it.isDigitsOnly() && it.toInt() > 0 && it.toInt() < 9999)) it else from
                        viewModel.setUploadInfo("to", it.ifBlank { from })
                    },
                    modifier = Modifier.padding(start = 6.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                )
            }
            Text(
                text = "页",
                modifier = Modifier.padding(start = 6.dp),
                fontSize = 14.sp
            )
        }
    }

    val radioOptions = listOf("全部", "部分")
    val (selectedOption, onOptionSelected) = rememberSaveable { mutableStateOf(radioOptions[0]) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .selectableGroup()
            .height(50.dp),
    ) {
        Text(
            "页数：",
            modifier = Modifier
                .padding(start = 16.dp)
                .width(60.dp),
        )
        radioOptions.forEach { text ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .selectable(
                        selected = (text == selectedOption),
                        onClick = { onOptionSelected(text) },
                        role = Role.RadioButton
                    ),
            ) {
                RadioButton(
                    selected = (text == selectedOption),
                    onClick = null,
//                    modifier = Modifier.padding(start = 10.dp)
                )
                Text(
                    text = text,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 5.dp, end = 10.dp),
                )
            }
        }
        if (selectedOption == "部分") {
            Page()
        } else {
            viewModel.setUploadInfo("from", "0")
            viewModel.setUploadInfo("to", "0")
        }
    }
}


@Composable
fun Copies(
    viewModel: UploadViewModel = viewModel(),
) {
    var count by rememberSaveable { mutableStateOf("1") }
    Row(
        modifier = Modifier.height(50.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "份数:",
            modifier = Modifier
                .width(85.dp)
                .padding(start = 16.dp),
        )
        OutlinedButton(
            onClick = {
                count =
                    if (count.isBlank() || (count.toInt() == 1)) "1" else (count.toInt() - 1).toString()
                viewModel.setUploadInfo("copies", count)
            },
            // count最小为1
            // 调整按钮的大小
            modifier = Modifier.size(25.dp, 25.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "-",
            )
        }
        BasicTextField(
            value = count,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
            onValueChange = {
                count =
                    if (it.isBlank() || (it.isDigitsOnly() && it.toInt() > 0 && it.toInt() < 9999)) it else "1"
                viewModel.setUploadInfo("copies", count.ifBlank { "1" })
            },   // 只能输入数字)},
            modifier = Modifier
                .width((20 + (count.length * 10)).dp)
                .padding(start = 10.dp, top = 2.dp),
        )
        OutlinedButton(
            onClick = {
                count = if (count.isBlank()) "1" else (count.toInt() + 1).toString()
                viewModel.setUploadInfo("copies", count)
            },
            modifier = Modifier.size(25.dp, 25.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("+")
        }
    }
}

@Composable
fun UploadButton(
    viewModel: UploadViewModel = viewModel(),
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.Center,
    ) {
        OutlinedButton(
            modifier = Modifier
                .size(80.dp, 40.dp),
//            contentPadding = PaddingValues(0.dp),
            onClick = {
                viewModel.isUploading.value = true
                viewModel.setUploadData()
                viewModel.upload()
                viewModel.setFile(null)
            },
            enabled = viewModel.uploadFile.value != null,
        ) {
            Text("上传")
        }
    }
}


@Composable
fun UploadDivider() {
    Divider(
        color = Color.Gray,
        thickness = (0.5).dp,
        modifier = Modifier.padding(start = 6.dp, end = 6.dp)
    )
}

@Composable()
fun Loading(state: String) {
    Box(
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize(),
        ) {
            drawRect(
                color = Color.Black,
                alpha = 0.5f
            )
        }
        Box(
            modifier = Modifier
                .size(100.dp, 100.dp)
                .background(color = Color.LightGray),
            contentAlignment = Alignment.Center,
        ) {
            Column(
//                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
                Text(
                    text = state,
//                modifier = Modifier.padding(top = 60.dp)
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PrinterTheme {
        // A surface container using the 'background' color from the theme
        val scaffoldState = rememberScaffoldState()
        val scope = rememberCoroutineScope()
        val viewModel: UploadViewModel = viewModel()
        var isUploading by viewModel.isUploading
        Box(
            contentAlignment = Alignment.Center,
        ) {
            Scaffold(
                scaffoldState = scaffoldState,
                topBar = { UploadTopBar() },
            )
//                    color = MaterialTheme.colors.background
            { innerPadding ->
                val paperOptions =
                    listOf(listOf("按原文档纸型打印", "-1"), listOf("A3", "8"), listOf("A4", "9"))
                val duplexOptions =
                    listOf(listOf("单面", "1"), listOf("双面长边", "2"), listOf("双面短边", "3"))
                val colorOptions = listOf(listOf("黑白", "1"), listOf("彩色", "2"))
                val paper = listOf("纸型", "paperId")
                val duplex = listOf("单双面", "duplex")
                val color = listOf("颜色", "color")
//                    BoxWithConstraints(
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.TopCenter,
                ) {
//                        println("height $maxHeight")
                    Column(
                        modifier = Modifier
                            .width(350.dp)
                            .padding(top = 16.dp),
                        /*  verticalArrangement = if (maxHeight > 800.dp) Arrangement.spacedBy(
                              16.dp
                          ) else Arrangement.spacedBy(
                              0.dp
                          ),*/
                    ) {
                        FileSelector({ })
                        UploadRadioOption(paper, paperOptions)
                        UploadDivider()
                        UploadRadioOption(duplex, duplexOptions)
                        UploadDivider()
                        UploadRadioOption(color, colorOptions)
                        UploadDivider()
                        Pages()
                        UploadDivider()
                        Copies()
                        UploadDivider()
                        UploadButton()
                    }
                }
            }
            /*if (isUploading) {
                val uploadResponse by viewModel.uploadResponseLiveData.observeAsState()
                Log.d("PrinterActivity", "上传中")
                Loading(state = "上传中")
                uploadResponse?.onSuccess { response ->
                    if (response.code == 0) {
                        isUploading = false
//                                viewModel.setFile(null)
                        Log.d("PrinterActivity", "上传成功${response.result.szJobName}")
                        scope.launch { scaffoldState.snackbarHostState.showSnackbar("上传成功") }
                    } else if (response.message.isNotEmpty()) {
                        Log.d("PrinterActivity", "upload failed")
                        isUploading = false
//                                viewModel.setFile(null)
                        scope.launch {
                            scaffoldState.snackbarHostState.showSnackbar(response.message)
                        }
                    }
                }
                uploadResponse?.onFailure {
                    Log.d("PrinterActivity", "上传失败")
                    isUploading = false
//                            viewModel.setFile(null)
                    scope.launch {
                        scaffoldState.snackbarHostState.showSnackbar(message = "上传失败",
                            duration = SnackbarDuration.Short)
                    }
                }
            }*/
        }
    }
}
