package org.piramalswasthya.sakhi.ui.asha_supervisor.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.FragmentSupervisorProfileBinding
import org.piramalswasthya.sakhi.ui.asha_supervisor.SupervisorActivity

@AndroidEntryPoint
class SupervisorProfileFragment : Fragment()  {

    private var _binding: FragmentSupervisorProfileBinding? = null
    private val binding: FragmentSupervisorProfileBinding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSupervisorProfileBinding.inflate(inflater, container, false)

        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
         _binding = null
          }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as SupervisorActivity).updateActionBar(
                R.drawable.logo_circle,
                getString(R.string.asha_profile)
            )
        }
    }
}