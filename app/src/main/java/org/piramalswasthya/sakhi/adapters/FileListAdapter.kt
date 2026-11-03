package org.piramalswasthya.sakhi.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.LayoutFileViewBinding

class FileListAdapter(private var images: MutableList<Uri>) : RecyclerView.Adapter<FileListAdapter.FileVH>() {


    fun updateFileList(newList: MutableList<Uri>) {
        this.images.clear()
        this.images.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileVH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_file_view, parent, false)

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = LayoutFileViewBinding.inflate(layoutInflater, parent, false)
        return FileVH(binding)
    }

    override fun getItemCount() = images.size

    override fun onBindViewHolder(holder: FileVH, position: Int) {
        val file = images.get(position)
        Glide.with(holder.binding.ivPreview)
            .load(file)
            .placeholder(R.drawable.img_assam_logo)
            .error(R.drawable.img_assam_logo)
            .centerCrop()
            .into(holder.binding.ivPreview)
    }

    override fun onViewRecycled(holder: FileVH) {
        Glide.with(holder.binding.ivPreview).clear(holder.binding.ivPreview)
        super.onViewRecycled(holder)
    }

    class FileVH(var binding: LayoutFileViewBinding) : RecyclerView.ViewHolder(binding.root) {

    }
}
