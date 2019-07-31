package com.example.west2summer.edit

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.west2summer.AutoSuggestAdapter
import com.example.west2summer.MainActivity
import com.example.west2summer.R
import com.example.west2summer.databinding.EditBikeInfoFragmentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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
            MaterialAlertDialogBuilder(context)
                .setMessage(getString(R.string.discard_change))
                .setPositiveButton(getString(R.string.discard)) { _, _ ->
                    findNavController().navigateUp()
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

        val adapter = AutoSuggestAdapter(
            context!!,
            R.layout.text_auto_complete, mutableListOf<String>()
        ).apply {
            binding.tvAutoComplete.setAdapter(this)
        }

        (activity as MainActivity).apply {
            binding.drawerLayout.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED)
            supportActionBar?.apply {
                setHomeAsUpIndicator(R.drawable.ic_close_black_24dp)
                title = viewModel.mode
            }
        }

        viewModel.uiPlace.observe(this, Observer {
            viewModel.refreshPlaceSuggestion()
        })

        viewModel.placeSuggestionsList.observe(this, Observer {
            adapter.clear()
            adapter.addAll(it)
            adapter.notifyDataSetChanged()
        })

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit_done -> viewModel.onDoneMenuClicked()
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
