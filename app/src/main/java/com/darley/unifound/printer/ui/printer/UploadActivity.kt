package com.darley.unifound.printer.ui.printer

import android.app.Activity
import android.app.ActivityOptions
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.RadioButton
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toFile
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.viewmodel.compose.viewModel
import com.darley.unifound.printer.APP.Companion.context
import com.darley.unifound.printer.R
import com.darley.unifound.printer.data.network.model.UploadRes
import com.darley.unifound.printer.ui.WebViewActivity
import com.darley.unifound.printer.ui.login.LoginActivity
import com.darley.unifound.printer.ui.theme.PrinterTheme
import com.darley.unifound.printer.ui.view.About
import com.darley.unifound.printer.ui.view.Loading
import com.darley.unifound.printer.ui.view.UploadDivider
import com.darley.unifound.printer.utils.ActivityCollector
import com.darley.unifound.printer.utils.FileUtil
import com.darley.unifound.printer.utils.ScreenUtil
import kotlinx.coroutines.launch
import me.jessyan.progressmanager.ProgressManager


// Compose Activity 使用 ComponentActivity 来实现
class UploadActivity : ComponentActivity() {
    private val viewModel by viewModels<UploadViewModel>()

    companion object {
        fun actionStart(context: Context, data: String) {
            val intent = Intent(context, UploadActivity::class.java)
            intent.putExtra("data", data)
            context.startActivity(
                intent,
                ActivityOptions.makeSceneTransitionAnimation(context as Activity).toBundle()
            )
        }
    }

