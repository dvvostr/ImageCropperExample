package ru.studiq.test.imagegallery.model.classes

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import ru.studiq.test.imagegallery.model.App
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.file.Files

class Image(val context: Context) {

    companion object {
        const val IMAGE_MAX_WIDTH = 1200
        const val IMAGE_MAX_HEIGHT = 1200

        fun rotateImage(source: Bitmap, angle: Float): Bitmap {
            val matrix = Matrix()
            matrix.postRotate(angle)
            return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        }

        fun deleteFile(context: Context, file: File) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                val where = MediaStore.MediaColumns.DATA + "=?"
                val selectionArgs = arrayOf(
                    file.absolutePath
                )
                val contentResolver = context.contentResolver
                val filesUri = MediaStore.Files.getContentUri("external")
                contentResolver.delete(filesUri, where, selectionArgs)
                if (file.exists()) {
                    contentResolver.delete(filesUri, where, selectionArgs)
                }
            } else {
                if (file.exists()) {
                    file.delete()
                }
            }
        }
    }
    @Throws(IOException::class)
    private fun resizeImg(filename: Uri): Bitmap? {
        val maxWidth = IMAGE_MAX_WIDTH
        val maxHeight = IMAGE_MAX_HEIGHT
        // create the options
        val opts = BitmapFactory.Options()
        BitmapFactory.decodeFile(Helper.Companion.getRealPath(filename), opts)
        //get the original size
        val orignalHeight = opts.outHeight
        val orignalWidth = opts.outWidth
        //just decode the file
        opts.inJustDecodeBounds = true
        //initialization of the scale
        var resizeScale = 1
        Log.e("qascript orignalWidth", Integer.toString(orignalWidth))
        Log.e("qascript orignalHeight", Integer.toString(orignalHeight))
        //get the good scale
        if (orignalWidth > maxWidth || orignalHeight > maxHeight) {
            resizeScale = 2
        }
        //put the scale instruction (1 -> scale to (1/1); 8-> scale to 1/8)
        opts.inSampleSize = resizeScale
        opts.inJustDecodeBounds = false
        //get the future size of the bitmap
        val bmSize = 6000
        //check if it's possible to store into the vm java the picture
        return if (Runtime.getRuntime().freeMemory() > bmSize) {
            //decode the file
            val `is` = context.contentResolver.openInputStream(filename)
            val bp = BitmapFactory.decodeStream(`is`, Rect(0, 0, 512, 512), opts)
            `is`!!.close()
            bp
        } else {
            Log.e("qascript", "not resize image")
            null
        }
    }
    fun saveImg(filename: Uri, newFilename: String?): Boolean {
        val mimeType = "image/jpeg"
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            newFilename
        )
        val directory = Environment.DIRECTORY_PICTURES
        val mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        try {
            var bmp = resizeImg(filename)?.let { bitmap ->
                bitmap
            } ?: MediaStore.Images.Media.getBitmap(App.instance?.getApplicationContext()?.getContentResolver(), filename)

            var orientation = 1
            val imageOutStream: OutputStream?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues()
                values.put(MediaStore.Images.Media.DISPLAY_NAME, newFilename)
                values.put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                values.put(MediaStore.Images.Media.RELATIVE_PATH, directory)
                val contentResolver = context.contentResolver
                imageOutStream = contentResolver.openOutputStream(contentResolver.insert(mediaContentUri, values)!!)
                try {
                    context.contentResolver.openInputStream(filename).use { inputStream ->
                        val exif = ExifInterface(inputStream!!)
                        orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                imageOutStream = FileOutputStream(file)
                val exif = ExifInterface(Helper.getRealPath(filename)!!)
                orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED
                )
            }
            bmp = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bmp, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bmp, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bmp, 270f)
                ExifInterface.ORIENTATION_NORMAL -> bmp
                else -> bmp
            }
            if (imageOutStream != null) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, imageOutStream)
            }
            imageOutStream?.flush()
            imageOutStream?.close()
        } catch (ex: Exception) {
            return false
            Log.e("qascript saveImg()", ex.message!!)
        }
        return file.exists()
    }
    fun saveBmp(bmp: Bitmap, newFilename: String?): Boolean {
        val mimeType = "image/jpeg"
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            newFilename
        )
        val directory = Environment.DIRECTORY_PICTURES
        val mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        try {
            val imageOutStream: OutputStream?
            imageOutStream = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues()
                values.put(MediaStore.Images.Media.DISPLAY_NAME, newFilename)
                values.put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                values.put(MediaStore.Images.Media.RELATIVE_PATH, directory)
                val contentResolver = context.contentResolver
                contentResolver.openOutputStream(contentResolver.insert(mediaContentUri, values)!!)
            } else {
                FileOutputStream(file)
            }
            if (imageOutStream != null) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, imageOutStream)
            }
            imageOutStream?.flush()
            imageOutStream?.close()
        } catch (ex: Exception) {
            Log.e("qascript saveBmp()", ex.message!!)
            return false
        } finally {
            Log.e("qascript saveBmp()", "success")
        }
        return file.exists()
    }
    fun createImage(filename: Uri, newFilename: String?): Boolean {
        val contextWrapper = ContextWrapper(context)
        val directory = contextWrapper.getDir(context.filesDir.name, Application.MODE_PRIVATE)
        val file = File(directory, newFilename)
        try {
            var bmp = resizeImg(filename)?.let {
                it
            } ?: MediaStore.Images.Media.getBitmap(App.instance?.getApplicationContext()?.getContentResolver(), filename)
            var orientation = 1
            val imageOutStream: OutputStream

            imageOutStream = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Files.newOutputStream(file.toPath())
            } else {
                FileOutputStream(file)
            }
            val exif = ExifInterface(Helper.getRealPath(filename)!!)
            orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )
            bmp = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bmp, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bmp, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bmp, 270f)
                ExifInterface.ORIENTATION_NORMAL -> bmp
                else -> bmp
            }
            bmp!!.compress(Bitmap.CompressFormat.JPEG, 90, imageOutStream)
            imageOutStream.flush()
            imageOutStream.close()
        } catch (ex: Exception) {
            Log.e("qascript saveImg()", ex.message!!)
            return false
        }
        return file.exists()
    }

    fun createThumbnail(bmp: Bitmap, newFilename: String?): Boolean {
        val contextWrapper = ContextWrapper(context)
        val directory = contextWrapper.getDir(context!!.filesDir.name, Application.MODE_PRIVATE)
        val file = File(directory, newFilename)
        try {
            val imageOutStream: OutputStream
            imageOutStream = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Files.newOutputStream(file.toPath())
            } else {
                FileOutputStream(file)
            }
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, imageOutStream)
            imageOutStream.flush()
            imageOutStream.close()
        } catch (ex: Exception) {
            Log.e("createThumbnail()", ex.message!!)
            return false
        } finally {
            Log.e("createThumbnail()", "success")
        }
        return file.exists()
    }


}