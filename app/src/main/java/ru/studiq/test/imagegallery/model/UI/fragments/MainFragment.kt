package ru.studiq.test.imagegallery.model.UI.fragments

import android.app.Activity
import android.content.Intent
import android.content.Intent.getIntent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import ru.studiq.test.imagegallery.R
import ru.studiq.test.imagegallery.model.App
import ru.studiq.test.imagegallery.model.UI.activityes.CropImageActivity
import ru.studiq.test.imagegallery.model.UI.activityes.CustomActivity
import ru.studiq.test.imagegallery.model.UI.fragments.imagegallery.ImageGalleryFragment
import ru.studiq.test.imagegallery.model.UI.fragments.imagegallery.ImageGalleryItem
import ru.studiq.test.imagegallery.model.UI.fragments.imagegallery.ImageGalleryItems
import ru.studiq.test.imagegallery.model.classes.CustomFragmentImageSelectDialog
import ru.studiq.test.imagegallery.model.classes.Helper
import java.io.File
import java.io.FileDescriptor


class MainFragment : CustomFragment() {
    private var imageDialog: CustomFragmentImageSelectDialog? = null

    private var viewHeader: View? = null
    private var viewBody: View? = null
    private var viewBottom: View? = null

    private var btnBottomLeft: Button? = null
    private var btnBottomRight: Button? = null

    private var imageGallery: ImageGalleryFragment? = null

//    private var cropImage: Any? = null
    private var cropImage: ActivityResultLauncher<CropImageContractOptions>? = null

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        handleActivityResult(result)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        initialize(view)
        return view
    }
    companion object {
        @JvmStatic
        fun newInstance() =
            MainFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun initialize(view: View?) {
        super.initialize(view)
        imageDialog = CustomFragmentImageSelectDialog(this)
        imageDialog?.setListener(object : CustomFragmentImageSelectDialog.IListener {
            override fun onFileUpload(file: File, params: Any?) {
                handleFileUpload((params as? Long) ?: 0, file)
            }
        })
        viewHeader = view?.findViewById(R.id.container_header)
        viewBody = view?.findViewById(R.id.container_body)
        viewBottom = view?.findViewById(R.id.container_bottom)

        btnBottomLeft = view?.findViewById(R.id.button_bottom_left)
        btnBottomLeft?.setOnClickListener { handleButtonBottomLeftClick() }
        btnBottomRight = view?.findViewById(R.id.button_bottom_right)
        btnBottomRight?.setOnClickListener { handleButtonBottomRightClick() }

        imageGallery = ImageGalleryFragment()
        imageGallery?.setListener(object: ImageGalleryFragment.IListener {
            override fun onAddItemClick(sender: ImageGalleryFragment) {
                handleGalleryItemAdd()
            }
        })
        childFragmentManager.beginTransaction().replace(R.id.container_body, imageGallery!!).commit()

        activity?.let { activity ->

            cropImage = activity?.registerForActivityResult<CropImageContractOptions, CropImageView.CropResult>(CropImageContract()) { result ->
                if (result.isSuccessful) {
                    // Use the returned uri.
                    val uriContent = result.uriContent
                    val uriFilePath = result.getUriFilePath(activity) // optional usage
                } else {
                    // An error occurred.
                    val exception = result.error
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    private fun doImageAdd(imageName: String?) {
        imageName?.let { str ->
            val uri = Uri.parse(str)
            val parcelFileDescriptor: ParcelFileDescriptor? = App.appContext.contentResolver.openFileDescriptor(uri, "r")
            val fileDescriptor = parcelFileDescriptor?.fileDescriptor
            val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor?.close()
            val item = ImageGalleryItem(
                index = 0,
                caption = null,
                desc = null,
                url = null,
                image = image
            )
            imageGallery?.addItem(item)
        }

    }
    private fun handleActivityResult(activityResult: ActivityResult) {
        if (activityResult.resultCode == Activity.RESULT_OK) {
            val data: Intent? = activityResult.data
            val action = data?.getLongExtra(CustomActivity.PARAM_ACTION, -1L)
            when (action) {
                ImageGalleryFragment.ACTION_GALLERY_FRAGMENT -> doImageAdd(data?.getStringExtra(CropImageActivity.PARAM_IMAGE))
            }
        }

    }
    private fun handleGalleryItemAdd() {
        if (Helper.checkStoragePermission()) {
            imageDialog?.choiceImage(ImageGalleryFragment.ACTION_GALLERY_FRAGMENT)
        } else {
            imageDialog?.requestStoragePermission(null)
        }
    }
    private fun handleButtonBottomLeftClick() {
        if (Helper.checkStoragePermission()) {
            imageDialog?.choiceImage(1L)
        } else {
            imageDialog?.requestStoragePermission(null)
        }
    }
    private fun handleButtonBottomRightClick() {
        if (Helper.checkStoragePermission()) {
            imageDialog?.choiceImage(2L)
        } else {
            imageDialog?.requestStoragePermission(null)
        }
    }

    private fun handleFileUpload(action: Long, file: File) {
        if (action == ImageGalleryFragment.ACTION_GALLERY_FRAGMENT) {
            handleCropCustomActivity(file.toUri(), action)
        } else if (action == 1L) {
            handleCropActivity(file.toUri())
        } else if (action == 2L) {
            handleCropCustomActivity(file.toUri(), action)
        }
    }

    private fun handleCropCustomActivity(uri: Uri, action: Long) {
        val intent = Intent(this.activity, CropImageActivity::class.java)
        intent.putExtra(CropImageActivity.PARAM_IMAGE, uri.toString())
        intent.putExtra(CustomActivity.PARAM_ACTION, action)
        resultLauncher.launch(intent)
    }
    private fun handleCropActivity(uri: Uri) {
        var options = CropImageContractOptions(
            uri,
            CropImageOptions(
                imageSourceIncludeGallery = false,
                imageSourceIncludeCamera= true,
                guidelines = CropImageView.Guidelines.ON,
                cropperLabelTextColor = Color.BLACK,
                cropperLabelText = "cropperLabelText",
                activityBackgroundColor = Color.CYAN,
                toolbarColor = Color.MAGENTA,
                toolbarTitleColor = Color.YELLOW,
                toolbarBackButtonColor = Color.BLUE,
                toolbarTintColor = Color.GREEN
            )
        )
        cropImage?.launch(options)
    }
}