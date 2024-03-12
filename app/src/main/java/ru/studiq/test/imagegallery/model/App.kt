package ru.studiq.test.imagegallery.model

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import ru.studiq.test.imagegallery.model.classes.CustomFragmentImageSelectDialog
import java.io.File

class App : Application() {
    var directory: File? = null
        private set

    override fun onCreate() {
        super.onCreate()
        appContext = this.applicationContext
        instance = this
        res = resources
        directory = ContextWrapper(applicationContext).getDir(filesDir.name, MODE_PRIVATE)
        initObjects()
    }

    companion object {
        lateinit var appContext: Context

        var instance: App? = null
            private set
        var res: Resources? = null
            private set

        var displayDensity: Float = 1.0F
            get() {
                return App.appContext.getResources().getDisplayMetrics().density
            }

        @Synchronized
        fun appInstance(): App? {
            return instance
        }
    }

    fun initObjects() {
        CustomFragmentImageSelectDialog.Config.apply {
            alertStyle = 0
            captionSettings = "Settings"
            captionCamera = "Camera"
            captionGallery = "Choose from gallery"
            captionNoCameraPermission = "Camera permission isn\'t granted"
            captionGrantCameraPermission = "Open Permissions and grant the Camera permission"
            captionNoStoragePermission = "Storage permission isn\'t granted"
            captionGrantStoragePermission = "Open Permissions and grant the Storage permission"
            errorTryAgain = "Error occured. Please try again later."
        }
    }
}