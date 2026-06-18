package org.piramalswasthya.sakhi.ui.login_activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.LanguageAdapter
import org.piramalswasthya.sakhi.databinding.BottomSheetLanguageBinding
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.Language

class LanguageBottomSheet(
    private val currentLanguage: Languages,
    private val onLanguageSelected: (Language) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetLanguageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetLanguageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvLanguages.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = LanguageAdapter(getLanguageList()) { selected ->
                if (selected.isSelected) {
                    dismiss()
                } else {
                    onLanguageSelected(selected)
                    dismiss()
                }
            }
        }
    }

    private fun getLanguageList(): List<Language> {
        val isMitanin = BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)
        val selected = ContextCompat.getDrawable(requireContext(), R.drawable.bg_lang_circle_stroke_selected)!!
        val unselected = ContextCompat.getDrawable(requireContext(), R.drawable.bg_lang_circle_stroke_unselected)!!

        return buildList {
            add(Language(
                id = 1,
                lanFirstWord = "E",
                lanName = getString(R.string.text_english),
                lanSelectedView = selected,
                lanUnselectedView = unselected,
                language = Languages.ENGLISH,
                isSelected = currentLanguage == Languages.ENGLISH
            ))
            add(Language(
                id = 2,
                lanFirstWord = "ह",
                lanName = getString(R.string.text_hindi),
                lanSelectedView = selected,
                lanUnselectedView = unselected,
                language = Languages.HINDI,
                isSelected = currentLanguage == Languages.HINDI
            ))
            if (!isMitanin) {
                add(
                    Language(
                        id = 3,
                        lanFirstWord = "অ",
                        lanName = getString(R.string.text_assamese),
                        lanSelectedView = selected,
                        lanUnselectedView = unselected,
                        language = Languages.ASSAMESE,
                        isSelected = currentLanguage == Languages.ASSAMESE
                    )
                )
                add(
                    Language(
                        id = 4,
                        lanFirstWord = "অ",
                        lanName = getString(R.string.text_bangali),
                        lanSelectedView = selected,
                        lanUnselectedView = unselected,
                        language = Languages.BANGLA,
                        isSelected = currentLanguage == Languages.BANGLA
                    )
                )
            }
            /*add(Language(
                id = 4,
                lanFirstWord = "বা",
                lanName = getString(R.string.text_bangali),
                lanSelectedView = selected,
                lanUnselectedView = unselected,
                language = Languages.BANGLA,
                isSelected = currentLanguage == Languages.BANGLA
            ))*/
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}