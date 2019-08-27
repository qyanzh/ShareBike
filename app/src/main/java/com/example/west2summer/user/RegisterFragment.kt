package com.example.west2summer.user

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.west2summer.R
import com.example.west2summer.component.EditBaseFragment
import com.example.west2summer.databinding.RegisterFragmentBinding
import com.example.west2summer.main.MainActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RegisterFragment : EditBaseFragment() {
    private lateinit var binding: RegisterFragmentBinding

    private val viewModel: RegisterViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProviders.of(
            this,
            RegisterViewModel.Factory(activity.application)
        )
            .get(RegisterViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = RegisterFragmentBinding.inflate(inflater)
        (activity as MainActivity).apply {
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_black_24dp)
        }
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        subscribeUi()
        return binding.root
    }

    private fun subscribeUi() {
        binding.sex.setOnClickListener {
            hideKeyboard()
            MaterialAlertDialogBuilder(context, R.style.AlertDialogTheme)
                .setTitle(getString(R.string.choose_sex))
                .setItems(
                    arrayOf(getString(R.string.man), getString(R.string.woman))
                ) { _, position ->
                    viewModel.onSexPicked(position)
                }.show()
        }
        viewModel.message.observe(this, Observer {
            if (!it.isNullOrBlank()) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        })
        viewModel.registerSuccess.observe(this, Observer { loginSuccess ->
            if (loginSuccess) {
                Toast.makeText(context, "注册成功", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
                viewModel.onRegistered()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.register_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.register -> viewModel.onRegisterClicked()

            android.R.id.home -> requireActivity().onBackPressed()
        }
        return true
    }

}