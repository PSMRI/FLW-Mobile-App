package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemPageNumBinding

class GetBenPageNumberAdapter(
    private val maxPageNumber: Int,
    private val clickListener: PageClickListener
) :
    RecyclerView.Adapter<GetBenPageNumberAdapter.PageNumberViewHolder>() {

    class PageNumberViewHolder private constructor(private val binding: RvItemPageNumBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): PageNumberViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemPageNumBinding.inflate(layoutInflater, parent, false)
                return PageNumberViewHolder(binding)
            }
        }

        fun bind(
            item: Int,
            clickListener: PageClickListener
        ) {
            binding.page = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) =
        PageNumberViewHolder.from(parent)

    override fun onBindViewHolder(holder: PageNumberViewHolder, position: Int) {
        holder.bind(position + 1, clickListener)
    }


    class PageClickListener(
        private val selectedPage: (page: Int) -> Unit,
    ) {
        fun onClickedPage(item: Int) = selectedPage(item)
    }

    override fun getItemCount(): Int {
        return maxPageNumber
    }
}