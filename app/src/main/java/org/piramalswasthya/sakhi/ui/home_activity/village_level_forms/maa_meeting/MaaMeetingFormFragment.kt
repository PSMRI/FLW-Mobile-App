package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.maa_meeting

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.FormInputAdapter
import org.piramalswasthya.sakhi.databinding.FragmentMaaMeetingFormBinding
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.ui.checkFileSize
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.utils.HelperUtil.getMimeFromUri

@AndroidEntryPoint
class MaaMeetingFormFragment : Fragment() {

    private var _binding: FragmentMaaMeetingFormBinding? = null

    private val binding : FragmentMaaMeetingFormBinding
        get() = _binding!!

    private val viewModel: MaaMeetingFormViewModel by viewModels()

    private var lastFileFormId: Int = -1
    private val args: MaaMeetingFormFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMaaMeetingFormBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.id = args.id

        if (viewModel.id > 0) {
            binding.btnSubmit.visibility = View.GONE
        }else{
            binding.btnSubmit.visibility = View.VISIBLE
        }

        viewModel.recordExists.observe(viewLifecycleOwner) { exists ->
            val adapter = FormInputAdapter(
                formValueListener = FormInputAdapter.FormValueListener { id, index ->
                    viewModel.updateListOnValueChanged(id, index)
                },
                selectImageClickListener = FormInputAdapter.SelectUploadImageClickListener { formId ->
                    lastFileFormId = formId
                    showFileChooser()
                },
                isEnabled = !exists,
                viewDocumentListner = FormInputAdapter.ViewDocumentOnClick { formId ->
                    val element = (binding.form.rvInputForm.adapter as? FormInputAdapter)
                        ?.currentList?.firstOrNull { it.id == formId }

                    element?.value?.let { uriStr ->
                        val uri = Uri.parse(uriStr)
                        val mime =
                            requireContext().contentResolver.getType(uri) ?: getMimeFromUri(uri)
                        try {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, mime)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Issue No Image Found", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            )

            binding.form.rvInputForm.adapter = adapter

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.formList.collect {
                        list ->
                    adapter.submitList(list)
                }
            }

            binding.btnSubmit.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    if (validateBeforeSubmit(adapter.currentList)) {
                        binding.btnSubmit.isEnabled = false
                        viewModel.saveForm()
                        Toast.makeText(requireContext(), "Data Saved Successfully.", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

    private fun validateBeforeSubmit(list: List<FormElement>): Boolean {
        var valid = true
        val date = list.firstOrNull { it.id == 1 }
        val place = list.firstOrNull { it.id == 2 }
        val participants = list.firstOrNull { it.id == 3 }

        if (date?.value.isNullOrEmpty()) { date?.errorText = getString(R.string.form_input_empty_error); valid = false }
        if (place?.value.isNullOrEmpty()) { place?.errorText = getString(R.string.form_input_empty_error); valid = false }
        if (participants?.value.isNullOrEmpty()) { participants?.errorText = getString(R.string.form_input_empty_error); valid = false }

        val uploads = list.filter { it.id in 10..14 }
        val uploadCount = uploads.count { !it.value.isNullOrEmpty() }
        if (uploadCount < 2) {
            uploads.forEach { if (it.value.isNullOrEmpty()) it.errorText = getString(R.string.form_input_empty_error) }
            valid = false
        } else {
            uploads.forEach { it.errorText = null }
        }

        // Quarter rule checked on submit via Room

        if (!valid) {
            (binding.form.rvInputForm.adapter as? FormInputAdapter)?.notifyDataSetChanged()
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.alert))
                .setMessage("Please fill all mandatory fields before submitting.")
                .setPositiveButton(R.string.ok, null)
                .show()
        }
        return valid
    }

    private fun showFileChooser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png", "application/pdf"))
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent, 1010)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1010 && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                // Persist permission so we can view later
                try {
                    requireContext().contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {}

                val mime = requireContext().contentResolver.getType(uri) ?: getMimeFromUri(uri)
                val allowed = setOf("image/jpeg", "image/png", "application/pdf")
                if (!allowed.contains(mime)) {
                    Toast.makeText(requireContext(), "Only JPEG, PNG, PDF allowed", Toast.LENGTH_LONG).show()
                    return
                }

                if (checkFileSize(uri, requireContext())) {
                    Toast.makeText(requireContext(), getString(R.string.file_size), Toast.LENGTH_LONG).show()
                    return
                }
                viewModel.setUploadUriFor(lastFileFormId, uri)
                (binding.form.rvInputForm.adapter as? FormInputAdapter)?.notifyDataSetChanged()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__village_level_form,
                getString(R.string.maa_meeting)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}