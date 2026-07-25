package org.piramalswasthya.sakhi.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R

/**
 * Full-screen notification panel. Added over `android.R.id.content` via [open] so it can be shown
 * from both HomeActivity (ASHA) and SupervisorActivity (Supervisor/CHO/ANM) without touching
 * either nav graph. Back button / close button pop it off the back stack.
 *
 * Phase 1: tap marks-as-read only (no navigation — deeplinking arrives in T17).
 */
@AndroidEntryPoint
class NotificationPanelFragment : Fragment() {

    private val viewModel: NotificationPanelViewModel by viewModels()
    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_notification_panel, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyStatusBarInset(view)

        val rv = view.findViewById<RecyclerView>(R.id.rvNotifications)
        val empty = view.findViewById<View>(R.id.layoutEmpty)
        val btnClearAll = view.findViewById<MaterialButton>(R.id.btnClearAll)

        // Toolbar back arrow and system back both just close the panel (never the app-exit prompt).
        view.findViewById<android.widget.ImageView>(R.id.iv_back).setOnClickListener { close() }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { close() }

        adapter = NotificationAdapter { item -> viewModel.markRead(item.notificationId) }
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        attachSwipeToDismiss(rv)

        viewModel.notifications.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            val isEmpty = list.isNullOrEmpty()
            empty.visibility = if (isEmpty) View.VISIBLE else View.GONE
            rv.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }

        btnClearAll.setOnClickListener { confirmClearAll() }
    }

    private fun applyStatusBarInset(root: View) {
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.updatePadding(top = top)
            insets
        }
        root.post {
            val top = ViewCompat.getRootWindowInsets(root)
                ?.getInsets(WindowInsetsCompat.Type.statusBars())?.top ?: 0
            if (root.paddingTop < top) root.updatePadding(top = top)
        }
        ViewCompat.requestApplyInsets(root)
    }

    private fun close() {
        parentFragmentManager.popBackStack()
    }

    private fun attachSwipeToDismiss(rv: RecyclerView) {
        val callback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val item = adapter.currentList.getOrNull(viewHolder.bindingAdapterPosition) ?: return
                viewModel.dismiss(item.notificationId)
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(rv)
    }

    private fun confirmClearAll() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.clear_notifications_confirm_title)
            .setMessage(R.string.clear_notifications_confirm_message)
            .setPositiveButton(R.string.notification_action_clear) { d, _ ->
                viewModel.clearAll()
                d.dismiss()
            }
            .setNegativeButton(R.string.notification_action_cancel) { d, _ -> d.dismiss() }
            .show()
    }

    companion object {
        const val TAG = "NotificationPanelFragment"

        /** Show the panel full-screen over the host activity's content. */
        fun open(fm: FragmentManager) {
            if (fm.isStateSaved || fm.findFragmentByTag(TAG) != null) return
            fm.beginTransaction()
                .add(android.R.id.content, NotificationPanelFragment(), TAG)
                .addToBackStack(TAG)
                .commit()
        }
    }
}