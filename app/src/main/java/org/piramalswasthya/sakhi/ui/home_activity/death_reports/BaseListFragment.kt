package org.piramalswasthya.sakhi.ui.home_activity.death_reports

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import androidx.navigation.fragment.findNavController
import org.piramalswasthya.sakhi.adapters.BenListAdapterForForm
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import javax.inject.Inject


abstract class BaseListFragment<T : ViewBinding> : Fragment() {


    protected open lateinit var prefDao: PreferenceDao


    protected abstract val viewModel: BaseListViewModel
    protected abstract val layoutInflaterBinding: (LayoutInflater, ViewGroup?, Boolean) -> T
    protected abstract val recyclerView: RecyclerView
    protected abstract val emptyStateView: View
    protected abstract val searchEditText: EditText
    protected abstract val iconResId: Int
    protected abstract val titleResId: Int
    protected open val isGeneralForm: Boolean = false
    protected abstract fun getNavDirection(hhId: Long, benId: Long): NavDirections

    private var _binding: T? = null
    protected val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = layoutInflaterBinding(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val benAdapter = BenListAdapterForForm(
            BenListAdapterForForm.ClickListener(
                { Toast.makeText(context, "Ben : $it clicked", Toast.LENGTH_SHORT).show() },
                { hhId, benId -> findNavController().navigate(getNavDirection(hhId, benId)) }
            ),
            resources.getString(R.string.mdsr_form),
            pref = prefDao,
            isGeneralForm = isGeneralForm
        )

        recyclerView.adapter = benAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.benList.collect { list ->
                emptyStateView.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                benAdapter.submitList(list)
            }
        }

        val searchTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.filterText(s?.toString() ?: "")
            }
        }

        searchEditText.setOnFocusChangeListener { view, focused ->
            if (focused) (view as EditText).addTextChangedListener(searchTextWatcher)
            else (view as EditText).removeTextChangedListener(searchTextWatcher)
        }
    }

    override fun onStart() {
        super.onStart()
        (activity as? HomeActivity)?.updateActionBar(iconResId, getString(titleResId))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
