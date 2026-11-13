package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.tb_screening.list

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.TbScreeningListAdapter
import org.piramalswasthya.sakhi.contracts.SpeechToTextContract
import org.piramalswasthya.sakhi.databinding.FragmentDisplaySearchRvButtonBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity

@AndroidEntryPoint
class TBScreeningListFragment : Fragment() {
    private var _binding: FragmentDisplaySearchRvButtonBinding? = null
    private val binding: FragmentDisplaySearchRvButtonBinding
        get() = _binding!!

    private val viewModel: TBScreeningListViewModel by viewModels()

    private val sttContract = registerForActivityResult(SpeechToTextContract()) { value ->
        val lowerValue = value.lowercase()
        binding.searchView.setText(lowerValue)
        binding.searchView.setSelection(lowerValue.length)
        viewModel.filterText(lowerValue)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDisplaySearchRvButtonBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnNextPage.visibility = View.GONE
        val benAdapter = TbScreeningListAdapter(
            TbScreeningListAdapter.ClickListener { item, addNew ->
                if (findNavController().currentDestination?.id == R.id.TBScreeningListFragment) {
                    if (addNew) {
                        findNavController().navigate(TBScreeningListFragmentDirections.actionTBScreeningListFragmentToTBScreeningFormFragment(item.ben.benId, -1, false))
                    } else showScreeningHistory(item)
                }
            }
        )
        binding.rvAny.adapter = benAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.benList.collect {
                if (it.isEmpty())
                    binding.flEmpty.visibility = View.VISIBLE
                else
                    binding.flEmpty.visibility = View.GONE
                benAdapter.submitList(it)
            }
        }

        binding.ibSearch.setOnClickListener { sttContract.launch(Unit) }
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
    }

    private fun showScreeningHistory(item: org.piramalswasthya.sakhi.model.BenWithTbScreeningDomain) {
        val dialog = BottomSheetDialog(requireContext())
        val content = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 32)
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadii = floatArrayOf(32f, 32f, 32f, 32f, 0f, 0f, 0f, 0f)
            }
        }
        item.tbRecords.sortedBy { it.visitDate }.forEachIndexed { index, record ->
            content.addView(TextView(requireContext()).apply {
                val date = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                    .format(java.util.Date(record.visitDate))
                text = "${if (index == 0) "1" else "${index + 1}"} TB screening ($date)"
                textSize = 16f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(Color.BLACK)
                setPadding(12, 18, 12, 18)
                setOnClickListener {
                    dialog.dismiss()
                    findNavController().navigate(TBScreeningListFragmentDirections.actionTBScreeningListFragmentToTBScreeningFormFragment(item.ben.benId, record.id, true))
                }
            })
        }
        dialog.setContentView(content)
        dialog.show()
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.tb_screening,
                getString(R.string.tb_screening_list)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
