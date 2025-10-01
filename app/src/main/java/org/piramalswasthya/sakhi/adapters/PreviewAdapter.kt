package org.piramalswasthya.sakhi.adapters


import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.model.PreviewItem

class PreviewAdapter(
    private val items: List<PreviewItem>,
    private val imageLoader: ((ImageView, Uri?) -> Unit)? = null // injectable for production (Glide/Picasso)
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
            imageLoader?.invoke(holder.ivImage, it.imageUri) ?: run {
                // Minimal default loader (URI -> ImageView). Use Glide/Picasso in prod.
                try {
                    it.imageUri?.let { uri ->
                        holder.ivImage.setImageURI(uri)
                    } ?: holder.ivImage.setImageResource(android.R.color.darker_gray)
                } catch (e: Exception) {
                    holder.ivImage.setImageResource(android.R.color.darker_gray)
                }
            }
        } else {
            holder.ivImage.visibility = View.GONE
            holder.tvValue.visibility = View.VISIBLE
            holder.tvValue.text = it.value
        }
    }

    override fun getItemCount(): Int = items.size
}
