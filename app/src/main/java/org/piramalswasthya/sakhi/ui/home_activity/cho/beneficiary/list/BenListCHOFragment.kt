package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.list

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.BenListAdapter
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.FragmentDisplaySearchRvButtonBinding
import org.piramalswasthya.sakhi.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class BenListCHOFragment : Fragment() {

    @Inject
    lateinit var prefDao: PreferenceDao

    private var _binding: FragmentDisplaySearchRvButtonBinding? = null

    private val binding: FragmentDisplaySearchRvButtonBinding
        get() = _binding!!

    private val viewModel: BenListCHOViewModel by viewModels()

    private val abhaDisclaimer by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle(resources.getString(R.string.beneficiary_abha_number))
            .setMessage("it")
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDisplaySearchRvButtonBinding.inflate(layoutInflater)
        return binding.root
    }

    private val onBackPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle back button press here
                Timber.d("handleOnBackPressed")
                findNavController().popBackStack()
                findNavController().navigateUp()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )
        val benAdapter = BenListAdapter(
            BenListAdapter.BenClickListener(
                { hhId, benId, isKid ->
//                    Toast.makeText(requireContext(), "ben clicked", Toast.LENGTH_SHORT).show()
                },
                {

                },
                { benId, hhId ->
                    checkAndGenerateABHA(benId)
                }, {}

                ),
//            showAbha = true,
            role = 1,
            pref = prefDao
        )
        binding.rvAny.adapter = benAdapter
        lifecycleScope.launch {
            viewModel.benList.collect {
                if (it.isEmpty())
                    binding.flEmpty.visibility = View.VISIBLE
                else
                    binding.flEmpty.visibility = View.GONE
                benAdapter.submitList(it)
            }
        }

        binding.btnNextPage.let {
            it.text = resources.getString(R.string.add_beneficiary)
            it.setBackgroundColor(
                resources.getColor(android.R.color.holo_orange_dark, context?.theme)
//                Color.rgb(252, 69, 19)
            )
        }
        binding.btnNextPage.setOnClickListener {
            findNavController().navigate(BenListCHOFragmentDirections.actionBenListCHOFragmentToBenRegisterCHOFragment())
        }
        val searchTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                viewModel.filterText(p0?.toString() ?: "")
            }

        }
        binding.searchView.setOnFocusChangeListener { searchView, b ->
            if (b)
                (searchView as EditText).addTextChangedListener(searchTextWatcher)
            else
                (searchView as EditText).removeTextChangedListener(searchTextWatcher)

        }

        viewModel.abha.observe(viewLifecycleOwner) {
            it.let {
                if (it != null) {
                    abhaDisclaimer.setMessage(it)
                    abhaDisclaimer.show()
                }
            }
        }

        viewModel.benRegId.observe(viewLifecycleOwner) {
            if (it != null) {
                val intent = Intent(requireActivity(), AbhaIdActivity::class.java)
                intent.putExtra("benId", viewModel.benId.value)
                intent.putExtra("benRegId", it)
                requireActivity().startActivity(intent)
                viewModel.resetBenRegId()
            }

        }
    }

    private fun checkAndGenerateABHA(benId: Long) {
        viewModel.fetchAbha(benId)
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(R.drawable.ic__ben)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}