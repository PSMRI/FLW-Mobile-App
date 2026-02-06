package org.piramalswasthya.sakhi.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.piramalswasthya.sakhi.databinding.DialogUploadSourceBinding

class UploadSourceDialog : BottomSheetDialogFragment() {

    private var _binding: DialogUploadSourceBinding? = null
    private val binding get() = _binding!!
    
    private var onCameraSelected: (() -> Unit)? = null
    private var onGallerySelected: (() -> Unit)? = null
    private var onDocumentSelected: (() -> Unit)? = null

    companion object {
        fun newInstance(
            onCameraSelected: () -> Unit,
            onGallerySelected: () -> Unit,
            onDocumentSelected: () -> Unit
        ): UploadSourceDialog {
            return UploadSourceDialog().apply {
                this.onCameraSelected = onCameraSelected
                this.onGallerySelected = onGallerySelected
                this.onDocumentSelected = onDocumentSelected
            }
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
            onCameraSelected?.invoke()
            dismiss()
        }
        
        binding.layoutGallery.setOnClickListener {
            onGallerySelected?.invoke()
            dismiss()
        }
        
        binding.layoutDocuments.setOnClickListener {
            onDocumentSelected?.invoke()
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
