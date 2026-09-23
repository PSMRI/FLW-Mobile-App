package org.piramalswasthya.sakhi.utils

import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import org.piramalswasthya.sakhi.R

object BellBadgeHelper {

    fun bind(
        menuItem: MenuItem,
        lifecycleOwner: LifecycleOwner,
        unreadCount: LiveData<Int>,
        onClick: () -> Unit
    ) {
        val actionView = menuItem.actionView ?: return
        val badge = actionView.findViewById<TextView>(R.id.tvBadge)

        actionView.setOnClickListener { onClick() }

        unreadCount.observe(lifecycleOwner) { count ->
            if (count != null && count > 0) {
                badge.text = if (count > 99) "99+" else count.toString()
                badge.visibility = View.VISIBLE
            } else {
                badge.visibility = View.GONE
            }
        }
    }
}