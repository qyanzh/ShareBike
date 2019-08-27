package com.example.west2summer.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.example.west2summer.R
import com.example.west2summer.component.MyNavigationUI
import com.example.west2summer.component.REQUEST_NAV_HEADER
import com.example.west2summer.component.handleImage
import com.example.west2summer.database.Network
import com.example.west2summer.databinding.ActivityMainBinding
import com.example.west2summer.databinding.ActivityMainNavHeaderBinding
import com.example.west2summer.user.User
import kotlinx.coroutines.*
import java.net.ConnectException

class MainActivity : AppCompatActivity() {


    lateinit var binding: ActivityMainBinding
    lateinit var navBinding: ActivityMainNavHeaderBinding
    private val navViewModel: MainNavHeaderViewModel by lazy {
        ViewModelProviders.of(this).get(MainNavHeaderViewModel::class.java)
    }
    //find the nav controller
    val navController by lazy {
        findNavController(R.id.nav_host_fragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )
        val navView = binding.navigationView.getHeaderView(0)
        navBinding = ActivityMainNavHeaderBinding.bind(navView)
        navBinding.viewModel = navViewModel
        navBinding.logoutButton.setOnClickListener {
            User.logout()
            Toast.makeText(applicationContext, "已退出登录", Toast.LENGTH_SHORT).show()
        }
        navBinding.loginInfo.setOnClickListener {
            if (!User.isLoginned()) {
                val builder = NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setEnterAnim(R.anim.nav_default_enter_anim)
                    .setExitAnim(R.anim.nav_default_exit_anim)
                    .setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
                    .setPopExitAnim(R.anim.nav_default_pop_exit_anim)
                val options = builder.build()
                navController.navigate(R.id.action_global_loginFragment, null, options)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
        }
        navBinding.navHeaderImg.setOnClickListener {
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_NAV_HEADER)
        }
        navBinding.lifecycleOwner = this
        navBinding.invalidateAll()
        setupNavigation()
        autoLogin()
    }

    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    private fun autoLogin() {
        val spf = getSharedPreferences("user", Context.MODE_PRIVATE)
        if (spf.contains("id") && spf.contains("password")) {
            uiScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        val id = spf.getString("id", "")!!.toLong()
                        val password = spf.getString("password", "")!!
                        if (Network.service.login(id, password).await().data == "success") {
                            User.setCurrentUser(Network.service.getUserInfo(id).await().user!!)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    applicationContext,
                                    getString(R.string.auto_login_success),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            throw Exception(getString(R.string.auto_login_failed))
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            if (e is ConnectException) {
                                Toast.makeText(
                                    applicationContext,
                                    getString(R.string.exam_network),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_NAV_HEADER) {
            data?.let {
                Log.d(
                    "MainActivity", "onActivityResult: " +
                            "$it"
                )
                val image = handleImage(this, data)
                Log.d(
                    "MainActivity", "onActivityResult: " +
                            "$image"
                )
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


}

