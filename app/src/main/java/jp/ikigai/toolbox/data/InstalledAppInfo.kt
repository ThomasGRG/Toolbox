package jp.ikigai.toolbox.data

import androidx.compose.ui.graphics.ImageBitmap

data class InstalledAppInfo(
    val packageName: String,
    val label: String,
    val icon: ImageBitmap,
)
