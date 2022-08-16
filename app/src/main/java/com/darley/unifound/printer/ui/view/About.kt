package com.darley.unifound.printer.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun About(onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        contentAlignment = Alignment.Center,
    ) {
        TextButton(onClick = onClick)
        {
            Text(
                text = "关于",
                fontSize = 12.sp,
                color = Color.Black,
                textDecoration = TextDecoration.Underline,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreviewAbout() {
    About()
}