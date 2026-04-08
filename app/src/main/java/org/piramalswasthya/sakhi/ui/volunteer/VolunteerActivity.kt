package org.piramalswasthya.sakhi.ui.volunteer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.core.view.GravityCompat
import androidx.core.view.MenuProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.ActivityVolunteerBinding
import org.piramalswasthya.sakhi.helpers.ImageUtils
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.helpers.MyContextWrapper
import org.piramalswasthya.sakhi.helpers.TapjackingProtectionHelper
import org.piramalswasthya.sakhi.ui.home_activity.sync.SyncBottomSheetFragment
import org.piramalswasthya.sakhi.ui.login_activity.LoginActivity
import org.piramalswasthya.sakhi.ui.volunteer.fragment.VolunteerViewModel
import org.piramalswasthya.sakhi.work.WorkerUtils
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class VolunteerActivity : AppCompatActivity() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WrapperEntryPoint {
        val pref: PreferenceDao
    }

    @Inject
    lateinit var pref: PreferenceDao

    private var _binding: ActivityVolunteerBinding? = null
    private val binding: ActivityVolunteerBinding
        get() = _binding!!

    private val viewModel: VolunteerViewModel by viewModels()

    private val syncBottomSheet: SyncBottomSheetFragment by lazy {
        SyncBottomSheetFragment()
    }

    private val logoutAlert by lazy {
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.logout))
            .setMessage(resources.getString(R.string.are_you_sure_to_logout))
            .setPositiveButton(resources.getString(R.string.yes)) { dialog, _ ->
                viewModel.logout()
                ImageUtils.removeAllBenImages(this)
                WorkerUtils.cancelAllWork(this)
                dialog.dismiss()
            }
            .setNegativeButton(resources.getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }.create()
    }

    private val langChooseAlert by lazy {
        val currentLanguageIndex = when (pref.getCurrentLanguage()) {
            Languages.ENGLISH -> 0
            Languages.HINDI -> 1
            Languages.ASSAMESE -> 2
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.choose_application_language))
            .setSingleChoiceItems(
                arrayOf(
                    resources.getString(R.string.english),
                    resources.getString(R.string.hindi),
                    resources.getString(R.string.assamese)
                ), currentLanguageIndex
            ) { di, checkedItemIndex ->
                val checkedLanguage = when (checkedItemIndex) {
                    0 -> Languages.ENGLISH
                    1 -> Languages.HINDI
                    2 -> Languages.ASSAMESE
                    else -> throw IllegalStateException("Unknown language index $checkedItemIndex")
                }
                if (checkedItemIndex == currentLanguageIndex) {
                    di.dismiss()
                } else {
                    pref.saveSetLanguage(checkedLanguage)
                    val restart = Intent(this, VolunteerActivity::class.java)
                    finish()
                    startActivity(restart)
                }
            }.create()
    }

    private val navController by lazy {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_volunteer) as NavHostFragment
        navHostFragment.navController
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

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return if (TapjackingProtectionHelper.isTouchAllowed(this, ev)) {
            super.dispatchTouchEvent(ev)
        } else {
            false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        TapjackingProtectionHelper.applyWindowSecurity(this)
        super.onCreate(savedInstanceState)
        _binding = ActivityVolunteerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        TapjackingProtectionHelper.enableTouchFiltering(this)

        setUpActionBar()
        setUpNavHeader()
        setUpMenu()

        binding.versionName.text = "APK Version ${BuildConfig.VERSION_NAME}"

        viewModel.navigateToLoginPage.observe(this) {
            if (it) {
                startActivity(Intent(this, LoginActivity::class.java))
                viewModel.navigateToLoginPageComplete()
                finish()
            }
        }
    }

    private fun setUpMenu() {
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.home_toolbar, menu)
                menu.findItem(R.id.toolbar_menu_home)?.isVisible = false
                menu.findItem(R.id.toolbar_menu_language)?.isVisible = true
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.toolbar_menu_language -> {
                        langChooseAlert.show()
                        return true
                    }
                    R.id.sync_status -> {
                        if (!syncBottomSheet.isVisible)
                            syncBottomSheet.show(
                                supportFragmentManager,
                                resources.getString(R.string.sync)
                            )
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun setUpNavHeader() {
        val headerView = binding.navView.getHeaderView(0)
        viewModel.currentUser?.let {
            headerView.findViewById<TextView>(R.id.tv_nav_name).text =
                resources.getString(R.string.nav_item_1_text, it.name)
            headerView.findViewById<TextView>(R.id.tv_nav_role).text =
                resources.getString(R.string.nav_item_2_text, it.userName)

            val englishId = String.format(Locale.ENGLISH, "%s", it.userId)
            val formatted = HtmlCompat.fromHtml(
                getString(R.string.nav_item_3_text, englishId),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            headerView.findViewById<TextView>(R.id.tv_nav_id).text = formatted
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbar)
        binding.navView.setupWithNavController(navController)

        val appBarConfiguration = AppBarConfiguration.Builder(
            setOf(R.id.volunteerHomeFragment)
        ).setOpenableLayout(binding.drawerLayout).build()

        NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfiguration)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        binding.navView.menu.findItem(R.id.menu_logout)?.setOnMenuItemClickListener {
            logoutAlert.show()
            true
        }

        binding.navView.menu.findItem(R.id.sync_pending_records)?.setOnMenuItemClickListener {
            WorkerUtils.triggerAmritPushWorker(this)
            binding.drawerLayout.close()
            true
        }
    }

    fun updateActionBar(icon: Int, title: String) {
        binding.ivToolbar.setImageResource(icon)
        binding.toolbar.title = null
        binding.tvToolbar.text = title
    }

    override fun onPause() {
        super.onPause()
        window.decorView.alpha = 0f
    }

    override fun onResume() {
        super.onResume()
        window.decorView.alpha = 1f
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START))
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        else super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}