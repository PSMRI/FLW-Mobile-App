package org.piramalswasthya.sakhi.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.piramalswasthya.sakhi.databinding.DialogUploadSourceBinding

class UploadSourceDialog : BottomSheetDialogFragment() {

    private var _binding: DialogUploadSourceBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val REQUEST_KEY = "upload_source_request"
        const val RESULT_SOURCE = "source"

        const val SOURCE_CAMERA = "camera"
        const val SOURCE_GALLERY = "gallery"
        const val SOURCE_DOCUMENT = "document"

        fun newInstance(): UploadSourceDialog {
            return UploadSourceDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogUploadSourceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.layoutCamera.setOnClickListener {
            setFragmentResult(REQUEST_KEY, bundleOf(RESULT_SOURCE to SOURCE_CAMERA))
            dismiss()
        }

        binding.layoutGallery.setOnClickListener {
            setFragmentResult(REQUEST_KEY, bundleOf(RESULT_SOURCE to SOURCE_GALLERY))
            dismiss()
        }

        binding.layoutDocuments.setOnClickListener {
            setFragmentResult(REQUEST_KEY, bundleOf(RESULT_SOURCE to SOURCE_DOCUMENT))
            dismiss()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}