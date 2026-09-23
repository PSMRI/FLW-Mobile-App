package org.piramalswasthya.sakhi.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
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
import org.piramalswasthya.sakhi.model.NotificationDomain
import org.piramalswasthya.sakhi.model.NotificationNavTarget
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import timber.log.Timber

/**
 * Full-screen notification panel. Added over `android.R.id.content` via [open] so it can be shown
 * from both HomeActivity (ASHA) and SupervisorActivity (Supervisor/CHO/ANM) without touching
 * either nav graph. Back button / close button pop it off the back stack.
 *
 * Tap always marks-as-read; it additionally deeplinks for the nav_id targets handled in
 * [onNotificationTapped] (currently INCENTIVE_SCREEN → ASHA's `IncentivesFragment`). Unhandled/
 * unknown nav_id values, or a host activity that doesn't support a given target, fall through to
 * mark-read only — never a crash.
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

        applyToolbarColor(view)
        applyStatusBarInset(view)

        val rv = view.findViewById<RecyclerView>(R.id.rvNotifications)
        val empty = view.findViewById<View>(R.id.layoutEmpty)
        val btnClearAll = view.findViewById<MaterialButton>(R.id.btnClearAll)

        // Toolbar back arrow and system back both just close the panel (never the app-exit prompt).
        view.findViewById<android.widget.ImageView>(R.id.iv_back).setOnClickListener { close() }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { close() }

        adapter = NotificationAdapter { item -> onNotificationTapped(item) }
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

    private fun applyToolbarColor(root: View) {
        val colorRes = arguments?.getInt(ARG_TOOLBAR_COLOR, DEFAULT_TOOLBAR_COLOR)
            ?: DEFAULT_TOOLBAR_COLOR
        val color = ContextCompat.getColor(requireContext(), colorRes)
        root.setBackgroundColor(color)
        root.findViewById<View>(R.id.appBarLayout).setBackgroundColor(color)
    }

    private fun applyStatusBarInset(root: View) {
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.updatePadding(top = top)
            insets
        }
        ViewCompat.requestApplyInsets(root)
    }

    /**
     * Pops this panel by name rather than blindly popping the top entry: a second tap (or a back
     * press landing on an already-closing panel) would otherwise pop whatever else sits on the
     * activity's back stack.
     */
    private fun close() {
        parentFragmentManager.popBackStack(TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    /**
     * Tap always marks-as-read regardless of the routing target. If the target has a handler for the
     * current host, the panel is closed and the host navigates; otherwise this is a no-op beyond
     * mark-read (unknown target, or a role/host that doesn't support it).
     *
     * ASHA (HomeActivity) has exactly one incentive screen, so any incentive/claim notification
     * routes there — including `INCENTIVE_APPROVAL`, which the backend also sends to ASHAs, and
     * unrecognised target keys on an otherwise incentive-related notification
     * ([NotificationDomain.isIncentiveRelated]). Without that fallback a naming mismatch between
     * `navId`, `redirect` and the mobile enum silently does nothing on tap.
     */
    private fun onNotificationTapped(item: NotificationDomain) {
        viewModel.markRead(item.notificationId)
        val isAshaHost = activity is HomeActivity
        when (NotificationNavTarget.fromNavId(item.navTarget)) {
            NotificationNavTarget.INCENTIVE_SCREEN -> navigateToIncentivesScreen()
            // Supervisor-side verification screen isn't wired yet; on an ASHA device the same
            // notification means "your claim was acted on" → the ASHA incentives screen.
            NotificationNavTarget.INCENTIVE_APPROVAL -> if (isAshaHost) navigateToIncentivesScreen()
            NotificationNavTarget.NONE -> {
                if (isAshaHost && item.isIncentiveRelated) {
                    Timber.d("Unrecognised nav target '${item.navTarget}'; routing incentive notification to IncentivesFragment")
                    navigateToIncentivesScreen()
                }
            }
        }
    }

    private fun navigateToIncentivesScreen() {
        val homeActivity = activity as? HomeActivity
        if (homeActivity == null) {
            Timber.w("INCENTIVE_SCREEN nav_id tapped but host is not HomeActivity; skipping navigation")
            return
        }
        close()
        homeActivity.navigateToIncentivesFromNotification()
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

        private const val ARG_TOOLBAR_COLOR = "toolbar_color"

        @ColorRes
        private val DEFAULT_TOOLBAR_COLOR = R.color.seed

        fun open(fm: FragmentManager, @ColorRes toolbarColor: Int = DEFAULT_TOOLBAR_COLOR) {
            if (fm.isStateSaved || fm.findFragmentByTag(TAG) != null) return
            val fragment = NotificationPanelFragment().apply {
                arguments = bundleOf(ARG_TOOLBAR_COLOR to toolbarColor)
            }
            fm.beginTransaction()
                .add(android.R.id.content, fragment, TAG)
                .addToBackStack(TAG)
                .commit()
        }
    }
}