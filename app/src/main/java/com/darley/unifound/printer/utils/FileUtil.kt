package com.darley.unifound.printer.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import com.darley.unifound.printer.APP.Companion.context
import java.io.File

object FileUtil {
    private val contentResolver: ContentResolver = context.contentResolver

    @SuppressLint("Range")
    fun contentToFile(uri: Uri): File? {
        // 删除旧文件
        val dirs = File(context.filesDir, "")
        dirs.listFiles()?.forEach {
            it.delete()
        }
        // schema == "content"
        // The query, because it only applies to a single document, returns only
        // one row. There's no need to filter, sort, or select fields,
        // because we want all fields for one document.
        val cursor: Cursor? = contentResolver.query(
            uri, null, null, null, null, null
        )
        cursor?.use {
            // moveToFirst() returns false if the cursor has 0 rows. Very handy for
            // "if there's anything to look at, look at it" conditionals.
            if (it.moveToFirst()) {
                // Note it's called "Display Name". This is
                // provider-specific, and might not necessarily be the file name.
                val displayName: String =
                    it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                val output = context.openFileOutput(displayName, ComponentActivity.MODE_PRIVATE)
                val input = contentResolver.openInputStream(uri)
                input?.use { input.copyTo(output) }
                output.close()
                return File(context.filesDir, displayName)
            }
        }
        return null
    }
}