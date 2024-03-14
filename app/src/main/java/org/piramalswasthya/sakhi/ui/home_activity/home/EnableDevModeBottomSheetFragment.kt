package org.piramalswasthya.sakhi.ui.home_activity.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.piramalswasthya.sakhi.databinding.BottomSheetEnableDevModeBinding
import org.piramalswasthya.sakhi.helpers.Konstants


class EnableDevModeBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetEnableDevModeBinding? = null
    private val binding: BottomSheetEnableDevModeBinding
        get() = _binding!!

    private val viewModel: HomeViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetEnableDevModeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.button5.setOnClickListener {
            binding.editText.text?.toString()?.toInt()?.takeIf { it == Konstants.devCode }?.let {
                viewModel.setDevMode(true)
                binding.editText.setText("")
                Toast.makeText(context, "Dev Mode Enabled!", Toast.LENGTH_LONG).show()
                dismiss()
            } ?: run {
                Toast.makeText(context, "Invalid PIN!", Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}