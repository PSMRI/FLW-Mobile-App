package org.piramalswasthya.sakhi.adapters


import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.model.PreviewItem

class PreviewAdapter(
    private val items: List<PreviewItem>,
    private val imageLoader: ((ImageView, Uri?) -> Unit)? = null
) : RecyclerView.Adapter<PreviewAdapter.PreviewVH>() {

    class PreviewVH(view: View) : RecyclerView.ViewHolder(view) {
        val tvLabel: TextView = view.findViewById(R.id.tvLabel)
        val tvValue: TextView = view.findViewById(R.id.tvValue)
        val ivImage: ImageView = view.findViewById(R.id.ivPreviewImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviewVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_preview_row, parent, false)
        return PreviewVH(v)
    }

    override fun onBindViewHolder(holder: PreviewVH, position: Int) {
        val it = items[position]
        holder.tvLabel.text = it.label
        if (it.isImage) {
            // show image view, hide text value
            holder.tvValue.visibility = View.GONE
            holder.ivImage.visibility = View.VISIBLE
            val uri = it.imageUri
            if (imageLoader != null) {
                imageLoader.invoke(holder.ivImage, uri)
            } else if (uri != null) {
                Glide.with(holder.ivImage)
                    .load(uri)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .centerCrop()
                    .into(holder.ivImage)
            } else {
                Glide.with(holder.ivImage).clear(holder.ivImage)
                holder.ivImage.setImageResource(R.drawable.ic_person)
            }
        } else {
            if (imageLoader == null) Glide.with(holder.ivImage).clear(holder.ivImage)
            holder.ivImage.visibility = View.GONE
            holder.tvValue.visibility = View.VISIBLE
            holder.tvValue.text = it.value
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onViewRecycled(holder: PreviewVH) {
        if (imageLoader == null) Glide.with(holder.ivImage).clear(holder.ivImage)
        super.onViewRecycled(holder)
    }
}
