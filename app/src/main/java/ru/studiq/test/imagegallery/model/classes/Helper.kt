package ru.studiq.test.imagegallery.model.classes

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import ru.studiq.test.imagegallery.model.App
import java.io.File
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

class Helper : Application {
    private var activity: AppCompatActivity? = null
    private var context: Context? = null

    constructor()
    constructor(current: Context?) {
        context = current
    }
    constructor(activity: AppCompatActivity?) {
        this.activity = activity
    }
    companion object {
        const val AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        var rnd = SecureRandom()

        fun checkPermission(permission: String?): Boolean {
            return (App.instance?.let {
                ContextCompat.checkSelfPermission(it.getApplicationContext(), permission!!)
            } ?: PackageManager.PERMISSION_DENIED) == PackageManager.PERMISSION_GRANTED
        }
        fun checkStoragePermission(): Boolean {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
            return checkPermission(permission)
        }
        fun checkCameraPermission(): Boolean {
            val permission = Manifest.permission.CAMERA
            return checkPermission(permission)
        }
        fun checkRecordAudioPermission(): Boolean {
            val permission = Manifest.permission.RECORD_AUDIO
            return checkPermission(permission)
        }
        fun checkVideoPermission(): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                checkPermission(Manifest.permission.READ_MEDIA_VIDEO)
            } else {
                false
            }
        }

        fun getRealPath(uri: Uri?): String? {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val type = split[0]
            val contentUri: Uri
            contentUri = when (type) {
                "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                else -> MediaStore.Files.getContentUri("external")
            }
            val selection = "_id=?"
            val selectionArgs = arrayOf(split[1])
            return getDataColumn(App.instance?.getApplicationContext(),
                contentUri,
                selection,
                selectionArgs
            )
        }

        fun getDataColumn(context: Context?, uri: Uri?, selection: String?, selectionArgs: Array<String>): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(column)
            try {
                cursor = context?.contentResolver?.query(uri!!, projection, selection, selectionArgs, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val column_index = cursor.getColumnIndexOrThrow(column)
                    val value = cursor.getString(column_index)
                    return if (value.startsWith("content://") || !value.startsWith("/") && !value.startsWith(
                            "file://"
                        )
                    ) {
                        null
                    } else value
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
            return null
        }

        fun dpToPx(c: Context, dp: Int): Int {
            val r = c.resources
            return Math.round(
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    dp.toFloat(),
                    r.displayMetrics
                )
            )
        }

        fun getGalleryGridCount(activity: FragmentActivity, cellWidth: Float): Int {
            val display = activity.windowManager.defaultDisplay
            val displayMetrics = DisplayMetrics()
            display.getMetrics(displayMetrics)
            val screenWidth = displayMetrics.widthPixels.toFloat()
            return Math.round(screenWidth / cellWidth)
        }

        fun getStickersGridSpanCount(activity: FragmentActivity?, cellWidth: Float, offset: Float): Int {
            val display = activity?.windowManager?.defaultDisplay
            val displayMetrics = DisplayMetrics()
            display?.getMetrics(displayMetrics)
            val screenWidth = displayMetrics.widthPixels.toFloat()
            return Math.round(screenWidth / (cellWidth + offset))
        }

        fun randomString(len: Int): String {
            val sb = StringBuilder(len)
            for (i in 0 until len) sb.append(
                Helper.Companion.AB.get(
                    Helper.Companion.rnd.nextInt(
                        Helper.Companion.AB.length
                    )
                )
            )
            return sb.toString()
        }

        fun md5(s: String): String {
            val MD5 = "MD5"
            try {

                // Create MD5 Hash
                val digest = MessageDigest.getInstance(MD5)
                digest.update(s.toByteArray())
                val messageDigest = digest.digest()

                // Create Hex String
                val hexString = StringBuilder()
                for (aMessageDigest in messageDigest) {
                    var h = Integer.toHexString(0xFF and aMessageDigest.toInt())
                    while (h.length < 2) h = "0$h"
                    hexString.append(h)
                }
                return hexString.toString()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
            return ""
        }
    }
}
