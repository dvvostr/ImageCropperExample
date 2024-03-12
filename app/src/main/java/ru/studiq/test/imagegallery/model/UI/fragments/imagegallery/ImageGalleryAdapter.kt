package ru.studiq.test.imagegallery.model.UI.fragments.imagegallery

import android.R.attr.height
import android.R.attr.width
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import ru.studiq.test.imagegallery.R
import ru.studiq.test.imagegallery.model.App


class ImageGalleryAdapter(private val context: Context?, items: ImageGalleryItems) :
    RecyclerView.Adapter<ImageGalleryAdapter.ViewHolder>() {
    private val items: ImageGalleryItems
    private var listener: OnItemClickListener? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var frame: View
        var imageView: ImageView
        var progressBar: ProgressBar

        init {
            frame = view.findViewById(R.id.container_body)
            imageView = view.findViewById(R.id.image_view)
            progressBar = view.findViewById(R.id.progress_bar)
        }
    }

    init {
        this.items = items
    }

    interface OnItemClickListener {
        fun onItemClick(view: View?, obj: ImageGalleryItem?, position: Int)
    }

    fun setOnItemClickListener(itemClickListener: OnItemClickListener?) {
        listener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.activity_image_gallery_cell, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: ImageGalleryItem = items.items[position]
        holder.progressBar.visibility = View.VISIBLE
        holder.imageView.visibility = View.GONE
        if (item.index == -100) {
            holder.imageView.setImageDrawable(context?.getDrawable(R.drawable.icon_item_add_circle))
            val params = holder.imageView.layoutParams
            params.height = (64 * App.displayDensity).toInt()
            params.width = (64 * App.displayDensity).toInt()
            holder.imageView.setLayoutParams(params)
            holder.imageView.alpha = 0.55F
            holder.progressBar.visibility = View.GONE
            holder.imageView.visibility = View.VISIBLE
        } else if (item.image != null) {
            holder.imageView.setImageBitmap(item.image)
            holder.progressBar.visibility = View.GONE
            holder.imageView.visibility = View.VISIBLE
        } else if ((item.url ?: "").length > 0) {
            val img = holder.imageView
            val progressView = holder.progressBar
            Picasso.with(context)
                .load(item.url!!)
                .into(holder.imageView, object: Callback {
                    override fun onSuccess() {
                        progressView.visibility = View.GONE
                        img.visibility = View.VISIBLE
                    }
                    override fun onError() {
                        progressView.visibility = View.GONE
                        img.visibility = View.VISIBLE
                        img.setImageResource(R.drawable.icon_photo)
                    }
                })
        } else {
            holder.progressBar.visibility = View.GONE
            holder.imageView.visibility = View.VISIBLE
            holder.imageView.setImageResource(R.drawable.icon_photo)
        }
        holder.frame.setOnClickListener(View.OnClickListener { view ->
            listener?.onItemClick(view, item, position)
        })
    }

    override fun getItemCount(): Int {
        return items.items.size
    }
}

