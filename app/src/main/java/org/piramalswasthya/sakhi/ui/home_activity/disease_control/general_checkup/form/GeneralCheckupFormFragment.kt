package org.piramalswasthya.sakhi.ui.home_activity.disease_control.general_checkup.form

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.dynamicAdapter.FormRendererAdapter
import org.piramalswasthya.sakhi.databinding.FragmentGeneralCheckupFormBinding
import org.piramalswasthya.sakhi.model.HealthProblemOwnerModule
import org.piramalswasthya.sakhi.model.HealthProblem
import org.piramalswasthya.sakhi.model.GeneralCheckupCache
import org.piramalswasthya.sakhi.model.ReferalCache
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.utils.safeNavigate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * Add Check-up form (General Check-up BRD FR3–FR7, FR14; prototype v0.5 §2.6–2.7). The fields are the
 * server-driven `general_check_up` form drawn by [FormRendererAdapter]; see [GeneralCheckupFormViewModel].
 */
@AndroidEntryPoint
class GeneralCheckupFormFragment : Fragment() {

    private var _binding: FragmentGeneralCheckupFormBinding? = null
    private val binding: FragmentGeneralCheckupFormBinding
        get() = _binding!!

    private val viewModel: GeneralCheckupFormViewModel by viewModels()

    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)

    private var formAdapter: FormRendererAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeneralCheckupFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.benName.observe(viewLifecycleOwner) { binding.tvBenName.text = it }
        viewModel.benDetails.observe(viewLifecycleOwner) { binding.tvBenDetails.text = it }

        setUpHistory()
        setUpForm()
        setUpPointer()

        binding.btnRefer.setOnClickListener { onReferClicked() }
        observeReferralResult()
    }

    private fun setUpHistory() {
        viewModel.history.observe(viewLifecycleOwner) { items ->
            binding.tvHistoryHeader.text = getString(R.string.gc_earlier_checkups_count, items.size)
            binding.tvHistoryBody.text =
                if (items.isEmpty()) getString(R.string.gc_no_earlier_checkups)
                else items.joinToString("\n\n") { viewModel.historyLine(it) }
        }
        binding.tvHistoryHeader.setOnClickListener {
            binding.tvHistoryBody.visibility =
                if (binding.tvHistoryBody.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }

    private fun setUpForm() {
        val adapter = FormRendererAdapter(
            mutableListOf(),
            formId = viewModel.formId,
            minVisitDate = Date(viewModel.minDate),
            maxVisitDate = Date(viewModel.maxDate),
            onValueChanged = { field, value ->
                // Keep the adapter's copy equal to the ViewModel's, so typing does not rebind the row.
                field.value = (value as? Collection<*>)
                    ?.mapNotNull { it?.toString() }?.toCollection(LinkedHashSet())
                    ?: value?.toString()
                viewModel.onFieldChanged(field.fieldId, value)?.let { kept ->
                    val owner = kept.ownerModule ?: return@let
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.gc_route_one_module, kept.label, ownerTitle(owner)),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
        formAdapter = adapter
        binding.rvForm.layoutManager = LinearLayoutManager(requireContext())
        binding.rvForm.adapter = adapter
        binding.btnRetry.setOnClickListener { viewModel.loadForm() }

        viewModel.formState.observe(viewLifecycleOwner) { state ->
            val ready = state == GeneralCheckupFormViewModel.FormState.Ready
            binding.pbForm.visibility = if (state == GeneralCheckupFormViewModel.FormState.Loading) View.VISIBLE else View.GONE
            val failed = state == GeneralCheckupFormViewModel.FormState.Unavailable ||
                    state == GeneralCheckupFormViewModel.FormState.Invalid
            binding.llFormUnavailable.visibility = if (failed) View.VISIBLE else View.GONE
            binding.tvFormUnavailable.setText(
                if (state == GeneralCheckupFormViewModel.FormState.Invalid) R.string.gc_form_invalid
                else R.string.gc_form_unavailable
            )
            // Debug builds only: the exact fix for the backend developer (selectable, to copy).
            val issues = viewModel.formIssues.takeIf { BuildConfig.DEBUG }
            binding.tvFormIssues.text = issues
            binding.tvFormIssues.visibility = if (issues != null) View.VISIBLE else View.GONE
            binding.rvForm.visibility = if (ready) View.VISIBLE else View.GONE
            binding.btnRefer.isEnabled = ready
        }
        viewModel.formChanged.observe(viewLifecycleOwner) { render() }
    }

    private fun setUpPointer() {
        binding.tvPointerCd.setOnClickListener {
            safeNavigate(
                GeneralCheckupFormFragmentDirections
                    .actionGeneralCheckupFormFragmentToCommunicableDiseasesGridFragment()
            )
        }
        binding.tvPointerNcd.setOnClickListener {
            safeNavigate(GeneralCheckupFormFragmentDirections.actionGeneralCheckupFormFragmentToNcdFragment())
        }
    }

    /** Redraws the fields and the pointer from the ViewModel. Priority is the server form's own field. */
    private fun render() {
        formAdapter?.let { adapter ->
            val fields = viewModel.visibleFields(::ownerTitle)
            // updateFields diffs by position only; a different field set (e.g. another type's
            // problem list in the same slot) needs a full redraw.
            if (adapter.getCurrentFields().map { it.fieldId } != fields.map { it.fieldId }) {
                adapter.updateFields(emptyList())
            }
            adapter.updateFields(fields)
        }

        val ready = viewModel.formState.value == GeneralCheckupFormViewModel.FormState.Ready
        binding.llPointer.visibility =
            if (ready && viewModel.selectedProblemType == null) View.VISIBLE else View.GONE
    }

    private fun ownerTitle(owner: HealthProblemOwnerModule) = getString(owner.titleRes())

    private fun onReferClicked() {
        viewModel.validate(requireContext(), ::ownerTitle)?.let { error ->
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            return
        }
        val owned = viewModel.ownedProblem
        if (owned != null) {
            showRoutingDialog(owned)
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            val duplicate = viewModel.findDuplicate()
            if (duplicate != null) showDuplicateDialog(duplicate) else openReferForm()
        }
    }

    private fun openReferForm() {
        safeNavigate(
            GeneralCheckupFormFragmentDirections.actionGeneralCheckupFormFragmentToNCDReferalForm(
                benId = viewModel.benId,
                referral = viewModel.referralReason(),
                referralType = GeneralCheckupFormViewModel.REFERRAL_TYPE,
                referralDate = viewModel.checkupDate ?: System.currentTimeMillis(),
                priority = viewModel.priority.name
            )
        )
    }

    private fun showRoutingDialog(problem: HealthProblem) {
        val owner = problem.ownerModule ?: return
        val moduleName = getString(owner.titleRes())
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.gc_route_title)
            .setMessage(getString(R.string.gc_route_msg, problem.label, moduleName))
            .setPositiveButton(getString(R.string.gc_route_continue, moduleName)) { _, _ -> routeTo(owner) }
            .setNegativeButton(R.string.gc_route_stay, null)
            .show()
    }

    private fun routeTo(owner: HealthProblemOwnerModule) {
        val directions = when (owner) {
            HealthProblemOwnerModule.MATERNAL ->
                GeneralCheckupFormFragmentDirections.actionGeneralCheckupFormFragmentToMotherCareFragment()
            HealthProblemOwnerModule.CHILD ->
                GeneralCheckupFormFragmentDirections.actionGeneralCheckupFormFragmentToChildCareFragment()
            HealthProblemOwnerModule.ECL ->
                GeneralCheckupFormFragmentDirections.actionGeneralCheckupFormFragmentToEligibleCoupleFragment()
            // The cataract form opens from the beneficiary's card in All Beneficiaries, which picks the eye.
            HealthProblemOwnerModule.CATARACT ->
                GeneralCheckupFormFragmentDirections.actionGeneralCheckupFormFragmentToAllBenFragment()
        }
        safeNavigate(directions)
    }

    /** BRD §8: one referral per beneficiary, problem and month. */
    private fun showDuplicateDialog(existing: GeneralCheckupCache) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.gc_dup_title)
            .setMessage(
                getString(R.string.gc_dup_msg, dateFormat.format(existing.referralDate))
            )
            .setPositiveButton(R.string.gc_dup_view) { _, _ ->
                safeNavigate(GeneralCheckupFormFragmentDirections.actionGeneralCheckupFormFragmentToHwcReferredListFragment())
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * The refer form hands the referral back through the saved-state handle, same as TB screening:
     * REFERRAL_RESULT keeps the referral, REFERRAL_DONE saves the check-up with it.
     * RESULT is observed first so it is delivered before DONE.
     */
    private fun observeReferralResult() {
        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<String>(KEY_REFERRAL_RESULT)
            ?.observe(viewLifecycleOwner) { json ->
                if (json.isNullOrBlank()) return@observe
                val referral = Gson().fromJson(json, ReferalCache::class.java)
                viewModel.addReferral(referral)
                findNavController().currentBackStackEntry?.savedStateHandle?.set(KEY_REFERRAL_RESULT, null)
            }

        findNavController()
            .currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<String>(KEY_REFERRAL_DONE)
            ?.observe(viewLifecycleOwner) { typeName ->
                if (typeName.isNullOrBlank()) return@observe
                viewModel.saveForm()
                findNavController().currentBackStackEntry?.savedStateHandle?.set(KEY_REFERRAL_DONE, null)
            }

        viewModel.saveState.observe(viewLifecycleOwner) { state ->
            when (state) {
                null -> return@observe
                GeneralCheckupFormViewModel.SaveState.Saved -> {
                    Toast.makeText(requireContext(), R.string.gc_referral_submitted, Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is GeneralCheckupFormViewModel.SaveState.Duplicate -> showDuplicateDialog(state.existing)
                GeneralCheckupFormViewModel.SaveState.Failed ->
                    Toast.makeText(requireContext(), R.string.gc_save_failed, Toast.LENGTH_LONG).show()
            }
            viewModel.onSaveStateHandled()
        }
    }

    private fun HealthProblemOwnerModule.titleRes() = when (this) {
        HealthProblemOwnerModule.MATERNAL -> R.string.gc_module_maternal
        HealthProblemOwnerModule.CHILD -> R.string.gc_module_child
        HealthProblemOwnerModule.ECL -> R.string.gc_module_ecl
        HealthProblemOwnerModule.CATARACT -> R.string.gc_module_cataract
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__general_op,
                getString(R.string.gc_add_checkup)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        formAdapter = null
        _binding = null
    }

    companion object {
        private const val KEY_REFERRAL_RESULT = "REFERRAL_RESULT"
        private const val KEY_REFERRAL_DONE = "REFERRAL_DONE"
    }
}
