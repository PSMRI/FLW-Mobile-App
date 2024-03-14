package org.piramalswasthya.sakhi.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration.ben_age_more_15.NewBenRegG15ObjectFragment
import timber.log.Timber

class NewBenGenPagerAdapter(private val list: MutableList<String>, fragment: Fragment) :
    FragmentStateAdapter(fragment) {

    companion object {
        const val ARG_OBJECT_INDEX = "INDEX"
    }

    override fun getItemCount() = list.size

    override fun createFragment(position: Int): Fragment {
        val fragment = NewBenRegG15ObjectFragment()
        Timber.d("Adapter created Fragment $fragment")
        fragment.arguments = Bundle().apply {
            putInt(ARG_OBJECT_INDEX, position + 1)
        }
        return fragment
    }

    fun addPage(pageName: String) {
        list.add(pageName)
        notifyItemInserted(list.size - 1)
    }

    fun getPageName(position: Int): String {
        return list[position]
    }

    fun removePage(position: Int) {
        list.removeAt(position)
        notifyItemRemoved(position)
    }

}
