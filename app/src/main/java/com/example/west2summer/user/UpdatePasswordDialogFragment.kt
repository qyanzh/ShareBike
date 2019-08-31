package com.example.west2summer.user

import android.app.Dialog
import android.content.DialogInterface.BUTTON_POSITIVE
import android.os.Bundle
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.west2summer.R
import com.example.west2summer.component.toast
import com.example.west2summer.databinding.UpdatePasswordDialogFragmentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class UpdatePasswordDialogFragment : DialogFragment() {

    lateinit var binding: UpdatePasswordDialogFragmentBinding

    private val viewModel: UpdatePasswordDialogViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProviders.of(
            this, UpdatePasswordDialogViewModel.Factory(activity.application)
        ).get(UpdatePasswordDialogViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.update_password_dialog_fragment, null, false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        subscribeUi()
        val builder = MaterialAlertDialogBuilder(context!!, R.style.AlertDialogTheme)
            .setView(binding.root)
            .setTitle(R.string.modify_password)
            .setPositiveButton(getString(R.string.confirm_modify)) { _, _ ->

            }.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
        val dialog = builder.create()
        dialog.setOnShowListener {
            val button = dialog.getButton(BUTTON_POSITIVE)
            button.setOnClickListener {
                viewModel.onConfirmClicked()
            }
        }
        return dialog
    }


    private fun subscribeUi() {
        viewModel.message.observe(this, Observer {
            it?.let {
                toast(context!!, it)
                viewModel.onMessageShowed()
            }
        })
        viewModel.modifySuccess.observe(this, Observer {
            it?.let { success ->
                if (success) {
                    dialog?.dismiss()
                    viewModel.onModifySuccess()
                }
            }
        })
    }

}
