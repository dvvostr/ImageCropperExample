package ru.studiq.test.imagegallery.model.classes

import android.Manifest
import android.R
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import ru.studiq.test.imagegallery.model.App
import java.io.File

class CustomFragmentImageSelectDialog {
    interface IListener {
        fun onFileUpload(file: File, params: Any?) {}
    }
    companion object Config{
        var alertStyle: Int = 0
        var captionSettings = "Settings"

        var captionCamera: String = "Camera"
        var captionGallery: String = "Choose from gallery"
        var captionNoCameraPermission = "Camera permission isn\'t granted"
        var captionGrantCameraPermission = "Open Permissions and grant the Camera permission"
        var captionNoStoragePermission = "Storage permission isn\'t granted"
        var captionGrantStoragePermission = "Open Permissions and grant the Storage permission"
        var errorTryAgain = "Error occured. Please try again later."


    }
    private var cameraPermissionLauncher: ActivityResultLauncher<String>? = null
    private var storagePermissionLauncher: ActivityResultLauncher<Array<String>>? = null
    private var imgFromGalleryActivityResultLauncher: ActivityResultLauncher<String>? = null
    private var imgFromCameraActivityResultLauncher: ActivityResultLauncher<Intent>? = null

    private var listener: IListener? = null
    private var intFragment: Fragment? = null
    private var intParams: Any? = null

    constructor(owner: Fragment){
        createLaunchers(owner)
    }

    fun setListener(listener: IListener?) {
        this.listener = listener
    }
    private fun createLaunchers(fragment: Fragment) {
        intFragment = fragment
        imgFromGalleryActivityResultLauncher = fragment.registerForActivityResult(ActivityResultContracts.GetContent()) { result->
            result?.let { uri ->
                val context = App.instance?.applicationContext
                Image(fragment.requireActivity()).createImage(uri, Constants.IMAGE_FILE)
                val file = File(App.instance?.directory, Constants.IMAGE_FILE)
                listener?.onFileUpload(file, intParams)
            }
        }

        storagePermissionLauncher = fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted: Map<String, Boolean> ->
            var granted = false
            var storage_permission =
                Manifest.permission.READ_EXTERNAL_STORAGE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                storage_permission = Manifest.permission.READ_MEDIA_IMAGES
            }
            for ((key, value) in isGranted) {
                if (key == storage_permission) {
                    if (value) {
                        granted = true
                    }
                }
            }
            if (granted) {
                choiceImage(intParams)
            } else {
                Log.e("Permissions", "denied")
                Snackbar.make(fragment.requireView(), captionNoStoragePermission, Snackbar.LENGTH_LONG)
                    .setAction(captionSettings) {
                        val appSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + App.instance!!.packageName))
                        fragment.startActivity(appSettingsIntent)
                        Toast.makeText(fragment.activity, captionGrantStoragePermission, Toast.LENGTH_SHORT).show()
                    }.show()
            }
        }
        cameraPermissionLauncher = fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted
                Log.e("Permissions", "Permission is granted")
                choiceImage(intParams)
            } else {
                // Permission is denied
                Log.e("Permissions", "denied")
                Snackbar.make(fragment.requireView(), captionNoCameraPermission, Snackbar.LENGTH_LONG).setAction(captionSettings) {
                    val appSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:" + App.instance!!.packageName))
                    fragment.startActivity(appSettingsIntent)
                    Toast.makeText(fragment.activity, captionGrantCameraPermission, Toast.LENGTH_SHORT).show()
                }.show()
            }
        }
        imgFromCameraActivityResultLauncher = fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result?.resultCode == Activity.RESULT_OK) {
                val file = File(App.instance!!.directory, Constants.IMAGE_FILE)
                listener?.onFileUpload(file, intParams)
            }
        }
    }

    fun requestStoragePermission(params: Any?) {
        intParams = params
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            storagePermissionLauncher!!.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES))
        } else {
            storagePermissionLauncher!!.launch(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
        }
    }

    private fun requestCameraPermission(params: Any?) {
        intParams = params
        cameraPermissionLauncher!!.launch(Manifest.permission.CAMERA)
    }

    fun choiceImage(params: Any?) {
        intParams = params
        intFragment?.let { fragment ->
            val builderSingle = AlertDialog.Builder(fragment.requireActivity(), alertStyle)
            val arrayAdapter = ArrayAdapter<String>(fragment.requireActivity(), R.layout.simple_list_item_1)
            arrayAdapter.add(captionGallery)
            arrayAdapter.add(captionCamera)
            builderSingle.setAdapter(arrayAdapter) { dialog, which ->
                when (which) {
                    0 -> {
                        imgFromGalleryActivityResultLauncher?.launch("image/*")
                    }
                    else -> {
                        if (Helper.checkPermission(Manifest.permission.CAMERA)) {
                            try {
                                val selectedImage = FileProvider.getUriForFile(
                                    App.instance!!.applicationContext,
                                    App.instance!!.packageName + ".provider",
                                    File(App.instance!!.directory, Constants.IMAGE_FILE)
                                )
                                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                cameraIntent.putExtra(
                                    MediaStore.EXTRA_OUTPUT,
                                    selectedImage
                                )
                                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                                imgFromCameraActivityResultLauncher!!.launch(cameraIntent)
                            } catch (e: Exception) {
                                Toast.makeText(fragment.requireActivity(), errorTryAgain, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            requestCameraPermission(params)
                        }
                    }
                }
            }
            val d = builderSingle.create()
            d.show()
        }
    }
}