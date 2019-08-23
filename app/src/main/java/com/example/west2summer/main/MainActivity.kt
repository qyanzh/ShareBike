package com.example.west2summer.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.example.west2summer.R
import com.example.west2summer.databinding.ActivityMainBinding
import com.example.west2summer.databinding.NavHeaderBinding
import com.example.west2summer.handleImage
import com.example.west2summer.user.User

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var navBinding: NavHeaderBinding
    val navViewModel: HeaderViewModel by lazy {
        ViewModelProviders.of(this).get(HeaderViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )
        val navView = binding.navigationView.getHeaderView(0)
        navBinding = NavHeaderBinding.bind(navView)
        navBinding.viewModel = navViewModel
        navBinding.logoutButton.setOnClickListener {
            User.setCurrentUser(null)
            Toast.makeText(applicationContext, "已退出登录", Toast.LENGTH_SHORT).show()
        }
        navBinding.loginInfo.setOnClickListener {
            if (!User.isLoginned()) {
                findNavController(R.id.nav_host_fragment).navigate(R.id.action_global_loginFragment)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
        }
        navBinding.navHeaderImg.setOnClickListener {
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
        navBinding.lifecycleOwner = this
        navBinding.invalidateAll()
        setupNavigation()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 0) {
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
        val navController = findNavController(R.id.nav_host_fragment)
        return NavigationUI.navigateUp(navController, binding.drawerLayout)
    }

    private fun setupNavigation() {
        //find the nav controller
        val navController = findNavController(R.id.nav_host_fragment)

        //setup the action bar, tell it about the DrawerLayout
        setSupportActionBar(binding.toolbar)
        NavigationUI.setupActionBarWithNavController(this, navController, binding.drawerLayout)

        //setup the left drawer
        NavigationUI.setupWithNavController(binding.navigationView, navController)

    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

}


