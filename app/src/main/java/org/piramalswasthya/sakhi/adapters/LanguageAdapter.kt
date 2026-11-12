package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.ItemLanguageBinding
import org.piramalswasthya.sakhi.model.Language

class LanguageAdapter(
    private val items: List<Language>,
    private val onItemClick: (Language) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemLanguageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemLanguageBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.binding.tvLangFirstWord.text = item.lanFirstWord
        holder.binding.tvLangName.text = item.lanName

//        holder.binding.tvLangFirstWord.setTextColor(
//            if (item.isSelected)
//                ContextCompat.getColor(holder.itemView.context, R.color.md_theme_light_primary)
//            else
//                ContextCompat.getColor(holder.itemView.context, R.color.md_theme_light_shadow)
//        )

        // Selected/Unselected circle background
//        holder.binding.tvLangFirstWord.background =
//            if (item.isSelected) item.lanSelectedView
//            else item.lanUnselectedView

//        holder.binding.tvLangName.setTextColor(
//            if (item.isSelected)
//                ContextCompat.getColor(holder.itemView.context, R.color.md_theme_light_primary)
//            else
//                ContextCompat.getColor(holder.itemView.context, R.color.md_theme_light_shadow)
//        )

        // Redesign (Figma M2.19-04 "Radio option"): selected tile = primary-subtle + 2dp primary border
        // (bg_ds_option_activated via isActivated), letter circle primary-container / #EEF1F5
        val ctx = holder.itemView.context
        holder.binding.root.isActivated = item.isSelected
        holder.binding.tvLangFirstWord.backgroundTintList = ContextCompat.getColorStateList(
            ctx,
            if (item.isSelected) R.color.ds_primary_container else R.color.ds_surface_container_high
        )
        holder.binding.tvLangFirstWord.setTextColor(
            ContextCompat.getColor(ctx, if (item.isSelected) R.color.ds_on_primary_container else R.color.ds_on_surface)
        )
        holder.binding.tvLangName.setTextColor(
            ContextCompat.getColor(ctx, if (item.isSelected) R.color.ds_primary else R.color.ds_on_surface)
        )

        holder.binding.root.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = items.size
}