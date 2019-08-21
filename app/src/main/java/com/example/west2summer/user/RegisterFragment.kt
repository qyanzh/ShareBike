package com.example.west2summer.user

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.west2summer.R
import com.example.west2summer.databinding.RegisterFragmentBinding
import com.example.west2summer.main.MainActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RegisterFragment : Fragment() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            MaterialAlertDialogBuilder(context, R.style.AlertDialogTheme)
                .setMessage(getString(R.string.cancel_register))
                .setPositiveButton(getString(R.string.discard)) { _, _ ->
                    try {
                        findNavController().navigateUp()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }.isEnabled = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.register_fragment, container, false
        )
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

    override fun onStop() {
        (activity as MainActivity).binding.apply {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        }
        hideKeyboard()
        super.onStop()
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}