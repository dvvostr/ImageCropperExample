package ru.studiq.test.imagegallery.model.UI.fragments.imagegallery

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.studiq.test.imagegallery.R
import ru.studiq.test.imagegallery.model.UI.fragments.CustomFragment
import ru.studiq.test.imagegallery.model.classes.Helper

class ImageGalleryFragment : CustomFragment() {
    interface IListener {
        fun onAddItemClick(sender: ImageGalleryFragment) {}
    }

    companion object {
        const val ACTION_GALLERY_FRAGMENT = 2441L
        @JvmStatic
        fun newInstance() =
            ImageGalleryFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    private var listener: IListener? = null
    private var items: ImageGalleryItems = ImageGalleryItems.Companion.DefaultItems
    private var listView: RecyclerView? = null
    private var adapter: ImageGalleryAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_image_gallery, container, false)
        initialize(view)
        return view
    }

    override fun initialize(view: View?) {
        super.initialize(view)
        listView = view?.findViewById(R.id.list_view)
        adapter = ImageGalleryAdapter(activity, items)
        adapter?.setOnItemClickListener(object: ImageGalleryAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, obj: ImageGalleryItem?, position: Int) {
                handleAdapterItemClick(obj, position)
            }
        })
        val layoutManager: LinearLayoutManager = GridLayoutManager(activity, 3)
        listView?.setLayoutManager(layoutManager)
        listView?.setHasFixedSize(true)
        listView?.setItemAnimator(DefaultItemAnimator())
        listView?.setAdapter(adapter)

        listView?.setNestedScrollingEnabled(true)
    }
    fun setListener(listener: IListener) {
        this.listener = listener
    }
    fun load(items: ImageGalleryItems) {
        items.assignTo(this.items)
        adapter?.notifyDataSetChanged()
    }
    fun addItem(item: ImageGalleryItem?) {
        item?.let { item ->
            items.add(item)
            adapter?.notifyDataSetChanged()
        }
    }
    private fun handleAdapterItemClick(item: ImageGalleryItem?, position: Int) {
        item?.let { item ->
            if (item.index == -100) {
                listener?.onAddItemClick(this)
            }
        }
    }


}
