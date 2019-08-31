package com.example.west2summer.main


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.example.west2summer.R
import com.example.west2summer.component.MyNavigationUI
import com.example.west2summer.component.defaultNavOptions
import com.example.west2summer.component.toast
import com.example.west2summer.databinding.ActivityMainBinding
import com.example.west2summer.databinding.ActivityMainNavHeaderBinding
import com.example.west2summer.source.Repository
import com.example.west2summer.source.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private var permissionRequestCount: Int = 0
    private val MAX_NUMBER_REQUEST_PERMISSIONS = 2
    private val REQUEST_CODE_AVATAR = 100
    private val REQUEST_CODE_PERMISSIONS = 101
    private val KEY_PERMISSIONS_REQUEST_COUNT = "KEY_PERMISSIONS_REQUEST_COUNT"

    private val permissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private val job = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    lateinit var binding: ActivityMainBinding

    lateinit var navBinding: ActivityMainNavHeaderBinding

    lateinit var navHeaderView: View

    private val navViewModel: MainNavHeaderViewModel by lazy {
        ViewModelProviders.of(this).get(MainNavHeaderViewModel::class.java)
    }

    private val navController by lazy {
        findNavController(R.id.nav_host_fragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )
        requestPermissionsIfNecessary()
        navHeaderView = binding.navigationView.getHeaderView(0)
        navBinding = ActivityMainNavHeaderBinding.bind(navHeaderView)
        subscribeUi()
        navBinding.viewModel = navViewModel
        navBinding.lifecycleOwner = this
        navBinding.invalidateAll()
        setupNavigation()

        savedInstanceState?.let {
            permissionRequestCount = it.getInt(KEY_PERMISSIONS_REQUEST_COUNT, 0)
        } ?: uiScope.launch { autoLogin() }
    }

    private fun subscribeUi() {
        navBinding.logoutButton.setOnClickListener {
            Repository.logout()
            navController.navigateUp()
            toast(applicationContext, getString(R.string.exit_success))
        }
        navBinding.loginInfo.setOnClickListener {
            if (!User.isLoginned()) {
                navController.navigate(R.id.action_global_loginFragment, null, defaultNavOptions)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
        }
        navBinding.navHeaderImg.setOnClickListener {
            val chooseIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(chooseIntent, REQUEST_CODE_AVATAR)
        }
    }

    private suspend fun autoLogin() {
        try {
            Repository.autoLogin()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupNavigation() {
        setSupportActionBar(binding.toolbar)
        NavigationUI.setupActionBarWithNavController(this, navController, binding.drawerLayout)
        MyNavigationUI.setupWithNavController(binding.navigationView, navController)
    }

    private fun requestPermissionsIfNecessary() {
        if (!checkAllPermissions()) {
            if (permissionRequestCount < MAX_NUMBER_REQUEST_PERMISSIONS) {
                permissionRequestCount += 1
                ActivityCompat.requestPermissions(
                    this,
                    permissions.toTypedArray(),
                    REQUEST_CODE_PERMISSIONS
                )
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.set_permissions_in_settings),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun checkAllPermissions(): Boolean {
        var hasPermissions = true
        for (permission in permissions) {
            hasPermissions = hasPermissions and isPermissionGranted(permission)
        }
        return hasPermissions
    }

    private fun isPermissionGranted(permission: String) =
        ContextCompat.checkSelfPermission(this, permission) ==
                PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            requestPermissionsIfNecessary() // no-op if permissions are granted already.
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_AVATAR -> data?.let { handleImageRequestResult(data) }
                else -> {
                }
            }
        } else {
            Timber.e("Unexpected Result code $resultCode")
        }
    }

    private fun handleImageRequestResult(intent: Intent) {
        // If clipdata is available, we use it, otherwise we use data
        val imageUri: Uri? = intent.clipData?.getItemAt(0)?.uri ?: intent.data
        Glide.with(this).load(imageUri).into(navBinding.navHeaderImg)
        if (imageUri == null) {
            Timber.e("Invalid input image Uri.")
            return
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, binding.drawerLayout)
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_PERMISSIONS_REQUEST_COUNT, permissionRequestCount)
    }

}
