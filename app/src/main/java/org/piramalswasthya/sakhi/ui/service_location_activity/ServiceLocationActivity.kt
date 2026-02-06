package org.piramalswasthya.sakhi.ui.service_location_activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.ActivityServiceTypeBinding
import org.piramalswasthya.sakhi.helpers.MyContextWrapper
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import timber.log.Timber

@AndroidEntryPoint
class ServiceLocationActivity : AppCompatActivity() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WrapperEntryPoint {
        val pref: PreferenceDao
    }

    private var _binding: ActivityServiceTypeBinding? = null
    private val binding: ActivityServiceTypeBinding
        get() = _binding!!

    private val viewModel: ServiceTypeViewModel by viewModels()
    private val onBackPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // If there are unsaved changes, confirm with the user
                if (viewModel.hasUnsavedChanges.value == true) {
                    showUnsavedChangesDialog(
                        onDiscard = {
                            viewModel.clearUnsavedChanges()
                            viewModel.clearDraftFromSavedState()
                            finish()
                            val goToHome = Intent(this@ServiceLocationActivity, HomeActivity::class.java)
                            startActivity(goToHome)
                        },
                        onStay = {
                            // do nothing - keep user on screen
                        },
                        onSaveDraft = {
                            viewModel.saveDraftToSavedState()
                            // after saving draft, navigate home (keeps parity with normal flow)
                            finish()
                            val goToHome = Intent(this@ServiceLocationActivity, HomeActivity::class.java)
                            startActivity(goToHome)
                        }
                    )
                    return
                }

                // fallback to existing behavior
                if (viewModel.isLocationSet()) {
                    finish()
                    val goToHome = Intent(this@ServiceLocationActivity, HomeActivity::class.java)
                    startActivity(goToHome)
                } else
                    if (!exitAlert.isShowing)
                        exitAlert.show()

            }
        }
    }
    private val incompleteLocationAlert by lazy {
        MaterialAlertDialogBuilder(this)
            .setTitle("Missing Detail")
            .setMessage("At least one of the following is missing value:\n \n\tState\n\tDistrict\n\tBlock\n\tVillage")
            .setPositiveButton("Understood") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }
    private val exitAlert by lazy {
        MaterialAlertDialogBuilder(this)
            .setTitle("Exit Application")
            .setMessage("Do you want to exit application")
            .setPositiveButton("Yes") { _, _ ->
                finish()
            }
            .setNegativeButton("No") { d, _ ->
                d.dismiss()
            }
            .create()
    }

    override fun attachBaseContext(newBase: Context) {
        val pref = EntryPointAccessors.fromApplication(
            newBase, WrapperEntryPoint::class.java
        ).pref
        super.attachBaseContext(
            MyContextWrapper.wrap(
                newBase,
                newBase.applicationContext,
                pref.getCurrentLanguage().symbol
            )
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityServiceTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Timber.d("onViewCreated() called!")
        binding.lifecycleOwner = this


        onBackPressedDispatcher.addCallback(
            this, onBackPressedCallback
        )

        binding.btnContinue.setOnClickListener {
            if (dataValid()) {
                viewModel.saveCurrentLocation()
                finish()
                val goToHome = Intent(this@ServiceLocationActivity, HomeActivity::class.java)
                startActivity(goToHome)
            } else
                incompleteLocationAlert.show()
        }
        viewModel.state.observe(this) {
            it?.let {
                when (it) {
                    ServiceTypeViewModel.State.IDLE -> {}//TODO()
                    ServiceTypeViewModel.State.LOADING -> {}//TODO()
                    ServiceTypeViewModel.State.SUCCESS -> {
                        binding.viewModel = viewModel
                        binding.actvStateDropdown.apply {
                            isEnabled = false
                            setText(viewModel.stateList.first())
                        }
                        binding.actvDistrictDropdown.apply {
                            isEnabled = false
                            setText(viewModel.districtList.first())
                        }
                        binding.actvBlockDropdown.apply {
                            isEnabled = false
                            setText(viewModel.blockList.first())
                        }
                        binding.actvVillageDropdown.apply {
                            setText(viewModel.selectedVillageName)
                            if (viewModel.villageList.size == 1) {
                                setText(viewModel.villageList.first())
                                viewModel.setVillage(0)
                            }
                            setOnItemClickListener { _, _, i, _ ->
                                viewModel.setVillage(i)
                                // mark unsaved change when user picks a village
                                viewModel.markLocationChanged()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun dataValid(): Boolean {
        return !(binding.actvStateDropdown.text.isNullOrBlank() ||
                binding.actvDistrictDropdown.text.isNullOrBlank() ||
                binding.actvBlockDropdown.text.isNullOrBlank() ||
                binding.actvVillageDropdown.text.isNullOrBlank())

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun showUnsavedChangesDialog(
        title: String = "Unsaved changes",
        message: String = "You have unsaved changes on this screen. Discard changes, stay on this screen, or save a draft to continue later?",
        onDiscard: () -> Unit,
        onStay: () -> Unit,
        onSaveDraft: () -> Unit,
    ) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton("Discard") { d, _ ->
                onDiscard()
                d.dismiss()
            }
            .setNeutralButton("Stay") { d, _ ->
                onStay()
                d.dismiss()
            }
            .setPositiveButton("Save Draft") { d, _ ->
                onSaveDraft()
                d.dismiss()
            }
            .show()
    }

}