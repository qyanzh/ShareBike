package com.example.west2summer.dialog

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Point
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
import com.example.west2summer.component.LikeFabState
import com.example.west2summer.databinding.BikeDialogFragmentBinding
import com.example.west2summer.source.User
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class BikeDialogFragment : BottomSheetDialogFragment() {

    lateinit var binding: BikeDialogFragmentBinding

    private val viewModel: BikeDialogViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProviders.of(
            this,
            BikeDialogViewModel.Factory(
                activity.application,
                BikeDialogFragmentArgs.fromBundle(arguments!!).bikeinfo
            )
        ).get(BikeDialogViewModel::class.java)
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

            val display = activity?.windowManager?.defaultDisplay
            val point = Point()
            display?.getSize(point)
            val height = point.y
            BottomSheetBehavior.from(bottomSheet).apply {
                setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        if (slideOffset < 0) {
                            dialog.dismiss()
                        }
                    }

                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                    }

                })
                peekHeight = height
            }
            dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                height
            )
        }


//        dialog.window!!.setGravity(Gravity.BOTTOM)

//        BottomSheetBehavior.from(view).peekHeight = height + 70

        return dialog
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BikeDialogFragmentBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        subscribeUi()
        return binding.root
    }

    private fun subscribeUi() {
        viewModel.fabState.observe(this, Observer { state ->
            binding.fab.setImageResource(
                when (state) {
                    LikeFabState.UNLIKE -> R.drawable.ic_favorite_border_black_24dp
                    LikeFabState.LIKED -> R.drawable.ic_favorite_black_24dp
                    else -> R.drawable.ic_mode_edit_black_24dp
                }
            )
        })
        binding.fab.setOnClickListener {
            if (User.isLoginned()) {
                when (viewModel.fabState.value) {
                    LikeFabState.UNLIKE -> showLikeDialog()
                    LikeFabState.LIKED -> undoLike()
                    else -> findNavController().navigate(
                        BikeDialogFragmentDirections.actionBikeInfoDialogToEditBikeInfoFragment(
                            viewModel.bikeInfo
                        )
                    )
                }
            } else {
                Toast.makeText(context, "请先登录", Toast.LENGTH_SHORT).show()
                findNavController().navigate(BikeDialogFragmentDirections.actionGlobalLoginFragment())
            }
        }
        binding.wechat.setOnClickListener {
            copyWechat()
        }
        binding.qq.setOnClickListener {
            jumpToQQ()
        }
        binding.phone.setOnClickListener {
            jumpToPhone()
        }
    }

    private fun undoLike() {
        //TODO:向服务器发送取消想租申请
        viewModel.sendUndoLikeRequest()
        Toast.makeText(context, "已取消", Toast.LENGTH_SHORT).show()
    }

    private fun showLikeDialog() {
        MaterialAlertDialogBuilder(context, R.style.AlertDialogTheme)
            .setTitle("想租这辆车吗？")
            .setMessage("你和车主将可以看到对方的联系方式")
            .setPositiveButton("想租") { dialog, _ ->
                dialog.dismiss()
                viewModel.sendLikeRequest()
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun copyWechat() {
        (context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
            .setPrimaryClip(ClipData.newPlainText("wechat", viewModel.owner.value?.wechat))
        Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
    }

    private fun jumpToQQ() {
        try {
            val url = "mqqwpa://im/chat?chat_type=wpa&uin=${viewModel.owner.value?.qq}"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Toast.makeText(context, "未安装QQ", Toast.LENGTH_SHORT).show()
        }
    }

    private fun jumpToPhone() {
        try {
            val intent =
                Intent(Intent.ACTION_DIAL, Uri.parse("tel:${viewModel.owner.value?.phone}"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (e: Exception) {
            (context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                .setPrimaryClip(ClipData.newPlainText("phone", viewModel.owner.value?.phone))
            Toast.makeText(context, "未找到电话应用, 已复制到剪切板", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showContactDialog() {
        val contactList = mutableListOf<String>()
        viewModel.owner.value?.qq?.let {
            contactList.add("QQ: $it (点击跳转)")
        }
        viewModel.owner.value?.wechat?.let {
            contactList.add("微信: $it (点击复制)")
        }
        viewModel.owner.value?.phone?.let {
            contactList.add("手机: $it (点击跳转)")
        }
        MaterialAlertDialogBuilder(context, R.style.AlertDialogTheme)
            .setTitle("车主联系方式")
            .setItems(contactList.toTypedArray()) { _, position ->
                when {
                    contactList[position].startsWith("微信") -> copyWechat()
                    contactList[position].startsWith("QQ") -> jumpToQQ()
                    contactList[position].startsWith("手机") -> jumpToPhone()
                }
            }.show()
    }


}