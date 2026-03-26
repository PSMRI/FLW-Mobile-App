package org.piramalswasthya.sakhi.ui.asha_supervisor

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.crashlytics.internal.common.CommonUtils.isEmulator
import com.google.firebase.crashlytics.internal.common.CommonUtils.isRooted
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.ActivitySupervisorBinding
import org.piramalswasthya.sakhi.helpers.ImageUtils
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.helpers.MyContextWrapper
import org.piramalswasthya.sakhi.helpers.TapjackingProtectionHelper
import org.piramalswasthya.sakhi.helpers.isInternetAvailable
import org.piramalswasthya.sakhi.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.SupervisorViewModel
import org.piramalswasthya.sakhi.ui.home_activity.sync.SyncBottomSheetFragment
import org.piramalswasthya.sakhi.ui.login_activity.LoginActivity
import org.piramalswasthya.sakhi.work.WorkerUtils
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class SupervisorActivity : AppCompatActivity() {

    private var lastAutoTriggerPushTime: Long = 0L
    private companion object {
        const val AUTO_PUSH_DEBOUNCE_MS = 120_000L  // 2 minutes
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WrapperEntryPoint {
        val pref: PreferenceDao
    }

    @Inject
    lateinit var pref: PreferenceDao

    private var _binding: ActivitySupervisorBinding? = null

    private val binding: ActivitySupervisorBinding
        get() = _binding!!


    private val syncBottomSheet: SyncBottomSheetFragment by lazy {
        SyncBottomSheetFragment()
    }


    private val viewModel: SupervisorViewModel by viewModels()

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

                    val restart = Intent(this, SupervisorActivity::class.java)
                    finish()
                    startActivity(restart)
                }

            }.create()
    }


    private val logoutAlert by lazy {
        var str = ""
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
            }
        }

    private val navController by lazy {
        val navHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_supervisor) as NavHostFragment
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
        _binding = ActivitySupervisorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        TapjackingProtectionHelper.enableTouchFiltering(this)

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
            if (it > 0) {
                val now = SystemClock.elapsedRealtime()
                if (now - lastAutoTriggerPushTime >= AUTO_PUSH_DEBOUNCE_MS) {
                    if (isInternetAvailable(this)) {
                        lastAutoTriggerPushTime = now
                        WorkerUtils.triggerAmritPushWorker(this)
                    }
                }
            }
        }
        binding.versionName.text = "APK Version ${BuildConfig.VERSION_NAME}"
    }


