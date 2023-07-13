package org.piramalswasthya.sakhi.ui.login_activity.sign_in

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.FragmentSignInBinding
import org.piramalswasthya.sakhi.helpers.Languages.ASSAMESE
import org.piramalswasthya.sakhi.helpers.Languages.ENGLISH
import org.piramalswasthya.sakhi.helpers.Languages.HINDI
import org.piramalswasthya.sakhi.helpers.NetworkResponse
import org.piramalswasthya.sakhi.ui.login_activity.LoginActivity
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber
import java.security.SecureRandom
import java.security.spec.KeySpec
import java.util.Base64
import java.util.Random
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

//import javax.xml.bind.DatatypeConverter


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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnLogin.setOnClickListener {
            view.findFocus()?.let { view ->
                val imm =
                    activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(view.windowToken, 0)
            }

//            val text = "This is a test\n new lin emate" // Replace with your message.
//            val toNumber = "918179784993"
            // Replace with mobile phone number without +Sign or leading zeros, but with country code
            //Suppose your country is India and your phone number is “xxxxxxxxxx”, then you need to send “91xxxxxxxxxx”.
//            val intent = Intent(Intent.ACTION_VIEW)
//            intent.data = Uri.parse("http://api.whatsapp.com/send?phone=$toNumber&text=$text")
//            startActivity(intent)

//            val ivSizeInBytes =  128// Size of the IV in bytes
//            val keyLength = 256
//            val passPhrase = "Piramal12Piramal"
//
//            val iterations = 1989

//            val encryptedPwd = encrypt(passPhrase, "Test@123")

            viewModel.loginInClicked()
        }
//        var ee = 0
//        binding.ivNhmLogo.setOnLongClickListener {
//            if (ee == 0) {
//                Toast.makeText(context, "Madhav Rocks!#?/!", Toast.LENGTH_SHORT / 4).show()
//                ee++
//            }
//            true
//        }

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


        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is NetworkResponse.Idle -> {
                    var hasRememberMeUsername = false
                    var hasRememberMePassword = false
//                    var hasRememberMeState = false
                    viewModel.fetchRememberedUserName()?.let {
                        binding.etUsername.setText(it)
                        hasRememberMeUsername = true
                    }
                    viewModel.fetchRememberedPassword()?.let {
                        binding.etPassword.setText(it)
                        binding.cbRemember.isChecked = true
                        hasRememberMePassword = true
                    }
//                    viewModel.fetchRememberedState()?.let {
//                        binding.toggleStates.check(
//                            when (it) {
//                                "Bihar" -> binding.tbtnBihar.id
//                                "Assam" -> binding.tbtnAssam.id
//                                else -> throw IllegalStateException("State unknown $it")
//                            }
//                        )
//                        hasRememberMeState = true
//                    }
                    if (hasRememberMeUsername && hasRememberMePassword/* && hasRememberMeState*/) validateInput()
                }
                is NetworkResponse.Loading -> validateInput()
                is NetworkResponse.Error -> {
                    binding.pbSignIn.visibility = View.GONE
                    binding.clContent.visibility = View.VISIBLE
                    binding.tvError.text = state.message
                    binding.tvError.visibility = View.VISIBLE
                }
//                State.ERROR_SERVER -> {
//                    binding.pbSignIn.visibility = View.GONE
//                    binding.clContent.visibility = View.VISIBLE
//                    binding.tvError.text = getString(R.string.error_sign_in_timeout)
//                    binding.tvError.visibility = View.VISIBLE
//                }
//                State.ERROR_NETWORK -> {
//                    binding.pbSignIn.visibility = View.GONE
//                    binding.clContent.visibility = View.VISIBLE
//                    binding.tvError.text = getString(R.string.error_sign_in_disconnected_network)
//                    binding.tvError.visibility = View.VISIBLE
//                }
                is NetworkResponse.Success -> {
                    if (binding.cbRemember.isChecked) {
                        val username = binding.etUsername.text.toString()
                        val password = binding.etPassword.text.toString()
                        viewModel.rememberUser(
                            username, password, /*when (binding.toggleStates.checkedButtonId) {
                                binding.tbtnBihar.id -> "Bihar"
                                binding.tbtnAssam.id -> "Assam"
                                else -> throw IllegalStateException("Unknown State!! !! !!")
                            }*/
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
                        else SignInFragmentDirections.actionSignInFragmentToHomeActivity()
                    )
                    activity?.finish()
                }
            }
        }

    }

    private fun generateKey(salt: String, passPhrase: String, keySize: Int, iterationCount: Int): ByteArray {
        val saltBytes = hexStringToByteArray(salt)
        val keySpec = PBEKeySpec(passPhrase.toCharArray(), saltBytes, iterationCount, keySize)
        val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val secretKey = secretKeyFactory.generateSecret(keySpec)
        return secretKey.encoded
    }
    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02x".format(it) }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun encryptWithIvSalt(salt: String, iv: String, passPhrase: String, plainText: String): String {
        val key = generateKey(salt, passPhrase, 256, 1989)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(key, "AES")
        val ivParameterSpec = IvParameterSpec(hexStringToByteArray(iv))

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
        val cipherText = cipher.doFinal(plainText.toByteArray())

        return Base64.getEncoder().encodeToString(cipherText)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun encrypt(passPhrase: String, plainText: String): String {
        val ivSize = 128 // Assuming 16 bytes for the IV
        val keySize = 256 // bits

        val ivarr = ByteArray(ivSize / 8)
        Random().nextBytes(ivarr)
        val iv = ivarr.toHexString()

        val saltArr = ByteArray(keySize / 8)
        Random().nextBytes(saltArr)
        val salt = saltArr.toHexString()

        val ciphertext = encryptWithIvSalt(salt, iv, passPhrase, plainText)

        return salt + iv + ciphertext
    }


    private fun hexStringToByteArray(hexString: String): ByteArray {
        val len = hexString.length
        val byteArray = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            val hex = hexString.substring(i, i + 2)
            byteArray[i / 2] = hex.toInt(16).toByte()
            i += 2
        }
        return byteArray
    }
    fun bytesToHex(bytes: ByteArray): String {
        val hexChars = "0123456789ABCDEF"
        val result = StringBuilder(bytes.size * 2)

        for (byte in bytes) {
            val index = byte.toInt() and 0xFF
            result.append(hexChars[index shr 4])
            result.append(hexChars[index and 0x0F])
        }

        return result.toString()
    }


    private fun validateInput() {
//        val state = when (binding.toggleStates.checkedButtonId) {
//            binding.tbtnBihar.id -> "Bihar"
//            binding.tbtnAssam.id -> "Assam"
//            View.NO_ID -> {
//                stateUnselectedAlert.show()
//                return
//            }
//            else -> throw IllegalStateException("Two States!!")
//        }
        binding.clContent.visibility = View.INVISIBLE
        binding.pbSignIn.visibility = View.VISIBLE
        val username = binding.etUsername.text.toString()
        val password = binding.etPassword.text.toString()

        Timber.d("Username : $username \n Password : $password")
        viewModel.authUser(username, password, /*state*/)
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}