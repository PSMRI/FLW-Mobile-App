package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.u_win_forms

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope

import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.UwinListAdapter
import org.piramalswasthya.sakhi.databinding.FragmentUwinListBinding

import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity

@AndroidEntryPoint
class UwinListFragment : Fragment() {

    private var _binding: FragmentUwinListBinding? = null
    private val binding: FragmentUwinListBinding get() = _binding!!


    private val viewModel: UwinViewModel by viewModels()
    private lateinit var adapter: UwinListAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentUwinListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()

        binding.btnAddNew.setOnClickListener {
            val action = UwinListFragmentDirections
                .actionUwinListFragmentToUwinFragment(uwinId = 0)
            findNavController().navigate(action)
        }
    }

    private fun setupRecyclerView() {
        adapter = UwinListAdapter(UwinListAdapter.UwinClickListener { id ->
            val action = UwinListFragmentDirections
                .actionUwinListFragmentToUwinFragment(uwinId = id)
            findNavController().navigate(action)

        })
        binding.recyclerUwinList.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerUwinList.adapter = adapter
    }


    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uwinList.observe(viewLifecycleOwner) { list ->

                android.util.Log.d("UwinListFragment", "List updated with ${list?.size ?: 0} items")
                adapter.submitList(list?.toList())
                binding.tvNoData.visibility = if (list.isNullOrEmpty()) View.VISIBLE else View.GONE
            }
        }
    }


    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__village_level_form,
                getString(R.string.u_win_session_list)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}