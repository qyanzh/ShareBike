package com.example.west2summer.dialog

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.west2summer.R
import com.example.west2summer.database.User
import com.example.west2summer.databinding.BikeInfoDialogFragmentBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class BikeInfoDialogFragment : BottomSheetDialogFragment() {

    lateinit var binding: BikeInfoDialogFragmentBinding

    private val viewModel: BikeInfoDialogViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProviders.of(
            this,
            BikeInfoDialogViewModel.Factory(
                activity.application,
                BikeInfoDialogFragmentArgs.fromBundle(arguments!!).bikeinfo
            )
        ).get(BikeInfoDialogViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet =
                dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
            BottomSheetBehavior.from(bottomSheet!!).state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BikeInfoDialogFragmentBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        subscribeUi()
        return binding.root
    }

    private fun subscribeUi() {
        viewModel.fabState.observe(this, Observer {
            binding.fab.setImageResource(
                when (it) {
                    0 -> R.drawable.ic_favorite_border_black_24dp
                    1 -> R.drawable.ic_favorite_black_24dp
                    else -> R.drawable.ic_mode_edit_black_24dp
                }
            )
        })
        binding.fab.setOnClickListener {
            when (viewModel.fabState.value) {
                0 -> showLikeDialog()
                1 -> undoLike()
                else -> findNavController().navigate(
                    BikeInfoDialogFragmentDirections.actionBikeInfoDialogToEditBikeInfoFragment(
                        viewModel.bikeInfo
                    )
                )
            }
        }
        binding.buttonShowContact.setOnClickListener {
            val owner = viewModel.getUserContact()
            showContactDialog(owner)
        }
    }

    private fun showLikeDialog() {
        MaterialAlertDialogBuilder(context, R.style.AlertDialogTheme)
            .setTitle("想租这辆车吗？")
            .setMessage("你和车主将可以看到对方的联系方式")
            .setPositiveButton("想租") { dialog, _ ->
                dialog.dismiss()
                val owner = viewModel.sendLikeRequest()
                showContactDialog(owner)
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun undoLike() {
        //TODO:向服务器发送取消想租申请
        viewModel.sendUndoLikeRequest()
        Toast.makeText(context, "已取消", Toast.LENGTH_SHORT).show()
    }

    private fun showContactDialog(user: User) {
        val contactList = mutableListOf<String>()
        user.qq?.let {
            contactList.add("QQ: ${user.qq} (点击跳转)")
        }
        user.wechat?.let {
            contactList.add("微信: ${user.wechat} (点击复制)")
        }
        user.phone?.let {
            contactList.add("手机: ${user.phone} (点击跳转)")
        }
        MaterialAlertDialogBuilder(context, R.style.AlertDialogTheme)
            .setTitle("车主联系方式")
            .setItems(contactList.toTypedArray()) { _, position ->
                when {
                    contactList[position].startsWith("QQ") -> try {
                        val url = "mqqwpa://im/chat?chat_type=wpa&uin=${user.qq}"
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (e: Exception) {
                        Toast.makeText(context, "未安装QQ", Toast.LENGTH_SHORT).show()
                    }

                    contactList[position].startsWith("微信") -> {
                        (context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                            .setPrimaryClip(ClipData.newPlainText("wechat", user.wechat))
                        Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
                    }

                    contactList[position].startsWith("手机") -> try {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${user.phone}"))
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    } catch (e: Exception) {
                        (context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                            .setPrimaryClip(ClipData.newPlainText("phone", user.phone))
                        Toast.makeText(context, "未找到电话应用, 已复制到剪切板", Toast.LENGTH_SHORT).show()
                    }
                }
            }.show()
    }


}