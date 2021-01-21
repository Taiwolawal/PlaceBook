package com.raywenderlich.placebook.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

object ImageUtils {

    fun saveBitmapToFile(context: Context, bitmap: Bitmap, filename: String) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val bytes = stream.toByteArray()
        ImageUtils.saveBytesToFile(context, bytes, filename)
    }
    // 7
    private fun saveBytesToFile(context: Context, bytes: ByteArray, filename: String) {
        val outputStream: FileOutputStream
        try {
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE)
            outputStream.write(bytes)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadBitmapFromFile(context: Context, filename: String): Bitmap? {
        val filePath = File(context.filesDir, filename).absolutePath
        return BitmapFactory.decodeFile(filePath)
    }

    @Throws(IOException::class)
    fun createUniqueImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val filename = "PlaceBook_" + timeStamp + "_"
        val filesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(filename, ".jpg", filesDir)
    }

    private fun calculateInSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int)
            : Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun decodeFileToSize(filePath: String, width: Int, height: Int): Bitmap {
// 1
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)
// 2
        options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, width, height)
// 3
        options.inJustDecodeBounds = false
// 4
        return BitmapFactory.decodeFile(filePath, options)
    }

    fun decodeUriStreamToSize(uri: Uri, width: Int, height: Int, context: Context): Bitmap? {
        var inputStream: InputStream? = null
        try {
            val options: BitmapFactory.Options
// 1
            inputStream = context.contentResolver.openInputStream(uri)
// 2
            if (inputStream != null) {
// 3
                options = BitmapFactory.Options()
                options.inJustDecodeBounds = false
                BitmapFactory.decodeStream(inputStream, null, options)
// 4
                inputStream.close()
                inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
// 5
                    options.inSampleSize = calculateInSampleSize(
                        options.outWidth, options.outHeight,
                        width, height)
                    options.inJustDecodeBounds = false
                    val bitmap = BitmapFactory.decodeStream(
                        inputStream, null, options)
                    inputStream.close()
                    return bitmap
                }
            }
            return null
        } catch (e: Exception) {
            return null
        } finally {
// 6
            inputStream?.close()
        }
    }



}