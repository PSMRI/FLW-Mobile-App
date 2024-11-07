package org.piramalswasthya.sakhi.ui.asha_facilitator_activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.MenuProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.crashlytics.internal.common.CommonUtils
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.ActivityFacilitatorsHomeAcivityBinding
import org.piramalswasthya.sakhi.databinding.ActivityHomeBinding
import org.piramalswasthya.sakhi.helpers.ImageUtils
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.helpers.MyContextWrapper
import org.piramalswasthya.sakhi.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.home.HomeViewModel
import org.piramalswasthya.sakhi.ui.home_activity.sync.SyncBottomSheetFragment
import org.piramalswasthya.sakhi.ui.login_activity.LoginActivity
import org.piramalswasthya.sakhi.ui.service_location_activity.ServiceLocationActivity
import org.piramalswasthya.sakhi.work.WorkerUtils
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class FacilitatorsHomeActivity : AppCompatActivity() {


    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WrapperEntryPoint {
        val pref: PreferenceDao
    }

    private val onClickTitleBar = View.OnClickListener {
        if (!showMenuHome) {
            finishAndStartServiceLocationActivity()
        }
    }

    @Inject
    lateinit var pref: PreferenceDao

    private var _binding: ActivityFacilitatorsHomeAcivityBinding? = null

    private val binding: ActivityFacilitatorsHomeAcivityBinding
        get() = _binding!!




    private val viewModel: HomeViewModel by viewModels()

    private val langChooseAlert by lazy {
        val currentLanguageIndex = when (pref.getCurrentLanguage()) {
            Languages.ENGLISH -> 0
            Languages.HINDI -> 1
            Languages.ASSAMESE -> 2
        }
        MaterialAlertDialogBuilder(this).setTitle(resources.getString(R.string.choose_application_language))
            .setSingleChoiceItems(
                arrayOf(
                    resources.getString(R.string.english),
                    resources.getString(R.string.hindi),
                    resources.getString(
                        R.string.assamese
                    )
                ), currentLanguageIndex
            ) { di, checkedItemIndex ->
                val checkedLanguage = when (checkedItemIndex) {
                    0 -> Languages.ENGLISH
                    1 -> Languages.HINDI
                    2 -> Languages.ASSAMESE
                    else -> throw IllegalStateException("yoohuulanguageindexunkonwn $checkedItemIndex")
                }
                if (checkedItemIndex == currentLanguageIndex) {
                    di.dismiss()
                } else {
                    pref.saveSetLanguage(checkedLanguage)
                    Locale.setDefault(Locale(checkedLanguage.symbol))

                    val restart = Intent(this, HomeActivity::class.java)
                    finish()
                    startActivity(restart)
                }

            }.create()
    }


    private val logoutAlert by lazy {
        var str = ""
        if (viewModel.unprocessedRecords > 0) {
            str += viewModel.unprocessedRecords
            str += resources.getString(R.string.not_processed)
        } else {
            str += resources.getString(R.string.all_records_synced)
        }
        str += resources.getString(R.string.are_you_sure_to_logout)

        MaterialAlertDialogBuilder(this).setTitle(resources.getString(R.string.logout))
            .setMessage(str)
            .setPositiveButton(resources.getString(R.string.yes)) { dialog, _ ->
                viewModel.logout()
                ImageUtils.removeAllBenImages(this)
                WorkerUtils.cancelAllWork(this)
                dialog.dismiss()
            }.setNegativeButton(resources.getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }.create()
    }

    private val imagePickerActivityResult =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            it?.let {
                viewModel.profilePicUri = it
                Glide.with(this).load(it).placeholder(R.drawable.ic_person).circleCrop()
                    .into(binding.navView.getHeaderView(0).findViewById(R.id.iv_profile_pic))
//                binding.navView.getHeaderView(0).findViewById<ImageView>(R.id.iv_profile_pic).setImageURI(it)
//                Glide.with(this)
//                    .load(it)
//                    .into(binding.navView.getHeaderView(0).findViewById(R.id.iv_profile_pic))
            }
        }

    private val navController by lazy {
        val navHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_home) as NavHostFragment
        navHostFragment.navController
    }

    var showMenuHome: Boolean = false

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
        _binding = ActivityFacilitatorsHomeAcivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpActionBar()
        setUpNavHeader()
        setUpMenu()

        if (isDeviceRootedOrEmulator()) {
            AlertDialog.Builder(this)
                .setTitle("Unsupported Device")
                .setMessage("This app cannot run on rooted devices or emulators.")
                .setCancelable(false)
                .setPositiveButton("Exit") { dialog, id -> finish() }
                .show()
        }

        viewModel.navigateToLoginPage.observe(this) {
            if (it) {
                startActivity(Intent(this, LoginActivity::class.java))
                viewModel.navigateToLoginPageComplete()
                finish()
            }
        }
    }

    override fun onResume() {
        // This will block user to cast app screen
        // Toggle screencast mode for staging & production builds
//        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        super.onResume()
        if (isDeviceRootedOrEmulator()) {
            AlertDialog.Builder(this)
                .setTitle("Unsupported Device")
                .setMessage("This app cannot run on rooted devices or emulators.")
                .setCancelable(false)
                .setPositiveButton("Exit") { dialog, id -> finish() }
                .show()
        }
    }
    private fun setUpMenu() {
        val menu = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.home_toolbar, menu)
                val homeMenu = menu.findItem(R.id.toolbar_menu_home)
                val langMenu = menu.findItem(R.id.toolbar_menu_language)
                val syncStatus = menu.findItem(R.id.sync_status)
                homeMenu.isVisible = showMenuHome
                langMenu.isVisible = !showMenuHome
                syncStatus.isVisible = false

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.toolbar_menu_home -> {
                        navController.popBackStack(R.id.facilitatorHomeFragment, false)
                        return true
                    }

                    R.id.toolbar_menu_language -> {
                        langChooseAlert.show()
                        return true
                    }

                }
                return false
            }

        }
        addMenuProvider(menu)

    }

    fun addClickListenerToHomepageActionBarTitle() {
        binding.toolbar.setOnClickListener(onClickTitleBar)
//        binding.toolbar.subtitle = resources.getString(R.string.tap_to_change)
    }

    fun removeClickListenerToHomepageActionBarTitle() {
        binding.toolbar.setOnClickListener(null)
        binding.toolbar.subtitle = null
    }


    private fun finishAndStartServiceLocationActivity() {
        val serviceLocationActivity = Intent(this, ServiceLocationActivity::class.java)
        finish()
        startActivity(serviceLocationActivity)
    }

    fun setHomeMenuItemVisibility(show: Boolean) {
        showMenuHome = show
        invalidateOptionsMenu()
    }


    private fun setUpNavHeader() {
        val headerView = binding.navView.getHeaderView(0)

        viewModel.currentUser?.let {
            headerView.findViewById<TextView>(R.id.tv_nav_name).text =
                resources.getString(R.string.nav_item_1_text, it.name)
            headerView.findViewById<TextView>(R.id.tv_nav_role).text =
                resources.getString(R.string.nav_item_2_text, it.userName)
            headerView.findViewById<TextView>(R.id.tv_nav_id).text =
                resources.getString(R.string.nav_item_5_text, it.userId)
        }
        viewModel.profilePicUri?.let {
            Glide.with(this).load(it).placeholder(R.drawable.ic_person).circleCrop()
                .into(binding.navView.getHeaderView(0).findViewById(R.id.iv_profile_pic))
        }
//

        binding.navView.getHeaderView(0).findViewById<ImageView>(R.id.iv_profile_pic)
            .setOnClickListener {
                imagePickerActivityResult.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbar)

        binding.navView.setupWithNavController(navController)

        val appBarConfiguration = AppBarConfiguration.Builder(
            setOf(
                R.id.facilitatorHomeFragment
            )
        ).setOpenableLayout(binding.drawerLayout).build()

        NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfiguration)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        binding.navView.menu.findItem(R.id.homeFragment).setOnMenuItemClickListener {
            navController.popBackStack(R.id.facilitatorHomeFragment, false)
            binding.drawerLayout.close()
            true

        }

        binding.navView.menu.findItem(R.id.menu_logout).setOnMenuItemClickListener {
            logoutAlert.show()
            true

        }

    }


    fun updateActionBar(logoResource: Int, title: String? = null) {
        binding.ivToolbar.setImageResource(logoResource)
//        binding.toolbar.setLogo(logoResource)
        title?.let {
            binding.toolbar.title = null
            binding.tvToolbar.text = it
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START))
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        else super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun isDeviceRootedOrEmulator(): Boolean {
        return CommonUtils.isRooted() || CommonUtils.isEmulator()
//                || RootedUtil().isDeviceRooted(applicationContext)
    }

}