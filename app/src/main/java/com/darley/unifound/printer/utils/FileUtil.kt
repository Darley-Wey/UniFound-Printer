package com.darley.unifound.printer.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.documentfile.provider.DocumentFile
import com.darley.unifound.printer.APP.Companion.context
import java.io.File
import java.io.FileDescriptor
import java.io.IOException

object FileUtil {
    private val contentResolver: ContentResolver = context.contentResolver

    @SuppressLint("Range")
    fun getFile(uri: Uri): File? {
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
                val input = contentResolver.openInputStream(uri)!!
                val output = context.openFileOutput(displayName, ComponentActivity.MODE_PRIVATE)
                input.copyTo(output)
                input.close()
                output.close()
                return File(context.filesDir, displayName)
//                Log.i(TAG, "Display Name: $displayName")

//                val sizeIndex: Int = it.getColumnIndex(OpenableColumns.SIZE)
                // If the size is unknown, the value stored is null. But because an
                // int can't be null, the behavior is implementation-specific,
                // and unpredictable. So as
                // a rule, check if it's null before assigning to an int. This will
                // happen often: The storage API allows for remote files, whose
                // size might not be locally known.
//                val size: String = if (!it.isNull(sizeIndex)) {
                // Technically the column stores an int, but cursor.getString()
                // will do the conversion automatically.
//                    it.getString(sizeIndex)
//                } else {
//                    "Unknown"
//                }
//                Log.i(TAG, "Size: $size")
            }
        }
        return null
    }


    @Throws(IOException::class)
    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val parcelFileDescriptor: ParcelFileDescriptor =
            contentResolver.openFileDescriptor(uri, "r")!!
        val fileDescriptor: FileDescriptor = parcelFileDescriptor.fileDescriptor
        val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        val documentFile: DocumentFile = DocumentFile.fromSingleUri(context, uri)!!
        val input = contentResolver.openInputStream(uri)!!
        val output = context.openFileOutput(documentFile.name, ComponentActivity.MODE_PRIVATE)
        input.copyTo(output)
        input.close()
        output.close()

//        file.
//            "image",
//            "image.png"
//        ).parcelFileDescriptor.close()
        return image
    }
}