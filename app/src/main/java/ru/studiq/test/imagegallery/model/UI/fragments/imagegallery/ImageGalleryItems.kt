package ru.studiq.test.imagegallery.model.UI.fragments.imagegallery

import android.content.Context
import android.graphics.Bitmap
import com.google.gson.annotations.SerializedName

class ImageGalleryItems (
    @field:SerializedName("code") var code: String?,
    @field:SerializedName("caption") var caption: String?,
    @field:SerializedName("items") var items: MutableList<ImageGalleryItem>,
): java.io.Serializable {
    companion object {
        val empty: ImageGalleryItems
            get() = ImageGalleryItems(
                code = null,
                caption = null,
                items = mutableListOf()
            )
    }
    fun assignTo(target: ImageGalleryItems) {
        target.code = this.code
        target.caption = this.caption
        target.items.clear()
        this.items.forEach { item ->
            target.items.add(item)
        }
    }
    fun add(item: ImageGalleryItem) = apply  {
        this.items.add(item)
    }
}

class ImageGalleryItem (
    @field:SerializedName("index") var index: Int,
    @field:SerializedName("caption") var caption: String?,
    @field:SerializedName("desc") var desc: String?,
    @field:SerializedName("url") var url: String?,
    @field:SerializedName("image") var image: Bitmap?
): java.io.Serializable {
    companion object {
        val empty: ImageGalleryItem
            get() = ImageGalleryItem(
                index = -1,
                caption = null,
                desc = null,
                url = null,
                image = null
            )
    }
    fun assignTo(target: ImageGalleryItem) {
        target.index = this.index
        target.caption = this.caption
        target.desc = this.desc
        target.url = this.url
        target.image = this.image
    }
}
val ImageGalleryItems.Companion.DefaultItems: ImageGalleryItems
    get() = ImageGalleryItems(
        code = "",
        caption = "",
        items = mutableListOf()
    )
        .add(ImageGalleryItem(-100, null, null, null, null))