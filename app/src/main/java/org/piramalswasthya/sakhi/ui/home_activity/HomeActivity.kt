package org.piramalswasthya.sakhi.ui.home_activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ExpandableListView
import android.widget.ExpandableListView.OnGroupClickListener
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.core.view.MenuProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.crashlytics.internal.common.CommonUtils.isEmulator
import com.google.firebase.crashlytics.internal.common.CommonUtils.isRooted
import com.yugasa.exxonmobil.view.adapters.ExpandableNavigationListAdapter
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.ActivityHomeBinding
import org.piramalswasthya.sakhi.helpers.ImageUtils
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.helpers.MyContextWrapper
import org.piramalswasthya.sakhi.model.MenuModel
import org.piramalswasthya.sakhi.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.sakhi.ui.home_activity.home.HomeViewModel
import org.piramalswasthya.sakhi.ui.home_activity.sync.SyncBottomSheetFragment
import org.piramalswasthya.sakhi.ui.login_activity.LoginActivity
import org.piramalswasthya.sakhi.ui.service_location_activity.ServiceLocationActivity
import org.piramalswasthya.sakhi.work.WorkerUtils
import java.net.URI
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {
    private val isChatSupportEnabled = true
   private lateinit var expandableListView: ExpandableListView
    private lateinit var expandableListAdapter: ExpandableNavigationListAdapter
    private lateinit var headerList: MutableList<MenuModel>
    private lateinit var childList: MutableMap<MenuModel, List<MenuModel>>
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


    private val syncBottomSheet: SyncBottomSheetFragment by lazy {
        SyncBottomSheetFragment()
    }



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
        // This will block user to cast app screen
        // Toggle screencast mode for staging & production builds
     //   window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        super.onCreate(savedInstanceState)
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpActionBar()
        setUpNavHeader()
        setUpFirstTimePullWorker()
        setUpMenu()

        val permissions = arrayOf<String>(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )

        ActivityCompat.requestPermissions(
            this,
            permissions,
            1010
        )

        if (isChatSupportEnabled)
        {
            binding.addFab.visibility = View.VISIBLE

            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

            binding.addFab.setOnClickListener {

                displaychatdialog(BuildConfig.CHAT_URL+"?lang="+pref.getCurrentLanguage().symbol)

            }

        }
        else
        {
            binding.addFab.visibility = View.GONE
        }


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

        initializeMenuData()
        setupExpandableList()
    }
    private fun initializeMenuData() {
        headerList = mutableListOf()
        childList = mutableMapOf()

        // Headers with no children
        headerList.add(MenuModel("Home", false, R.drawable.ic_home))
        headerList.add(MenuModel("Sync Records", false, R.drawable.ic_synced))
        headerList.add(MenuModel("Create ABHA Number", false, R.drawable.ic_completed_tasks))
        headerList.add(MenuModel("Incentives", false, R.drawable.ic__incentive))

        // Header with children
        val helpHeader = MenuModel("Help", true, R.drawable.baseline_chat_24)
        val helpChildList = listOf(
            MenuModel("EC Service (Family Planning)", false, R.drawable.family_planning_services),
            MenuModel("ANC Services", false, R.drawable.anc_services),
            MenuModel("PNC Services", false, R.drawable.pnc_services),
            MenuModel("Infant and Child Services", false, R.drawable.infant___child_services),
            MenuModel("Immunization Services", false, R.drawable.immunization_services),
            MenuModel("NCD Services", false, R.drawable.ncd_services),
            MenuModel("TB Services", false, R.drawable.tb_services)
        )
        headerList.add(helpHeader)
        childList[helpHeader] = helpChildList

        // Logout section (not expandable)
        headerList.add(MenuModel("Logout", false, R.drawable.ic_logout))
    }

private fun setupExpandableList() {
   expandableListView = findViewById(R.id.expandableListView)
   initializeMenuData()
   expandableListAdapter = ExpandableNavigationListAdapter(this, headerList, childList)
   expandableListView.setAdapter(expandableListAdapter)

   expandableListView.setOnGroupClickListener { parent, v, groupPosition, id ->
       val header = headerList[groupPosition]
       if (!header.hasChildren) {
           // Handle non-expandable items (e.g., navigate to a new activity)
           handleNavigation(header.title, "")
           return@setOnGroupClickListener true // Prevent expansion
       }
       else{
           val imgSelection = v.findViewById<ImageView>(R.id.icon_start)
           if (parent.isGroupExpanded(groupPosition)) {
               imgSelection.setImageResource(R.drawable.ic_arrow_down) // Set down icon
           } else {
               imgSelection.setImageResource(R.drawable.ic_arrow_up) // Set up icon
           }
           return@setOnGroupClickListener false // Prevent expansion
       }
       false
   }

    // Handle clicks on child items
    expandableListView.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
        val header = headerList[groupPosition] // Get parent header
        val childItem = childList[header]?.get(childPosition) // Get child item

        childItem?.let {
            handleNavigation(header.title, childItem.title)

            // Perform action based on child item
        }
        true
    }
}

