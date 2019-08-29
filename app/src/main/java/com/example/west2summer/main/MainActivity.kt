package com.example.west2summer.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.example.west2summer.R
import com.example.west2summer.component.*
import com.example.west2summer.databinding.ActivityMainBinding
import com.example.west2summer.databinding.ActivityMainNavHeaderBinding
import com.example.west2summer.source.Repository
import com.example.west2summer.source.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {


    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)
    lateinit var binding: ActivityMainBinding
    lateinit var navBinding: ActivityMainNavHeaderBinding

    private val navViewModel: MainNavHeaderViewModel by lazy {
        ViewModelProviders.of(this).get(MainNavHeaderViewModel::class.java)
    }

    //find the nav controller
    private val navController by lazy {
        findNavController(R.id.nav_host_fragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Repository.init(application)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )
        val navView = binding.navigationView.getHeaderView(0)
        navBinding = ActivityMainNavHeaderBinding.bind(navView)
        navBinding.viewModel = navViewModel
        subscribeUi()
        navBinding.lifecycleOwner = this
        navBinding.invalidateAll()
        setupNavigation()
        if (savedInstanceState == null) {
            uiScope.launch {
                autoLogin()
                refreshList()
            }
        }
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
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_NAV_HEADER)
        }
    }

    private suspend fun autoLogin() {
        try {
            Repository.autoLogin()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun refreshList() {
        try {
            Repository.refreshBikeList()
        } catch (e: Exception) {
            toast(this, getString(R.string.network_error))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_NAV_HEADER) {
            data?.let {
                val image = handleImage(this, data)
                Glide.with(this).load(image).into(navBinding.navHeaderImg)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, binding.drawerLayout)
    }

    private fun setupNavigation() {

        //setup the action bar, tell it about the DrawerLayout
        setSupportActionBar(binding.toolbar)
        NavigationUI.setupActionBarWithNavController(this, navController, binding.drawerLayout)

        //setup the left drawer
        MyNavigationUI.setupWithNavController(binding.navigationView, navController)

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
        outState.putBoolean("firstIn", false)
    }

}
