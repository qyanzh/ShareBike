package com.example.west2summer.edit

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.example.west2summer.R
import com.example.west2summer.component.*
import com.example.west2summer.databinding.BikeEditFragmentBinding
import com.example.west2summer.main.MainActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*

class BikeEditFragment : EditBaseFragment() {

    private lateinit var binding: BikeEditFragmentBinding

    private val viewModel: BikeEditViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProviders.of(
            this,
            BikeEditViewModel.Factory(
                activity.application,
                BikeEditFragmentArgs.fromBundle(arguments!!).bikeinfo
            )
        ).get(BikeEditViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = BikeEditFragmentBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        (activity as MainActivity).supportActionBar?.title =
            if (viewModel.mode == EditState.ADD)
                resources.getString(R.string.toolbar_add)
            else
                resources.getString(R.string.toolbar_edit)

        subscribeUi()

        return binding.root
    }

    private fun subscribeUi() {
        viewModel.editSuccess.observe(this, Observer { success ->
            success?.let {
                if (success) {
                    findNavController().navigateUp()
                    viewModel.onSuccess()
                }
            }
        })
        viewModel.message.observe(this, Observer { msg ->
            msg?.let {
                toast(context!!, msg)
                viewModel.onMessageShowed()
            }
        })
        viewModel.shouldOpenPicker.observe(this, Observer {
            hideKeyboard()
            it?.let {
                if (it > 0) {
                    val pvBuilder = TimePickerBuilder(context) { date: Date, _: View? ->
                        val c = Calendar.getInstance()
                        c.time = date
                        viewModel.onTimePicked(c)
                    }.addOnCancelClickListener {
                        viewModel.onTimePicked(null)
                    }.setType(booleanArrayOf(true, true, true, true, true, false))
                        .setSubmitColor(ContextCompat.getColor(context!!, R.color.primaryTextColor))
                        .setCancelColor(ContextCompat.getColor(context!!, R.color.primaryTextColor))
                        .setCancelText(getString(R.string.clear))
                    when (it) {
                        1 -> viewModel.preUiFrom.value?.let { c -> pvBuilder.setDate(c) }
                        2 -> viewModel.preUiTo.value?.let { c -> pvBuilder.setDate(c) }
                    }
                    pvBuilder.build().setOnDismissListener {
                        binding.rootLinear.requestFocus()
                        viewModel.onTimePicked(null)
                    }.show()
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_menu, menu)
        menu.getItem(0).isVisible = viewModel.mode == EditState.EDIT
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit_done -> {
                viewModel.onDoneClicked()
            }
            R.id.edit_delete -> {
                MaterialAlertDialogBuilder(context, R.style.AlertDialogTheme)
                    .setMessage(getString(R.string.confirm_delete))
                    .setPositiveButton(getString(R.string.delete)) { _, _ ->
                        viewModel.onDeleteClicked()
                    }
                    .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                        dialog.dismiss()
                    }.show()
            }
            R.id.edit_image -> {
                if (viewModel.bikeImage.value == null) {
                    pickImage()
                } else {
                    MaterialAlertDialogBuilder(
                        context,
                        R.style.AlertDialogTheme
                    ).setItems(arrayOf("更换图片", "取消图片")) { _, position ->
                        when (position) {
                            0 -> pickImage()
                            1 -> viewModel.onImageCanceled()
                        }
                    }.show()
                }
            }
            android.R.id.home -> requireActivity().onBackPressed()
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_BIKE_IMAGE) {
            data?.let {
                handleImage(requireContext(), data)?.let {
                    viewModel.onImagePicked(it)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun pickImage() {
        val intent = Intent("android.intent.action.GET_CONTENT")
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_BIKE_IMAGE)
    }


}
