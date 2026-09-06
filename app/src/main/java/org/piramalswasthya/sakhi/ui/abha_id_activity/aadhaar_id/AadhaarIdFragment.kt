package org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_id


import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.FragmentAadhaarIdBinding
import org.piramalswasthya.sakhi.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_id.AadhaarIdViewModel.State
import timber.log.Timber
import androidx.core.net.toUri
import org.piramalswasthya.sakhi.helpers.AnalyticsHelper
import org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_otp.AadhaarOtpFragmentDirections
import javax.inject.Inject


@AndroidEntryPoint
class AadhaarIdFragment : Fragment() {

    private var _binding: FragmentAadhaarIdBinding? = null
    private val binding: FragmentAadhaarIdBinding
        get() = _binding!!

    private lateinit var abhaMode: AadhaarIdViewModel.Abha
    val viewModel: AadhaarIdViewModel by viewModels({ requireActivity() })

    @Inject lateinit var analyticsHelper: AnalyticsHelper

    private lateinit var navController: NavController
    private val aadhaarNavController by lazy {
        val navHostFragment: NavHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host_fragment_find_abha) as NavHostFragment
        navHostFragment.navController
    }

    private var ABHA_URI = "https://phrsbx.abdm.gov.in/face-auth?txnId="

    private var ABHA_PACKAGE =
        if(BuildConfig.FLAVOR.contains("stag", ignoreCase = true) ||
            BuildConfig.FLAVOR.contains("uat", ignoreCase = true)) {
            ABHA_URI = "https://phrsbx.abdm.gov.in/face-auth?txnId="
            "in.ndhm.phr.debug"
        } else {
            ABHA_URI = "https://phr.abdm.gov.in/face-auth?txnId="
            "in.ndhm.phr"
        }

    private val pleaseWaitDialog by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle(resources.getString(R.string.please_wait))
            .setCancelable(false)
            .create()
    }

    private val alertDialog by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle(resources.getString(R.string.message))
            .setMessage("")
            .setCancelable(false)
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
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

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            viewModel.aadhaarVerificationTypeValues
        )

        binding.actvAadharVerificationDropdown.setAdapter(adapter)

        binding.createToggle.setOnClickListener {
            binding.searchToggle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_dark_shadow))
            binding.createToggle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_light_onSecondary))
            binding.createToggle.setBackgroundResource(R.drawable.background_rectangle_lightest_grey_20)
            binding.tilAadhaarVerifyDropdown.visibility = View.VISIBLE
            binding.actvAadharVerificationDropdown.visibility = View.VISIBLE
            binding.searchToggle.setBackgroundResource(0)
            binding.createToggle.setTypeface(resources.getFont(R.font.opensans_semibold))
            binding.searchToggle.setTypeface(resources.getFont(R.font.opensans_regular))
            binding.navHostFragmentFindAbha.visibility = View.GONE
            binding.navHostFragmentAadhaarId.visibility = View.VISIBLE

            viewModel.selectedNavToggle = "navHostFragmentAadhaarId"

        }
        binding.searchToggle.setOnClickListener {
            binding.createToggle.setBackgroundResource(0)
            binding.searchToggle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_light_onSecondary))
            binding.createToggle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_dark_shadow))
            binding.tilAadhaarVerifyDropdown.visibility = View.GONE
            binding.actvAadharVerificationDropdown.visibility = View.GONE

            binding.searchToggle.setBackgroundResource(R.drawable.background_rectangle_lightest_grey_20)
            binding.createToggle.setTypeface(resources.getFont(R.font.opensans_regular))
            binding.searchToggle.setTypeface(resources.getFont(R.font.opensans_semibold))
            binding.navHostFragmentAadhaarId.visibility = View.GONE
            binding.navHostFragmentFindAbha.visibility = View.VISIBLE
            viewModel.selectedNavToggle = "navHostFragmentFindAbha"

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
                    viewModel.setVerificationType("FA")
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
                        } else if (viewModel.verificationType.value == "FA") {
                            binding.clContentAadharId.visibility = View.VISIBLE
                            binding.pbLoadingAadharId.visibility = View.INVISIBLE
                            binding.clError.visibility = View.INVISIBLE
                            pleaseWaitDialog.setMessage(resources.getString(R.string.initializing))
                            pleaseWaitDialog.show()
                            Timber.d("FACE_TXN_GENERATED reached, txnId=${viewModel.txnId}")

                            val uri = (ABHA_URI + viewModel.txnId).toUri()
                            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                setPackage(ABHA_PACKAGE)
                            }

                            try {
                                startActivity(intent)
                                viewModel.submitCapturedPid()
                            } catch (e: ActivityNotFoundException) {
                                pleaseWaitDialog.dismiss()
                                e.printStackTrace()
                            }
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
                            viewModel.txnId, "", "", "",""
                        )
                    )
                }

                State.FACE_AUTH_PENDING -> {
                    pleaseWaitDialog.dismiss()
                    pleaseWaitDialog.setMessage(resources.getString(R.string.face_pending))
                    pleaseWaitDialog.show()
                }

                State.FACE_AUTH_VERIFIED -> {
                    pleaseWaitDialog.dismiss()
                    pleaseWaitDialog.setMessage(resources.getString(R.string.face_verified))
                    pleaseWaitDialog.show()
                }

                State.FACE_AUTH_SUCCESS -> {
                    pleaseWaitDialog.dismiss()
                    alertDialog.dismiss()
                    alertDialog.setTitle(resources.getString(R.string.message))
                    alertDialog.setMessage(resources.getString(R.string.face_success))
                    alertDialog.show()
                    viewModel.completeFaceEnrollment()
                }

                State.FACE_AUTH_FAILURE -> {
                    pleaseWaitDialog.dismiss()
                    alertDialog.dismiss()
                    alertDialog.setTitle(resources.getString(R.string.message))
                    alertDialog.setMessage(resources.getString(R.string.face_failed))
                    alertDialog.show()
                }

                State.ABHA_GENERATION_PENDING -> {
                    pleaseWaitDialog.dismiss()
                    alertDialog.dismiss()
                    pleaseWaitDialog.setTitle(resources.getString(R.string.message))
                    pleaseWaitDialog.setMessage(resources.getString(R.string.processing))
                    pleaseWaitDialog.show()
                }

                State.ABHA_GENERATION_FAILED -> {
                    pleaseWaitDialog.dismiss()
                    alertDialog.dismiss()
                    alertDialog.setTitle(resources.getString(R.string.message))
                    alertDialog.setMessage(viewModel.errorMessage.value.toString())
                    alertDialog.show()
                }

                State.ABHA_GENERATION_SUCCESS -> {
                    pleaseWaitDialog.dismiss()
                    alertDialog.dismiss()
                    val timestamp = System.currentTimeMillis()
                    analyticsHelper.logCustomTimestampEvent("create_abha_response",timestamp)
                    findNavController().navigate(
                        AadhaarOtpFragmentDirections.actionAadhaarOtpFragmentToCreateAbhaFragment(
                            viewModel.txnId, viewModel.name, viewModel.phrAddress, viewModel.abhaNumber,viewModel.abhaResponse
                        )
                    )
                }

                else -> {
                    pleaseWaitDialog.dismiss()
                    alertDialog.dismiss()
                    Toast.makeText(requireContext(), "Face Enrollment Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.navigateToAadhaarConsent.observe(viewLifecycleOwner){
            if (it==true){
                findNavController().navigate(
                    AadhaarIdFragmentDirections.actionAadhaarIdFragmentToAadhaarConsentFragment()
                )
                viewModel.navigateToAadhaarConsent(false)
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

    override fun onResume() {
        super.onResume()


        if (viewModel.selectedNavToggle == "navHostFragmentAadhaarId"){
            binding.searchToggle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_dark_shadow))
            binding.createToggle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_light_onSecondary))
            binding.createToggle.setBackgroundResource(R.drawable.background_rectangle_lightest_grey_20)
            binding.tilAadhaarVerifyDropdown.visibility = View.VISIBLE
            binding.actvAadharVerificationDropdown.visibility = View.VISIBLE
            binding.searchToggle.setBackgroundResource(0)
            binding.createToggle.setTypeface(resources.getFont(R.font.opensans_semibold))
            binding.searchToggle.setTypeface(resources.getFont(R.font.opensans_regular))
            binding.navHostFragmentFindAbha.visibility = View.GONE
            binding.navHostFragmentAadhaarId.visibility = View.VISIBLE

            viewModel.selectedNavToggle = "navHostFragmentAadhaarId"
        }else{
            binding.createToggle.setBackgroundResource(0)
            binding.searchToggle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_light_onSecondary))
            binding.createToggle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_dark_shadow))
            binding.tilAadhaarVerifyDropdown.visibility = View.GONE
            binding.actvAadharVerificationDropdown.visibility = View.GONE

            binding.searchToggle.setBackgroundResource(R.drawable.background_rectangle_lightest_grey_20)
            binding.createToggle.setTypeface(resources.getFont(R.font.opensans_regular))
            binding.searchToggle.setTypeface(resources.getFont(R.font.opensans_semibold))
            binding.navHostFragmentAadhaarId.visibility = View.GONE
            binding.navHostFragmentFindAbha.visibility = View.VISIBLE
            viewModel.selectedNavToggle = "navHostFragmentFindAbha"



        }
    }

}

