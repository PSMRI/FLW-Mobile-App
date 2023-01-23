package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavDirections
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemIconGridBinding
import org.piramalswasthya.sakhi.model.Icon

class IconGridAdapter(private val iconList : List<Icon>, private val clickListener: GridIconClickListener) : RecyclerView.Adapter<IconGridAdapter.IconViewHolder>() {



    class IconViewHolder private constructor(private val binding : RvItemIconGridBinding) : RecyclerView.ViewHolder(binding.root) {

        companion object{
            fun from(parent: ViewGroup) : IconViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemIconGridBinding.inflate(layoutInflater,parent,false)
                return IconViewHolder(binding)
            }
        }

        fun bind(item: Icon, clickListener: GridIconClickListener){
            binding.homeIcon = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        IconViewHolder.from(parent)

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.bind(iconList[position],clickListener)
    }

    override fun getItemCount() = iconList.size

    class GridIconClickListener(val selectedListener: (dest : NavDirections) -> Unit) {
        fun onClicked(icon : Icon) = selectedListener(icon.navAction)

    }
}