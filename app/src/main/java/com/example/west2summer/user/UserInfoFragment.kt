package com.example.west2summer.user

import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.west2summer.R
import com.example.west2summer.component.EditBaseFragment
import com.example.west2summer.component.toast
import com.example.west2summer.databinding.UserInfoFragmentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class UserInfoFragment : EditBaseFragment() {

    private lateinit var binding: UserInfoFragmentBinding

    private val viewModel: UserInfoViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProviders.of(
            this, UserInfoViewModel.Factory(activity.application)
        ).get(UserInfoViewModel::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = UserInfoFragmentBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        subscribeUi()
        return binding.root
    }

    private fun subscribeUi() {
        binding.sex.setOnClickListener {
            hideKeyboard()
            MaterialAlertDialogBuilder(context, R.style.AlertDialogTheme)
                .setTitle(getString(R.string.please_choose_sex))
                .setItems(
                    arrayOf(getString(R.string.man), getString(R.string.woman))
                ) { _, position ->
                    viewModel.onSexPicked(position)
                }.show()
        }
        viewModel.message.observe(this, Observer {
            it?.let {
                toast(context!!, it)
                viewModel.onMessageShowed()
            }
        })
        viewModel.modifySuccess.observe(this, Observer { success ->
            if (success == true) {
                findNavController().navigateUp()
                viewModel.onModifySuccess()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.user_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.modify_done -> {
                viewModel.onDoneClicked()
            }
            R.id.change_password -> {
                //TODO:修改密码
            }

            android.R.id.home -> requireActivity().onBackPressed()
        }
        return true
    }
}
