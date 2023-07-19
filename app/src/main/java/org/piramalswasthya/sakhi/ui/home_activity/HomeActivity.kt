package org.piramalswasthya.sakhi.ui.home_activity

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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.MenuProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.ActivityHomeBinding
import org.piramalswasthya.sakhi.helpers.ImageUtils
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.helpers.MyContextWrapper
import org.piramalswasthya.sakhi.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.sakhi.ui.home_activity.home.HomeViewModel
import org.piramalswasthya.sakhi.ui.login_activity.LoginActivity
import org.piramalswasthya.sakhi.ui.service_location_activity.ServiceLocationActivity
import org.piramalswasthya.sakhi.work.WorkerUtils
import javax.inject.Inject


@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {


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

    private var _binding: ActivityHomeBinding? = null

    private val binding: ActivityHomeBinding
        get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private val langChooseAlert by lazy {
        val currentLanguageIndex = when (pref.getCurrentLanguage()) {
            Languages.ENGLISH -> 0
            Languages.HINDI -> 1
            Languages.ASSAMESE -> 2
        }
        MaterialAlertDialogBuilder(this).setTitle("Choose Application Language")
            .setSingleChoiceItems(
                arrayOf("English", "Hindi", "Assamese"), currentLanguageIndex
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
                    val restart = Intent(this, HomeActivity::class.java)
                    finish()
                    startActivity(restart)
                }

            }.create()
    }


    private val logoutAlert by lazy {
        MaterialAlertDialogBuilder(this).setTitle("Logout")
            .setMessage("${if (viewModel.unprocessedRecords > 0) "${viewModel.unprocessedRecords} not Processed." else "All records synced"} Are you sure to logout?")
            .setPositiveButton("YES") { dialog, _ ->
                viewModel.logout()
                ImageUtils.removeAllBenImages(this)
                WorkerUtils.cancelAllWork(this)
                dialog.dismiss()
            }.setNegativeButton("NO") { dialog, _ ->

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
        super.attachBaseContext(MyContextWrapper.wrap(newBase, pref.getCurrentLanguage().symbol))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpActionBar()
        setUpNavHeader()
        setUpSyncWorker()
        setUpMenu()



        viewModel.navigateToLoginPage.observe(this) {
            if (it) {
                startActivity(Intent(this, LoginActivity::class.java))
                viewModel.navigateToLoginPageComplete()
                finish()
            }
        }
    }

    private fun setUpMenu() {

        val menu = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.home_toolbar, menu)
                val homeMenu = menu.findItem(R.id.toolbar_menu_home)
                val langMenu = menu.findItem(R.id.toolbar_menu_language)
//                val servMenu = menu.findItem(R.id.change_service_location)
                homeMenu.isVisible = showMenuHome
                langMenu.isVisible = !showMenuHome
//                servMenu.isVisible = !showMenuHome

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.toolbar_menu_home -> {
                        navController.popBackStack(R.id.homeFragment, false)
                        return true
                    }

                    R.id.toolbar_menu_language -> {
                        langChooseAlert.show()
                    }
//                    R.id.change_service_location -> {
//                        finishAndStartServiceLocationActivity()
//                    }
                }
                return false
            }

        }
        addMenuProvider(menu)

    }

    fun addClickListenerToHomepageActionBarTitle() {
        binding.toolbar.setOnClickListener(onClickTitleBar)
        binding.toolbar.subtitle = "Tap to Change"
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

    private fun setUpSyncWorker() {
        WorkerUtils.triggerAmritSyncWorker(this)
        WorkerUtils.triggerD2dSyncWorker(this)
    }

    private fun setUpNavHeader() {
        val headerView = binding.navView.getHeaderView(0)
        viewModel.currentUser?.let {
            headerView.findViewById<TextView>(R.id.tv_nav_name).text =
                getString(R.string.nav_item_1_text, it.name)
            headerView.findViewById<TextView>(R.id.tv_nav_role).text =
                getString(R.string.nav_item_2_text, it.userName)
            headerView.findViewById<TextView>(R.id.tv_nav_id).text =
                getString(R.string.nav_item_3_text, it.userId)

//                headerView.findViewById<TextView>(R.id.tv_nav_version).text =
//                    getString(R.string.version)
        }
        viewModel.profilePicUri?.let {
            Glide.with(this).load(it).placeholder(R.drawable.ic_person).circleCrop()
                .into(binding.navView.getHeaderView(0).findViewById(R.id.iv_profile_pic))
        }
//

        binding.navView.getHeaderView(0).findViewById<ImageView>(R.id.iv_profile_pic)
            .setOnClickListener {
//            val galleryIntent = Intent(Intent.ACTION_PICK)
//            galleryIntent.type = "image/*"
                imagePickerActivityResult.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbar)

        binding.navView.setupWithNavController(navController)

        val appBarConfiguration = AppBarConfiguration.Builder(
            setOf(
                R.id.homeFragment, R.id.allHouseholdFragment, R.id.allBenFragment
            )
        ).setOpenableLayout(binding.drawerLayout).build()

        NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfiguration)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        binding.navView.menu.findItem(R.id.homeFragment).setOnMenuItemClickListener {
            navController.popBackStack(R.id.homeFragment, false)
            binding.drawerLayout.close()
            true

        }
        binding.navView.menu.findItem(R.id.sync_pending_records).setOnMenuItemClickListener {
            setUpSyncWorker()
            binding.drawerLayout.close()
            true

        }
        binding.navView.menu.findItem(R.id.menu_logout).setOnMenuItemClickListener {
            logoutAlert.show()
            true

        }

        binding.navView.menu.findItem(R.id.abha_id_activity).setOnMenuItemClickListener {
            navController.popBackStack(R.id.homeFragment, false)
            startActivity(Intent(this, AbhaIdActivity::class.java))
            binding.drawerLayout.close()
            true

        }
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == android.R.id.home) {
//            hideKeyboard(this)
//            Toast.makeText(this, "onOptionsItemSelected called!", Toast.LENGTH_SHORT).show()
//            onBackPressedDispatcher.onBackPressed()
//            return true // must return true to consume it here
//
//        }
//        return super.onOptionsItemSelected(item)
//    }

//    fun hideKeyboard(activity: Activity) {
//        this.currentFocus?.let { view ->
//            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
//            imm?.hideSoftInputFromWindow(view.windowToken, 0)
//        }
//    }


    fun updateActionBar(logoResource: Int, title: String? = null) {
        binding.toolbar.setLogo(logoResource)
        title?.let {
            binding.toolbar.title = it
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
}