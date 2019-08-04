package com.example.west2summer.edit

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.example.west2summer.MainActivity
import com.example.west2summer.R
import com.example.west2summer.databinding.EditBikeInfoFragmentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*

class EditBikeInfoFragment : Fragment() {

    private lateinit var binding: EditBikeInfoFragmentBinding

    private val viewModel: EditBikeInfoViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProviders.of(
            this,
            EditBikeInfoViewModel.Factory(
                activity.application,
                EditBikeInfoFragmentArgs.fromBundle(arguments!!).bikeinfo
            )
        )
            .get(EditBikeInfoViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            MaterialAlertDialogBuilder(context, R.style.AlertDialogTheme)
                .setMessage(getString(R.string.discard_change))
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = EditBikeInfoFragmentBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        (activity as MainActivity).apply {
            binding.drawerLayout.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED)
            supportActionBar?.apply {
                setHomeAsUpIndicator(R.drawable.ic_close_black_24dp)
                title = viewModel.mode
            }
        }

        subscribeUi()

        return binding.root
    }

    private fun subscribeUi() {
        viewModel.shouldOpenPicker.observe(this, Observer {
            hideKeyboard()
            if (it > 0) {
                val pvBuilder = TimePickerBuilder(context) { date: Date, _: View? ->
                    val c = Calendar.getInstance()
                    c.time = date
                    viewModel.onPickerShowed(c)
                }.addOnCancelClickListener {
                    viewModel.onPickerShowed(null)
                }.setType(booleanArrayOf(true, true, true, true, true, false))
                    .setSubmitColor(ContextCompat.getColor(context!!, R.color.primaryTextColor))
                    .setCancelColor(ContextCompat.getColor(context!!, R.color.primaryTextColor))
                    .setCancelText(getString(R.string.clear))
                when (it) {
                    1 -> viewModel.preuiFrom.value?.let { c -> pvBuilder.setDate(c) }
                    2 -> viewModel.preuiTo.value?.let { c -> pvBuilder.setDate(c) }
                }
                pvBuilder.build().setOnDismissListener {
                    binding.rootLinear.requestFocus()
                }.show()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit_done -> {
                if (viewModel.onDoneMenuClicked()) {
                    findNavController().navigateUp()
                }
            }
            android.R.id.home -> requireActivity().onBackPressed()
        }
        return true
    }

    override fun onStop() {
        (activity as MainActivity).binding.apply {
            drawerLayout.setDrawerLockMode(LOCK_MODE_UNLOCKED)
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
