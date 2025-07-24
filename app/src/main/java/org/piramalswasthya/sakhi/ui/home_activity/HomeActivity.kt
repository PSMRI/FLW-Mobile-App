package org.piramalswasthya.sakhi.ui.home_activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.os.SystemClock
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.internal.common.CommonUtils.isEmulator
import com.google.firebase.crashlytics.internal.common.CommonUtils.isRooted
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.ActivityHomeBinding
import org.piramalswasthya.sakhi.helpers.AnalyticsHelper
import org.piramalswasthya.sakhi.helpers.ImageUtils
import org.piramalswasthya.sakhi.helpers.InAppUpdateHelper
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.helpers.MyContextWrapper
import org.piramalswasthya.sakhi.helpers.isInternetAvailable
import org.piramalswasthya.sakhi.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.sakhi.ui.home_activity.home.HomeViewModel
import org.piramalswasthya.sakhi.ui.home_activity.sync.SyncBottomSheetFragment
import org.piramalswasthya.sakhi.ui.login_activity.LoginActivity
import org.piramalswasthya.sakhi.ui.service_location_activity.ServiceLocationActivity
import org.piramalswasthya.sakhi.utils.KeyUtils
import org.piramalswasthya.sakhi.work.WorkerUtils
import java.net.URI
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class HomeActivity : AppCompatActivity(), MessageUpdate {

    var isChatSupportEnabled : Boolean = false
    private lateinit var updateHelper: InAppUpdateHelper
    @Inject lateinit var analyticsHelper: AnalyticsHelper

    private lateinit var inAppUpdateHelper: InAppUpdateHelper

    var lastClickTime: Long = 0L

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
            Languages.ASSAMESE -> 1
         /*   Languages.HINDI -> 2*/

        }
        MaterialAlertDialogBuilder(this).setTitle(resources.getString(R.string.choose_application_language))
            .setSingleChoiceItems(
                arrayOf(
                    resources.getString(R.string.english),
                  /*  resources.getString(R.string.hindi),*/
                    resources.getString(R.string.assamese)
                ), currentLanguageIndex
            ) { di, checkedItemIndex ->
                val checkedLanguage = when (checkedItemIndex) {
                    0 -> Languages.ENGLISH
               /*     2 -> Languages.HINDI*/
                    1 -> Languages.ASSAMESE
                    else -> throw IllegalStateException("yoohuulanguageindexunkonwn $checkedItemIndex")
                }
                if (checkedItemIndex == currentLanguageIndex) {
                    di.dismiss()
                } else {
                    pref.saveSetLanguage(checkedLanguage)
                    di.dismiss()
                    val restart = Intent(this, HomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(restart)
                    finish()
                }

            }.create()
    }


    private val logoutAlert by lazy {
        var str = ""
        if (viewModel.unprocessedRecordsCount.value!! > 0) {
            str += viewModel.unprocessedRecordsCount.value!!
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
        if (BuildConfig.FLAVOR.equals("niramay", true) ||BuildConfig.FLAVOR.equals("xushrukha", true) || BuildConfig.FLAVOR.equals("saksham", true))  {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
        FirebaseApp.initializeApp(this)
        FBMessaging.messageUpdate = this
        FirebaseMessaging.getInstance().subscribeToTopic("All")
//        FirebaseMessaging.getInstance().subscribeToTopic("ANC${pref.getLoggedInUser()?.userId}")
//        FirebaseMessaging.getInstance().subscribeToTopic("Immunization${pref.getLoggedInUser()?.userId}")
        super.onCreate(savedInstanceState)
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpActionBar()
        setUpNavHeader()
        setUpFirstTimePullWorker()
        setUpMenu()

        askForPermissions()

        if (isChatSupportEnabled)
        {
            binding.addFab.visibility = View.VISIBLE

            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

            binding.addFab.setOnClickListener {

                displaychatdialog()

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
        viewModel.unprocessedRecordsCount.observe(this) {
            if (it>0) {
                if (isInternetAvailable(this)){
                    WorkerUtils.triggerAmritPushWorker(this)
                }
            }
        }
        binding.versionName.text = "${BuildConfig.VERSION_NAME}"//"APK Version 2.2.3"

        inAppUpdateHelper = InAppUpdateHelper(this)
        inAppUpdateHelper.checkForUpdate()

        viewModel.currentUser?.let {
            analyticsHelper.setUserId(it.userId.toString())
            val params = Bundle().apply {
                putString(FirebaseAnalytics.Param.VALUE, "${it.userId}")
            }
            analyticsHelper.setUserProperty("user_role", "asha")
            analyticsHelper.setUserProperty("app_version", BuildConfig.VERSION_NAME)
            analyticsHelper.logEvent(FirebaseAnalytics.Event.APP_OPEN, params)

        }

    }

    fun askForPermissions() {

        val permissions = arrayOf<String>(
            Manifest.permission.CALL_PHONE,
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

    }



   private fun displaychatdialog() {

        val dialog = BottomSheetDialog(this)

        // on below line we are inflating a layout file which we have created.
        val view = layoutInflater.inflate(R.layout.bottomsheet_chat_window, null)

        val web = view.findViewById<WebView>(R.id.webv)
        val progress = view.findViewById<ProgressBar>(R.id.progressBarv)


       web.setWebChromeClient(object : WebChromeClient() {
           override fun onPermissionRequest(request: PermissionRequest) {
               request.grant(request.resources)
           }
       })



// Enable JavaScript
        web.settings.javaScriptEnabled = true
        web.settings.javaScriptCanOpenWindowsAutomatically = true
        web.isVerticalScrollBarEnabled = true




// Load URL
       web.loadUrl(KeyUtils.chatUrl())


// Handle WebView events
       web.webViewClient = object : WebViewClient() {
           override fun shouldOverrideUrlLoading(
               view: WebView,
               request: WebResourceRequest
           ): Boolean {
               return if (request.url.host == URI(KeyUtils.chatUrl()).host) {
                   false  // Let WebView handle same-origin URLs
               } else {
                   startActivity(Intent(Intent.ACTION_VIEW, request.url))
                   true
               }
           }

           override fun onReceivedError(
               view: WebView?,
               request: WebResourceRequest?,
               error: WebResourceError?
           ) {
               super.onReceivedError(view, request, error)
               progress.visibility = View.GONE
               // Show error view
               Toast.makeText(
                   this@HomeActivity,
                   R.string.chat_error,
                   Toast.LENGTH_SHORT
               ).show()
           }

            override fun onPageStarted(webview: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(webview, url, favicon)
                // Show ProgressBar when the page starts loading
                progress.visibility = View.VISIBLE
                web.visibility = View.GONE
            }

            override fun onPageFinished(webview: WebView, url: String) {
                super.onPageFinished(webview, url)
                // Hide ProgressBar when the page finishes loading
                progress.visibility = View.GONE
                web.visibility = View.VISIBLE
            }
        }


        // on below line we are creating a variable for our button
        // which we are using to dismiss our dialog.
        // on below line we are adding on click listener
        // for our dismissing the dialog button.

        // below line is use to set cancelable to avoid
        // closing of dialog box when clicking on the screen.
        dialog.setCancelable(true)

        // on below line we are setting
        // content view to our view.
        dialog.setContentView(view)
     //  dialog.behavior.setPeekHeight(6000)

       val displayMetrics = resources.displayMetrics
       val screenHeight = displayMetrics.heightPixels
       dialog.behavior.setPeekHeight((screenHeight * 0.85).toInt())


        // on below line we are calling
        // a show method to display a dialog.


        dialog.show()


        }




    override fun onResume() {
        // This will block user to cast app screen
        if (BuildConfig.FLAVOR.equals("niramay", true) ||BuildConfig.FLAVOR.equals("xushrukha", true) || BuildConfig.FLAVOR.equals("saksham", true))  {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
        super.onResume()
        if (isDeviceRootedOrEmulator()) {
            AlertDialog.Builder(this)
                .setTitle("Unsupported Device")
                .setMessage("This app cannot run on rooted devices or emulators.")
                .setCancelable(false)
                .setPositiveButton("Exit") { dialog, id -> finish() }
                .show()
        }
        binding.versionName.text ="${BuildConfig.VERSION_NAME}" //"APK Version 2.2.3"
        inAppUpdateHelper.resumeUpdateIfNeeded()
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

        binding.navView.menu.findItem(R.id.homeFragment).setOnMenuItemClickListener {
            navController.popBackStack(R.id.homeFragment, false)
            binding.drawerLayout.close()
            true

        }
        binding.navView.menu.findItem(R.id.sync_pending_records).setOnMenuItemClickListener {
            if (SystemClock.elapsedRealtime() - lastClickTime < 900000L) {
                Toast.makeText(this, "Please wait Syncing in Progress", Toast.LENGTH_SHORT).show()
                return@setOnMenuItemClickListener true
            }

            // Save the click time
            lastClickTime = SystemClock.elapsedRealtime()

            WorkerUtils.triggerAmritPushWorker(this)
            if (!pref.isFullPullComplete)
                WorkerUtils.triggerAmritPullWorker(this)
            binding.drawerLayout.close()
            true

        }

        binding.navView.menu.findItem(R.id.ChatFragment).setOnMenuItemClickListener {
//            navController.popBackStack(R.id.lmsFragment, false)
            navController.navigate(R.id.lmsFragment)

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
        binding.navView.menu.findItem(R.id.menu_delete_account).setOnMenuItemClickListener {
            var url = ""
            if (BuildConfig.FLAVOR.equals("saksham", true) ||BuildConfig.FLAVOR.equals("niramay", true) || BuildConfig.FLAVOR.equals("xushrukha", true))  {
                url = "https://forms.office.com/r/HkE3c0tGr6"
            } else {
                url =
                    "https://forms.office.com/Pages/ResponsePage.aspx?id=jQ49md0HKEGgbxRJvtPnRISY9UjAA01KtsFKYKhp1nNURUpKQzNJUkE1OUc0SllXQ0IzRFVJNlM2SC4u"
            }

            if (url.isNotEmpty()){
                val i = Intent(Intent.ACTION_VIEW)
                i.setData(Uri.parse(url))
                startActivity(i)
            }
            binding.drawerLayout.close()
            true

        }

        binding.navView.menu.findItem(R.id.menu_support).setOnMenuItemClickListener {
            var url = "https://forms.office.com/r/AqY1KqAz3v"
            if (url.isNotEmpty()){
                val i = Intent(Intent.ACTION_VIEW)
                i.setData(Uri.parse(url))
                startActivity(i)
            }
            binding.drawerLayout.close()
            true

        }

        if (isChatSupportEnabled) {
            binding.navView.menu.findItem(R.id.ChatFragment).setVisible(true)
            binding.navView.menu.findItem(R.id.ChatFragment).setOnMenuItemClickListener {
                displaychatdialog()
                /*navController.popBackStack(R.id.homeFragment, false)
            startActivity(Intent(this, ChatSupport::class.java))*/
                binding.drawerLayout.close()
                true

            }
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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START))
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        else super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        inAppUpdateHelper.unregisterListener()
        _binding = null
    }

    private fun isDeviceRootedOrEmulator(): Boolean {

//      return isRooted() || isEmulator() || RootedUtil().isDeviceRooted(applicationContext)
        return isRooted() || isEmulator()

    }

    override fun ApiUpdate() {
        try {
            Log.e("AAAAAMessage","ApiUpdate")
//            mChatMessageUpdate.apiUpdate()
        } catch (e: Exception) {
            Log.e("AAAAAMessage","ApiUpdate $e")
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (inAppUpdateHelper.onActivityResult(requestCode, resultCode)) {
            // Handled update result
            return
        }
        // Handle other activity results here if needed
    }

}
