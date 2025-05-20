package org.piramalswasthya.sakhi.ui.home_activity.child_care.adolescent_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.databinding.FragmentAdolescentHealthBinding
import org.piramalswasthya.sakhi.ui.home_activity.child_care.adolescent_list.form.AdolescentHealthFormViewModel

@AndroidEntryPoint
class AdolescentHealthFragment:Fragment() {
    private var _binding: FragmentAdolescentHealthBinding? = null
    private val viewModel: AdolescentHealthFormViewModel by viewModels()


    private val binding: FragmentAdolescentHealthBinding
        get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdolescentHealthBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
}