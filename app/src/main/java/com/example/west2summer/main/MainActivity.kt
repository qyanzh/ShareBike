package com.example.west2summer.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.west2summer.R
import com.example.west2summer.databinding.ActivityMainBinding
import com.example.west2summer.databinding.NavHeaderBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )
        val navView = binding.navigationView.getHeaderView(0)
        val navBinding = NavHeaderBinding.bind(navView)
        navBinding.viewModel = ViewModelProviders.of(this).get(HeaderViewModel::class.java)
        navBinding.lifecycleOwner = this
        navBinding.invalidateAll()
        setupNavigation()
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
