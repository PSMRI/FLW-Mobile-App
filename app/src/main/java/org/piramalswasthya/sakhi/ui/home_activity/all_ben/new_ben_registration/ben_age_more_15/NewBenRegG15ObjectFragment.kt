package org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration.ben_age_more_15

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.adapters.FormInputAdapter
import org.piramalswasthya.sakhi.adapters.NewBenKidPagerAdapter
import org.piramalswasthya.sakhi.databinding.FragmentInputFormPageBinding
import org.piramalswasthya.sakhi.model.FormInput
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class NewBenRegG15ObjectFragment : Fragment() {

    private var _binding: FragmentInputFormPageBinding? = null
    private val binding: FragmentInputFormPageBinding
        get() = _binding!!

    private val viewModel: NewBenRegG15ViewModel by viewModels({ requireParentFragment() })


    private var latestTmpUri: Uri? = null

    private var latestImageForm: FormInput? = null

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                latestTmpUri?.let { uri ->
                    latestImageForm?.value?.value = uri.toString()
                    binding.inputForm.rvInputForm.apply {
                        val adapter = this.adapter as FormInputAdapter
                        latestImageForm?.errorText = null
                        adapter.notifyItemChanged(0)
                    }
                    Timber.d("Image saved at @ $uri")
                }
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInputFormPageBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pageNumber = arguments?.getInt(NewBenKidPagerAdapter.ARG_OBJECT_INDEX)
            ?: throw IllegalStateException("No argument passed to viewpager object!")
        viewModel.recordExists.observe(viewLifecycleOwner) {
            it?.let {
                when (pageNumber) {
                    1 -> {
                        val adapter =
                            FormInputAdapter(FormInputAdapter.ImageClickListener { form ->
                                latestImageForm = form
                                takeImage()

                            }, isEnabled = !it)
                        binding.inputForm.rvInputForm.adapter = adapter
                        lifecycleScope.launch {
                            adapter.submitList(viewModel.getFirstPage(adapter))
                        }
                    }
                    2 -> {

                        val adapter = FormInputAdapter(isEnabled = !it)
                        binding.inputForm.rvInputForm.adapter = adapter
                        adapter.submitList(viewModel.getSecondPage(adapter))
                    }
                    3 -> {

                        val adapter = FormInputAdapter(isEnabled = !it)
                        binding.inputForm.rvInputForm.adapter = adapter
                        adapter.submitList(viewModel.getThirdPage(adapter))
                    }
                }
            }
        }


    }

    private fun takeImage() {
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                latestTmpUri = uri
                takePicture.launch(uri)
            }
        }
    }

    private fun getTmpFileUri(): Uri {
        val tmpFile =
            File.createTempFile("tmp_image_file", ".jpeg", requireActivity().cacheDir).apply {
                createNewFile()
                deleteOnExit()
            }
        return FileProvider.getUriForFile(
            requireContext(),
            "${BuildConfig.APPLICATION_ID}.provider",
            tmpFile
        )
    }

    fun validate(): Boolean {
        val result = binding.inputForm.rvInputForm.adapter?.let {
            (it as FormInputAdapter).validateInput()
        }
        Timber.d("Validation : $result")
        return if (result == -1)
            true
        else {
            if (result != null) {
                binding.inputForm.rvInputForm.scrollToPosition(result)
            }
            false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
