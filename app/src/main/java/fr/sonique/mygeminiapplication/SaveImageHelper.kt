package fr.sonique.mygeminiapplication

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.IOException
import java.io.OutputStream

object SaveImageHelper {


    private fun finishSaveFile(context: Context, uri: Uri) {
        val values = ContentValues()

        values.put(MediaStore.Images.Media.IS_PENDING, false)
        context.getContentResolver().update(uri, values, null, null)
    }

    @Throws(IOException::class)
    fun saveBitmap(context: Context, bitmap: Bitmap, fileName: String) {

        var uri: Uri? = null
        if (Build.VERSION.SDK_INT >= 29) {

            val values = ContentValues()
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            values.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + File.separator + "geminiweather"
            )
            values.put(MediaStore.MediaColumns.ALBUM, "geminiweather")
            values.put(MediaStore.MediaColumns.IS_PENDING, true)

            // RELATIVE_PATH and IS_PENDING are introduced in API 29.
            uri = context.contentResolver
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                val outputStream = context.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    savePNG(outputStream, bitmap)
                    finishSaveFile(context, uri)
                }
            } else {
                Log.w("savefile", "openFile: Uri is null")
            }
        } else {
            Log.w("savefile", "openFile: android.os.Build.VERSION.SDK_INT < 29")

        }
    }

    @Throws(IOException::class)
    private fun savePNG(outputStream: OutputStream, bitmap: Bitmap) {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.close()
    }
}