//    private fun displaychatdialog() {
//
//        val dialog = BottomSheetDialog(this)
//
//        // on below line we are inflating a layout file which we have created.
//        val view = layoutInflater.inflate(R.layout.bottomsheet_chat_window, null)
//
//        val web = view.findViewById<WebView>(R.id.webv)
//        val progress = view.findViewById<ProgressBar>(R.id.progressBarv)
//
//
//        web.setWebChromeClient(object : WebChromeClient() {
//            override fun onPermissionRequest(request: PermissionRequest) {
//                request.grant(request.resources)
//            }
//        })
//
//
//// Enable JavaScript
//        web.settings.javaScriptEnabled = true
//        web.settings.javaScriptCanOpenWindowsAutomatically = true
//        web.isVerticalScrollBarEnabled = true
//
//
//// Load URL
//        web.loadUrl(KeyUtils.chatUrl())
//
//
//// Handle WebView events
//        web.webViewClient = object : WebViewClient() {
//            override fun shouldOverrideUrlLoading(
//                view: WebView,
//                request: WebResourceRequest
//            ): Boolean {
//                return if (request.url.host == URI(KeyUtils.chatUrl()).host) {
//                    false  // Let WebView handle same-origin URLs
//                } else {
//                    startActivity(Intent(Intent.ACTION_VIEW, request.url))
//                    true
//                }
//            }
//
//            override fun onReceivedError(
//                view: WebView?,
//                request: WebResourceRequest?,
//                error: WebResourceError?
//            ) {
//                super.onReceivedError(view, request, error)
//                progress.visibility = View.GONE
//                // Show error view
//                Toast.makeText(
//                    this@SupervisorActivity,
//                    R.string.chat_error,
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//
//            override fun onPageStarted(webview: WebView, url: String, favicon: Bitmap?) {
//                super.onPageStarted(webview, url, favicon)
//                // Show ProgressBar when the page starts loading
//                progress.visibility = View.VISIBLE
//                web.visibility = View.GONE
//            }
//
//            override fun onPageFinished(webview: WebView, url: String) {
//                super.onPageFinished(webview, url)
//                // Hide ProgressBar when the page finishes loading
//                progress.visibility = View.GONE
//                web.visibility = View.VISIBLE
//            }
//        }
//
//
//        // on below line we are creating a variable for our button
//        // which we are using to dismiss our dialog.
//        // on below line we are adding on click listener
//        // for our dismissing the dialog button.
//
//        // below line is use to set cancelable to avoid
//        // closing of dialog box when clicking on the screen.
//        dialog.setCancelable(true)
//
//        // on below line we are setting
//        // content view to our view.
//        dialog.setContentView(view)
//        //  dialog.behavior.setPeekHeight(6000)
//
//        val displayMetrics = resources.displayMetrics
//        val screenHeight = displayMetrics.heightPixels
//        dialog.behavior.setPeekHeight((screenHeight * 0.85).toInt())
//
//
//        // on below line we are calling
//        // a show method to display a dialog.
//
//
//        dialog.show()
//
//
//    }


    override fun onResume() {
        super.onResume()
        window.decorView.alpha = 1f
        if (isDeviceRootedOrEmulator()) {
            AlertDialog.Builder(this)
                .setTitle("Unsupported Device")
                .setMessage("This app cannot run on rooted devices or emulators.")
                .setCancelable(false)
                .setPositiveButton("Exit") { dialog, id -> finish() }
                .show()
        }
        binding.versionName.text = "APK Version ${BuildConfig.VERSION_NAME}"
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
                        navController.popBackStack(R.id.supervisorFragment, false)
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

    fun removeClickListenerToHomepageActionBarTitle() {
        binding.toolbar.setOnClickListener(null)
        binding.toolbar.subtitle = null
    }

    fun setHomeMenuItemVisibility(show: Boolean) {
        showMenuHome = show
        invalidateOptionsMenu()
    }

    private fun setUpFirstTimePullWorker() {
        WorkerUtils.triggerPeriodicPncEcUpdateWorker(this)
        if (!pref.isFullPullComplete)
            WorkerUtils.triggerAmritPullWorker(this)
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
                R.id.supervisorFragment
            )
        ).setOpenableLayout(binding.drawerLayout).build()

        NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfiguration)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        binding.navView.menu.findItem(R.id.supervisorFragment).setOnMenuItemClickListener {
            navController.popBackStack(R.id.supervisorFragment, false)
            binding.drawerLayout.close()
            true
        }

        binding.navView.menu.findItem(R.id.sync_pending_records).setOnMenuItemClickListener {
            WorkerUtils.triggerAmritPushWorker(this)
            if (!pref.isFullPullComplete)
                WorkerUtils.triggerAmritPullWorker(this)
            binding.drawerLayout.close()
            true

        }

        binding.navView.menu.findItem(R.id.menu_logout).setOnMenuItemClickListener {
            logoutAlert.show()
            true

        }

        binding.navView.menu.findItem(R.id.ChatFragment).setOnMenuItemClickListener {
            navController.navigate(R.id.lmsFragment)

            binding.drawerLayout.close()
            true

        }
//
        binding.navView.menu.findItem(R.id.abha_id_activity).setOnMenuItemClickListener {
            navController.popBackStack(R.id.supervisorFragment, false)
            startActivity(Intent(this, AbhaIdActivity::class.java))
            binding.drawerLayout.close()
            true

        }
        binding.navView.menu.findItem(R.id.menu_delete_account).setOnMenuItemClickListener {
            var url = ""
            if (BuildConfig.FLAVOR.equals("sakshamProd", true) ||BuildConfig.FLAVOR.equals("niramayProd", true) || BuildConfig.FLAVOR.equals("xushrukhaProd", true))  {
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

    }


    fun updateActionBar(logoResource: Int, title: String? = null) {
        binding.ivToolbar.setImageResource(logoResource)
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
        _binding = null
    }

    private fun isDeviceRootedOrEmulator(): Boolean {
        return isRooted() || isEmulator()
    }

}