    private val registerForSelectFile =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                val contentResolver: ContentResolver = context.contentResolver
                val file = FileUtil.contentToFile(uri)
                val type = contentResolver.getType(uri)
                viewModel.setFile(file)
                viewModel.setFileType(type)
            }
        }

    private fun selectFile() {
        registerForSelectFile.launch("*/*")
    }

    private fun parseIntent(intent: Intent) {
        viewModel.isParsing.value = true
        val action = intent.action
        val type = intent.type
        val uri =
            if (action == Intent.ACTION_VIEW) intent.data else intent.getParcelableExtra(Intent.EXTRA_STREAM)
        uri?.let {
            val file = if (it.scheme == "file") it.toFile() else FileUtil.contentToFile(it)
            viewModel.setFile(file)
            viewModel.setFileType(type)
            viewModel.isParsing.value = false
            Log.d("UploadActivity", "uri: $uri action: $action, type: $type, scheme: $it.scheme")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProgressManager.getInstance()
            .addRequestListener(
                "http://10.135.0.139:9130/api/client/CloudPrint/Upload",
                viewModel.getUploadListener()
            )
        requestedOrientation = if (ScreenUtil.isTabletWindow(this)) {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        ActivityCollector.addActivity(this)
        val action = intent.action
        if (action == Intent.ACTION_VIEW || action == Intent.ACTION_SEND) {
            parseIntent(intent)
        }

        setContent {
            PrinterTheme {
                val scaffoldState = rememberScaffoldState()
                val scope = rememberCoroutineScope()
                val viewModel by viewModels<UploadViewModel>()
                // 跳转登录页面
                if (!viewModel.hasLoginInfo()) {
                    val intent = Intent(this@UploadActivity, LoginActivity::class.java)
                    startActivity(
                        intent,
                        ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
                    )
                    finish()
                }
                var isUploading by viewModel.isUploading
                val animatedProgress by animateFloatAsState(
                    targetValue = viewModel.uploadProgress,
                    animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
                )

                if (!viewModel.isValidFileType) {
                    WrongFileTypeDialog()
                }

                Box(
                    contentAlignment = Alignment.Center,
                ) {
                    Scaffold(
                        scaffoldState = scaffoldState,
                        topBar = {
                            UploadTopBar(
                                goBack = { onBackPressed() },
                                goWebView = {
                                    WebViewActivity.actionStart(
                                        this@UploadActivity,
                                        WebViewActivity.DOC_URL
                                    )
                                }
                            )
                        },
                    )
                    // Scaffold 必须接收 innerPadding
                    { innerPadding ->
                        val paperOptions =
                            listOf(
                                listOf("按原文档纸型打印", "-1"),
                                listOf("A3", "8"),
                                listOf("A4", "9")
                            )
                        val duplexOptions =
                            listOf(
                                listOf("单面", "1"),
                                listOf("双面长边", "2"),
                                listOf("双面短边", "3")
                            )
                        val colorOptions = listOf(listOf("黑白", "1"), listOf("彩色", "2"))
                        val paper = listOf("纸型", "paperId")
                        val duplex = listOf("单双面", "duplex")
                        val color = listOf("颜色", "color")
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(innerPadding),
                            contentAlignment = Alignment.TopCenter,
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                val configuration = LocalConfiguration.current
                                val screenHeight = configuration.screenHeightDp.dp
                                println("height $screenHeight")
                                FileSelector {
                                    selectFile()
                                }
                                Spacer(modifier = Modifier.height(if (screenHeight > 900.dp) 40.dp else 20.dp))
                                Column(
                                    modifier = Modifier.width(400.dp),
                                    verticalArrangement = if (screenHeight > 900.dp) Arrangement.spacedBy(
                                        16.dp
                                    ) else Arrangement.spacedBy(
                                        0.dp
                                    ),
                                ) {
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
                                    Spacer(modifier = Modifier.height(20.dp))
                                    UploadButton()
                                    About(onClick = {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://gitee.com/Darley-Wey/Unifound-Printer")
                                        )
                                        startActivity(intent)
                                    })
                                }
                            }
                        }
                    }
                    if (isUploading) {
                        val uploadResponse by viewModel.uploadResponseLiveData.observeAsState()
                        Log.d("UploadActivity", "上传中")
                        Loading(state = viewModel.uploadState, progress = animatedProgress)

                        uploadResponse?.onSuccess { response: UploadRes ->
                            isUploading = false
                            viewModel.setFile(null)
                            if (response.code == 0) {
                                Log.d("UploadActivity", "上传成功${response.result!!.szJobName}")
                                viewModel.uploadProgress = 0f
                                viewModel.uploadState = "上传中，0%"
                                scope.launch {
                                    scaffoldState.snackbarHostState.showSnackbar(
                                        message = "上传成功",
                                        duration = SnackbarDuration.Short
                                    )
                                    WebViewActivity.actionStart(
                                        this@UploadActivity,
                                        WebViewActivity.DOC_URL
                                    )
                                }
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
                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar(message = "上传失败，请检查网络或文件")
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun UploadTopBar(goBack: () -> Unit, goWebView: () -> Unit) {
    val context = LocalContext.current
    val back = BitmapFactory.decodeResource(context.resources, R.drawable.back)
    val folder = BitmapFactory.decodeResource(context.resources, R.drawable.folder)
    TopAppBar(
        modifier = Modifier.height(48.dp),
        backgroundColor = Color.White,
        navigationIcon = {
            IconButton(
                onClick = { goBack() },
            ) {
                Image(
                    bitmap = back.asImageBitmap(), "返回",
                    modifier = Modifier.size(width = 24.dp, height = 24.dp)
                )
            }
        },
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "云打印",
                textAlign = TextAlign.Center,
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal,
            )
        },
        actions = {
            IconButton(
                modifier = Modifier.width(68.dp),
                onClick = { goWebView() },
            ) {
                Image(
                    bitmap = folder.asImageBitmap(), "待打印文档",
                    modifier = Modifier.size(width = 24.dp, height = 24.dp)
                )
            }
        },
    )
}


@Composable
fun FileSelector(
    viewModel: UploadViewModel = viewModel(),
    selectFile: () -> Unit,
) {
    val file by viewModel.uploadFile
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(0.8f),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                val context = LocalContext.current
                val bitmap =
                    BitmapFactory.decodeResource(context.resources, R.drawable.upload)
                Image(
                    bitmap = bitmap.asImageBitmap(), "文件选择器背景图",
                    modifier = Modifier.fillMaxWidth(),
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
            }
            Text(
                text = file?.name ?: "",
                fontSize = 14.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .width(280.dp)
            )
        }
        Text(
            text = "支持：jpg，png，word，excel，pdf，txt",
            modifier = Modifier.padding(top = 10.dp),
            fontSize = 12.sp,
        )
    }
}


@Composable
fun UploadRadioOption(
    radioName: List<String>,
    radioOptions: List<List<String>>,
    viewModel: UploadViewModel = viewModel(),
) {
    val (selectedOption, onOptionSelected) = rememberSaveable { mutableStateOf(radioOptions[0]) }
// Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
    Row(
        modifier = Modifier
            .selectableGroup()
            .height(50.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        Text(
            text = "${radioName[0]}:",
            modifier = Modifier
                .padding(start = 20.dp)
                .width(60.dp),
        )
        // 每个选项是一个row包含按钮和文字，然后整体是外层row的一项
        radioOptions.forEach { option ->
            Row(
                modifier = Modifier
                    .selectable(
                        selected = (option == selectedOption),
                        onClick = {
                            onOptionSelected(option)
                            viewModel.setUploadInfo(radioName[1], option[1])
                        },
                        role = Role.RadioButton
                    )
                    .height(25.dp),
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
    fun Page() {
        var from by rememberSaveable { mutableStateOf("1") }
        var to by rememberSaveable { mutableStateOf("1") }
        // 控件每有更新时此处就会重新执行
        viewModel.setUploadInfo("from", from.ifBlank { "1" })
        viewModel.setUploadInfo("to", to.ifBlank { "1" })
        Row(
            // 垂直居中
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(43.dp, 25.dp)
                    .padding(start = 5.dp)
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
                modifier = Modifier
                    .size(43.dp, 25.dp)
                    .padding(start = 5.dp)
                    .border(1.dp, Color.Gray),
                contentAlignment = Alignment.CenterStart,
            ) {
                BasicTextField(
                    value = to,
                    singleLine = true,
                    onValueChange = {
                        to =
                            if (it.isBlank() || (it.isDigitsOnly() && it.toInt() > 0 && it.toInt() < 9999)) it else from
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
            text = "页数：",
            modifier = Modifier
                .padding(start = 20.dp)
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
                    )
                    .height(25.dp),
            ) {
                RadioButton(
                    selected = (text == selectedOption),
                    onClick = null,
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
    var copies by rememberSaveable { mutableStateOf("1") }
    viewModel.setUploadInfo("copies", copies.ifBlank { "1" })
    Row(
        modifier = Modifier.height(50.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        Text(
            text = "份数:",
            modifier = Modifier
                .width(80.dp)
                .padding(start = 20.dp),
        )
        OutlinedButton(
            onClick = {
                copies =
                    if (copies.isBlank() || (copies.toInt() == 1)) "1" else (copies.toInt() - 1).toString()
            },
            // count最小为1
            // 调整按钮的大小，内边距设置为0
            modifier = Modifier.size(25.dp, 25.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "-",
            )
        }
        BasicTextField(
            value = copies,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
            onValueChange = {
                // 只能输入数字，且不能为0
                copies =
                    if (it.isBlank() || (it.isDigitsOnly() && it.toInt() > 0 && it.toInt() < 9999)) it else "1"
            },
            modifier = Modifier
                .width((20 + (copies.length * 10)).dp)
                .padding(start = 10.dp, top = 2.dp),
        )
        OutlinedButton(
            onClick = {
                copies = if (copies.isBlank()) "1" else (copies.toInt() + 1).toString()
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
            onClick = {
                viewModel.isUploading.value = true
                viewModel.setUploadData()
                viewModel.upload()
            },
            enabled = viewModel.uploadFile.value != null,
        ) {
            Text("确定")
        }
    }
}

@Composable
fun WrongFileTypeDialog(
    viewModel: UploadViewModel = viewModel(),
) {
    if (!viewModel.isValidFileType) {
        AlertDialog(
            onDismissRequest = {
                viewModel.isValidFileType = true
                viewModel.uploadFile.value = null
            },
            title = {
                Text(text = "不支持此种格式文件")
            },
            text = {
                Text(text = "请重新选择文件")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.isValidFileType = true
                        viewModel.uploadFile.value = null
                    }
                ) {
                    Text("确定")
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PrinterTheme {
        val scaffoldState = rememberScaffoldState()
        Box(
            contentAlignment = Alignment.Center,
        ) {
            Scaffold(
                scaffoldState = scaffoldState,
            )
            // Scaffold 必须接收 innerPadding
            { innerPadding ->
                val paperOptions =
                    listOf(listOf("按原文档纸型打印", "-1"), listOf("A3", "8"), listOf("A4", "9"))
                val duplexOptions =
                    listOf(listOf("单面", "1"), listOf("双面长边", "2"), listOf("双面短边", "3"))
                val colorOptions = listOf(listOf("黑白", "1"), listOf("彩色", "2"))
                val paper = listOf("纸型", "paperId")
                val duplex = listOf("单双面", "duplex")
                val color = listOf("颜色", "color")
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(innerPadding),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        val configuration = LocalConfiguration.current
                        val screenHeight = configuration.screenHeightDp.dp
                        println("height $screenHeight")
                        FileSelector() {}
                        Spacer(modifier = Modifier.height(if (screenHeight > 900.dp) 40.dp else 20.dp))
                        Column(
                            modifier = Modifier.width(400.dp),
                            verticalArrangement = if (screenHeight > 900.dp) Arrangement.spacedBy(
                                16.dp
                            ) else Arrangement.spacedBy(
                                0.dp
                            ),
                        ) {
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
                            Spacer(modifier = Modifier.height(20.dp))
                            UploadButton()
                            About()
                        }
                    }
                }
            }
        }
    }
}