private fun handleNavigation(headerTitle: String, childTitle: String?) {
   when (headerTitle) {
       "Home" -> {
           navController.popBackStack(R.id.homeFragment, false)
           binding.drawerLayout.close()
       }
       "Sync Records" -> {
           WorkerUtils.triggerAmritPushWorker(this)
           if (!pref.isFullPullComplete)
               WorkerUtils.triggerAmritPullWorker(this)
           binding.drawerLayout.close()
       }
       "Create ABHA Number" -> {
           navController.popBackStack(R.id.homeFragment, false)
           startActivity(Intent(this, AbhaIdActivity::class.java))
           binding.drawerLayout.close()
       }
       "Incentives" ->  {

       }
       "Help" -> {
           when (childTitle) {
               "EC Service (Family Planning)" ->  {
                   displaychatdialog("https://piramalvoicebot.yugasa.org/?node=ec_services&lang="+pref.getCurrentLanguage().symbol)
                   binding.drawerLayout.close()
               }
               "ANC Services" -> {
                   displaychatdialog("https://piramalvoicebot.yugasa.org/?node=anc_services&lang="+pref.getCurrentLanguage().symbol)
                   binding.drawerLayout.close()
               }
               "PNC Services" -> {
                   displaychatdialog("https://piramalvoicebot.yugasa.org/?node=pnc_services&lang="+pref.getCurrentLanguage().symbol)
                   binding.drawerLayout.close()
               }
               "Infant and Child Services" -> {
                   displaychatdialog("https://piramalvoicebot.yugasa.org/?node=infant_child_services&lang="+pref.getCurrentLanguage().symbol)
                   binding.drawerLayout.close()
               }
               "Immunization Services" -> {
                   displaychatdialog("https://piramalvoicebot.yugasa.org/?node=immunization_services&lang="+pref.getCurrentLanguage().symbol)
                   binding.drawerLayout.close()
               }
               "NCD Services" -> {
                   displaychatdialog("https://piramalvoicebot.yugasa.org/?node=ncd_services&lang="+pref.getCurrentLanguage().symbol)
                   binding.drawerLayout.close()
               }
               "TB Services" -> {
                   displaychatdialog("https://piramalvoicebot.yugasa.org/?node=tb_services&lang="+pref.getCurrentLanguage().symbol)
                   binding.drawerLayout.close()
               }
           }
       }
       "Logout" ->  logoutAlert.show()
   }
}

private fun navigateActivity(activityClass: Class<*>, extraData: String) {
   val intent = Intent(this, activityClass)
   intent.putExtra("EXTRA_DATA", extraData)
   startActivity(intent)
}

private fun logoutAlert() {
   AlertDialog.Builder(this)
       .setTitle("Logout")
       .setMessage("Are you sure you want to logout?")
       .setPositiveButton("Yes") { _, _ ->
           // Perform logout logic here
           Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
       }
       .setNegativeButton("Cancel", null)
       .show()
}

    private fun displaychatdialog(url: String) {
        val dialog = BottomSheetDialog(this)

        val view = layoutInflater.inflate(R.layout.bottomsheet_chat_window, null)
        val web = view.findViewById<WebView>(R.id.webv)
        val ivBottom = view.findViewById<ImageView>(R.id.iv_bottom)
        val progress = view.findViewById<ProgressBar>(R.id.progressBarv)

        web.settings.javaScriptEnabled = true
        web.settings.javaScriptCanOpenWindowsAutomatically = true
        web.isVerticalScrollBarEnabled = true
        web.loadUrl(url)

        web.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                request.grant(request.resources)
            }
        }

        web.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progress.visibility = View.VISIBLE
                web.visibility = View.GONE
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                progress.visibility = View.GONE
                web.visibility = View.VISIBLE
            }
        }

        dialog.setContentView(view)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.behavior.isDraggable = true

        val screenHeight = resources.displayMetrics.heightPixels
        dialog.behavior.peekHeight = (screenHeight * 0.85).toInt()

        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        // Apply custom animation
        dialog.window?.setWindowAnimations(R.style.DialogAnimation)

        ivBottom.setOnClickListener {
            dialog.dismiss()
        }

        // Ensure smooth dismissal
        dialog.setOnDismissListener {
            web.loadUrl("about:blank")
        }

        dialog.show()
    }
override fun onResume() {
   // This will block user to cast app screen
   // Toggle screencast mode for staging & production builds
//  window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
   super.onResume()
   if (isDeviceRootedOrEmulator()) {
       AlertDialog.Builder(this)
           .setTitle("Unsupported Device")
           .setMessage("This is app cannot run on rooted devices or emulators.")
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
           homeMenu.isVisible = showMenuHome
           langMenu.isVisible = !showMenuHome

       }

       override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
           when (menuItem.itemId) {
               R.id.toolbar_menu_home -> {
                   navController.popBackStack(R.id.homeFragment, false)
                   return true
               }

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

private fun setUpFirstTimePullWorker() {
   WorkerUtils.triggerPeriodicPncEcUpdateWorker(this)
   if (!pref.isFullPullComplete)
       WorkerUtils.triggerAmritPullWorker(this)
//        WorkerUtils.triggerD2dSyncWorker(this)
}

private fun setUpNavHeader() {
   val headerView = binding.navView.getHeaderView(0)

   viewModel.currentUser?.let {
       headerView.findViewById<TextView>(R.id.tv_nav_name).text =
           resources.getString(R.string.nav_item_1_text, it.name)
       headerView.findViewById<TextView>(R.id.tv_nav_role).text =
           resources.getString(R.string.nav_item_2_text, it.userName)
       headerView.findViewById<TextView>(R.id.tv_nav_id).text =
           resources.getString(R.string.nav_item_3_text, it.userId)
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
           R.id.homeFragment, R.id.allHouseholdFragment, R.id.allBenFragment
       )
   ).setOpenableLayout(binding.drawerLayout).build()

   NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfiguration)
   NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
}


fun updateActionBar(logoResource: Int, title: String? = null) {
   binding.ivToolbar.setImageResource(logoResource)
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

//      return isRooted() || isEmulator() || RootedUtil().isDeviceRooted(applicationContext)
   return isRooted() || isEmulator()

}

}
