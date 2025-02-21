package org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_id

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.FragmentAadhaarIdBinding
import org.piramalswasthya.sakhi.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_id.AadhaarIdViewModel.State


@AndroidEntryPoint
class AadhaarIdFragment : Fragment() {

    private var _binding: FragmentAadhaarIdBinding? = null
    private val binding: FragmentAadhaarIdBinding
        get() = _binding!!

    private lateinit var abhaMode: AadhaarIdViewModel.Abha
    val viewModel: AadhaarIdViewModel by viewModels({ requireActivity() })

    private lateinit var navController: NavController
    private val aadhaarNavController by lazy {
        val navHostFragment: NavHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host_fragment_find_abha) as NavHostFragment
        navHostFragment.navController
    }

    private val onBackPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAadhaarIdBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        binding.viewModel = viewModel
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )

        binding.createToggle.setOnClickListener {
            binding.createToggle.setBackgroundResource(R.drawable.background_rectangle_lightest_grey_20)
            binding.searchToggle.setBackgroundResource(0)
            binding.createToggle.setTypeface(resources.getFont(R.font.opensans_semibold))
            binding.searchToggle.setTypeface(resources.getFont(R.font.opensans_regular))
            binding.navHostFragmentFindAbha.visibility = View.GONE
            binding.navHostFragmentAadhaarId.visibility = View.VISIBLE

        }
        binding.searchToggle.setOnClickListener {
            binding.createToggle.setBackgroundResource(0)
            binding.searchToggle.setBackgroundResource(R.drawable.background_rectangle_lightest_grey_20)
            binding.createToggle.setTypeface(resources.getFont(R.font.opensans_regular))
            binding.searchToggle.setTypeface(resources.getFont(R.font.opensans_semibold))
            binding.navHostFragmentAadhaarId.visibility = View.GONE
            binding.navHostFragmentFindAbha.visibility = View.VISIBLE

        }

        binding.rgGovAsha.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_asha -> {
                    viewModel.setUserType("ASHA")
                    aadhaarNavController.navigate(R.id.aadhaarNumberAshaFragment)
                }

                R.id.rb_gov -> {
                    viewModel.setUserType("GOV")
                    aadhaarNavController.navigate(R.id.aadhaarNumberGovFragment)
                }
            }
        }

        binding.actvAadharVerificationDropdown.id = 0

        binding.actvAadharVerificationDropdown.setOnItemClickListener { _, _, i, _ ->
            when (i) {
                0 -> {
                    viewModel.setVerificationType("OTP")
                }

                1 -> {
                    viewModel.setVerificationType("FP")
                    binding.rgGovAsha.visibility = View.INVISIBLE
                }
            }
        }

        viewModel.abhaMode.observe(viewLifecycleOwner) { mode->
            abhaMode = mode
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                State.IDLE -> {}
                State.LOADING -> {
                    binding.clContentAadharId.visibility = View.INVISIBLE
                    binding.pbLoadingAadharId.visibility = View.VISIBLE
                    binding.clError.visibility = View.INVISIBLE
                }

                State.SUCCESS -> {
                    if (viewModel.userType.value == "ASHA") {
                        viewModel.resetState()
                        if (viewModel.verificationType.value == "OTP") {
                            findNavController().navigate(
                                AadhaarIdFragmentDirections.actionAadhaarIdFragmentToAadhaarOtpFragment(
                                    viewModel.txnId, viewModel.mobileNumber
                                )
                            )
                        } else if (viewModel.verificationType.value == "FP") {
                            findNavController().navigate(
                                AadhaarIdFragmentDirections.actionAadhaarIdFragmentToGenerateMobileOtpFragment(
                                    viewModel.txnId, viewModel.mobileNumber
                                )
                            )
                        }
                    }
                }

                State.ERROR_SERVER -> {
                    binding.pbLoadingAadharId.visibility = View.INVISIBLE
                    binding.clContentAadharId.visibility = View.VISIBLE
                    binding.clError.visibility = View.INVISIBLE
                }

                State.ERROR_NETWORK -> {
                    binding.clContentAadharId.visibility = View.INVISIBLE
                    binding.pbLoadingAadharId.visibility = View.INVISIBLE
                    binding.clError.visibility = View.VISIBLE
                }

                State.STATE_DETAILS_SUCCESS -> {
                    binding.clContentAadharId.visibility = View.VISIBLE
                    binding.pbLoadingAadharId.visibility = View.INVISIBLE
                }

                State.ABHA_GENERATED_SUCCESS -> {
                    findNavController().navigate(
                        AadhaarIdFragmentDirections.actionAadhaarIdFragmentToCreateAbhaFragment(
                            viewModel.txnId, "", "", ""
                        )
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as AbhaIdActivity).updateActionBar(
                R.drawable.ic__abha_logo_v1_24,
                getString(R.string.generate_abha)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}

