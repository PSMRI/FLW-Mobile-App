package org.piramalswasthya.sakhi.ui.asha_supervisor.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.FragmentSupervisorProfileBinding
import org.piramalswasthya.sakhi.helpers.ImageUtils
import org.piramalswasthya.sakhi.ui.login_activity.LoginActivity
import org.piramalswasthya.sakhi.work.WorkerUtils
import kotlin.getValue
import org.piramalswasthya.sakhi.ui.asha_supervisor.SupervisorActivity

@AndroidEntryPoint
class SupervisorProfileFragment : Fragment()  {

    private var _binding: FragmentSupervisorProfileBinding? = null
    private val binding: FragmentSupervisorProfileBinding
        get() = _binding!!

    private val viewModel: SupervisorProfileViewModel by viewModels()

    private val logoutAlert by lazy {
        var str = ""
        str += resources.getString(R.string.are_you_sure_to_logout)

        MaterialAlertDialogBuilder(requireActivity()).setTitle(resources.getString(R.string.logout))
            .setMessage(str)
            .setPositiveButton(resources.getString(R.string.yes)) { dialog, _ ->
                viewModel.logout()
                ImageUtils.removeAllBenImages(requireActivity())
                WorkerUtils.cancelAllWork(requireActivity())
                dialog.dismiss()
            }.setNegativeButton(resources.getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }.create()
    }


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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvValue.setText(viewModel.getUserMobile())
        binding.emailtvValue.setText(viewModel.getUserEmail())
        binding.supervisorId.setText("EMP. ID : ${ viewModel.getEmpId().toString()}")
        binding.supervisorName.setText(viewModel.getSuperVisorname())
        binding.subName.setText(viewModel.getSuperVisorSubname())
        binding.districtvValue.text = viewModel.getDistrict()
        binding.blocktvValue.text = viewModel.getBlock()
        binding.subcentertvValue.text = viewModel.getSubcenter()
        binding.logoutlayout.setOnClickListener {

            logoutAlert.show()
        }

        viewModel.navigateToLoginPage.observe(viewLifecycleOwner) {
            viewModel.navigateToLoginPage.observe(this) {
                if (it) {
                    startActivity(Intent(requireActivity(), LoginActivity::class.java))
                    viewModel.navigateToLoginPageComplete()
                    activity?.finish()
                }
            }
        }
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