package org.piramalswasthya.sakhi.ui.login_activity.sign_in

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.FragmentSignInBinding
import org.piramalswasthya.sakhi.helpers.ImageUtils
import org.piramalswasthya.sakhi.helpers.Languages.ASSAMESE
import org.piramalswasthya.sakhi.helpers.Languages.ENGLISH
import org.piramalswasthya.sakhi.helpers.Languages.HINDI
import org.piramalswasthya.sakhi.helpers.NetworkResponse
import org.piramalswasthya.sakhi.helpers.UserType
import org.piramalswasthya.sakhi.ui.login_activity.LoginActivity
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class SignInFragment : Fragment() {

    @Inject
    lateinit var prefDao: PreferenceDao

    private var _binding: FragmentSignInBinding? = null
    private val binding: FragmentSignInBinding
        get() = _binding!!


    private val viewModel: SignInViewModel by viewModels()

    private val stateUnselectedAlert by lazy {
        AlertDialog.Builder(context).setTitle("State Missing")
            .setMessage("Please choose user registered state: ")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }.create()
    }

    private val userChangeAlert by lazy {
        var str = "previously logged in with " + viewModel.getLoggedInUser()?.userName + " do you" +
                " want to override? "
        viewModel.unprocessedRecordsCount.value?.let {
            if (it > 0) {
                str += "there are" + viewModel.unprocessedRecordsCount.value + " unprocessed records, wait till records are synced"
            }
        }

        MaterialAlertDialogBuilder(requireContext()).setTitle(resources.getString(R.string.logout))
            .setMessage(str)
            .setPositiveButton(resources.getString(R.string.yes)) { dialog, _ ->
                viewModel.unprocessedRecordsCount.value?.let {
                    if (it > 0) {
                        WorkerUtils.triggerAmritPushWorker(requireContext())
                    } else {
                        lifecycleScope.launch {
                            viewModel.logout()
                        }
                        ImageUtils.removeAllBenImages(requireContext())
                        WorkerUtils.cancelAllWork(requireContext())
                    }
                }
                dialog.dismiss()
            }.setNegativeButton(resources.getString(R.string.no)) { dialog, _ ->
                viewModel.updateState(NetworkResponse.Idle())
                dialog.dismiss()
            }.create()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnLogin.setOnClickListener {
            view.findFocus()?.let { view ->
                val imm =
                    activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(view.windowToken, 0)
            }
            viewModel.loginInClicked()
        }

        when (prefDao.getCurrentLanguage()) {
            ENGLISH -> binding.rgLangSelect.check(binding.rbEng.id)
            HINDI -> binding.rgLangSelect.check(binding.rbHindi.id)
            ASSAMESE -> binding.rgLangSelect.check(binding.rbAssamese.id)
        }

        binding.rgLangSelect.setOnCheckedChangeListener { _, i ->
            val currentLanguage = when (i) {
                binding.rbEng.id -> ENGLISH
                binding.rbHindi.id -> HINDI
                binding.rbAssamese.id -> ASSAMESE
                else -> ENGLISH
            }
            prefDao.saveSetLanguage(currentLanguage)
            val refresh = Intent(requireContext(), LoginActivity::class.java)
            //Timber.d("refresh Called!-${Locale.getDefault().language}-${savedLanguage.symbol}-")
            requireActivity().finish()
            startActivity(refresh)
            activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)


        }

        binding.rgUserType?.setOnCheckedChangeListener { _, i ->
            val currentUser = when (i) {
                binding.rbAsha?.id -> UserType.ASHA
                binding.rbAshaFacilator?.id -> UserType.FACILITATOR
                else -> UserType.ASHA
            }

        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is NetworkResponse.Idle -> {
                    binding.clContent.visibility = View.VISIBLE
                    binding.pbSignIn.visibility = View.INVISIBLE
                    var hasRememberMeUsername = false
                    var hasRememberMePassword = false
                    var hasRememberMeState = false
                    viewModel.fetchRememberedUserName()?.let {
                        binding.etUsername.setText(it)
                        hasRememberMeUsername = true
                    }
                    viewModel.fetchRememberedPassword()?.let {
                        binding.etPassword.setText(it)
                        binding.cbRemember.isChecked = true
                        hasRememberMePassword = true
                    }
                    if (hasRememberMeUsername && hasRememberMePassword) validateInput()
                }

                is NetworkResponse.Loading -> validateInput()
                is NetworkResponse.Error -> {
                    binding.pbSignIn.visibility = View.GONE
                    binding.clContent.visibility = View.VISIBLE
                    binding.tvError.text = state.message
                    binding.tvError.visibility = View.VISIBLE
                }

                is NetworkResponse.Success -> {
                    if (binding.cbRemember.isChecked) {
                        val username = binding.etUsername.text.toString()
                        val password = binding.etPassword.text.toString()
                        viewModel.rememberUser(
                            username, password
                        )
                    } else {
                        viewModel.forgetUser()
                    }
                    binding.clContent.visibility = View.INVISIBLE
                    binding.pbSignIn.visibility = View.VISIBLE
                    binding.tvError.visibility = View.GONE
                    WorkerUtils.triggerGenBenIdWorker(requireContext())
                    findNavController().navigate(
                        if (prefDao.getLocationRecord() == null) SignInFragmentDirections.actionSignInFragmentToServiceLocationActivity()
                        else SignInFragmentDirections.actionSignInFragmentToFacilitatorsHomeActivity()
                    )
                    activity?.finish()
                }
            }
        }

        viewModel.logoutComplete.observe(viewLifecycleOwner) {
            it?.let {
                if (it) validateInput()
            }
        }
    }

    /**
     * get username and password
     * validate with existing logged in user if exists else call login api
     */
    private fun validateInput() {
        binding.clContent.visibility = View.INVISIBLE
        binding.pbSignIn.visibility = View.VISIBLE
        val username = binding.etUsername.text.toString()
        val password = binding.etPassword.text.toString()

        val loggedInUser = viewModel.getLoggedInUser()

        if (loggedInUser == null) {
            viewModel.authUser(username, password)
        } else {
            if (loggedInUser.userName.equals(username.trim(), true)) {
                if (loggedInUser.password == password) {
                    viewModel.updateState(NetworkResponse.Success(loggedInUser))
                } else {
                    viewModel.updateState(NetworkResponse.Error("Invalid Password"))
                }
            } else {
                userChangeAlert.setCanceledOnTouchOutside(false)
                userChangeAlert.show()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}