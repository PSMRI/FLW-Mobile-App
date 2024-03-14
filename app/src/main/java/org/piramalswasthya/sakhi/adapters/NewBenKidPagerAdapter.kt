package org.piramalswasthya.sakhi.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class NewBenKidPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    companion object {
        const val ARG_OBJECT_INDEX = "INDEX"
    }

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
//        val fragment = NewBenRegL15ObjectFragment()
//        Timber.d("Adapter created Fragment $fragment")
//        fragment.arguments = Bundle().apply {
//            putInt(ARG_OBJECT_INDEX, position + 1)
//        }
        return Fragment()
    }
}
