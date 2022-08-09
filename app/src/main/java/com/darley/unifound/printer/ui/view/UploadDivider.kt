package com.darley.unifound.printer.ui.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun UploadDivider() {
    Divider(
        color = Color.Gray,
        thickness = (0.5).dp,
        modifier = Modifier.padding(start = 6.dp, end = 6.dp)
    )
}
