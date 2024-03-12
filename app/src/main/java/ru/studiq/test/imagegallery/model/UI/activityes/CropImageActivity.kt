package ru.studiq.test.imagegallery.model.UI.activityes

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.canhub.cropper.CropImageView
import ru.studiq.test.imagegallery.R
import java.io.ByteArrayOutputStream


class CropImageActivity : CustomActivity() {
    companion object {
        const val PARAM_IMAGE = "PARAM_IMAGE"
    }
    private val toolbar by lazy { findViewById<Toolbar?>(R.id.toolbar) }
    private val imageView by lazy { findViewById<CropImageView?>(R.id.view_crop_image) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop_image)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.title = "Crop image"
        supportActionBar?.subtitle = "Crop image description"
        load(Uri.parse(intent.getStringExtra(PARAM_IMAGE)))
    }

    override fun onBackButtonClick() {
        super.onBackButtonClick()
        finish()
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.clear()
        menuInflater.inflate(R.menu.menu_crop_image, menu)
        menu?.findItem(R.id.action_save)?.actionView?.findViewById<View>(R.id.action_menu_save)?.
            setOnClickListener { handleSave() }
        return super.onCreateOptionsMenu(menu)
    }
    private fun load(uri: Uri) {
        imageView?.setImageUriAsync(uri)
    }
    private fun handleSave() {
        imageView.setOnCropImageCompleteListener(object: CropImageView.OnCropImageCompleteListener {
            override fun onCropImageComplete(view: CropImageView, result: CropImageView.CropResult) {
                val intent = Intent()
                intent.putExtra(PARAM_ACTION, action)
                var res: Int = if (result.isSuccessful) RESULT_OK else RESULT_CANCELED
                if (result.isSuccessful) {
                    intent.putExtra(PARAM_IMAGE, result.uriContent.toString())
                }
                setResult(res, intent)
                finish()
            }
        })
        imageView.croppedImageAsync()
    }
}
