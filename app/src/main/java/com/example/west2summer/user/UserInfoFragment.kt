package com.example.west2summer.user

import android.os.Bundle
import android.view.*
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProviders
import com.example.west2summer.R
import com.example.west2summer.component.EditBaseFragment
import com.example.west2summer.databinding.UserInfoFragmentBinding
import com.example.west2summer.main.MainActivity

class UserInfoFragment : EditBaseFragment() {

    private lateinit var binding: UserInfoFragmentBinding
    private val viewModel: UserInfoViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProviders.of(
            this
        ).get(UserInfoViewModel::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = UserInfoFragmentBinding.inflate(inflater)
        (activity as MainActivity).apply {
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_black_24dp)
        }
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.user_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit_done -> {
                viewModel.onDoneClicked()
                //TODO:提交个人信息
            }
            R.id.change_password -> {
                //TODO:修改密码
            }

            android.R.id.home -> requireActivity().onBackPressed()
        }
        return true
    }
}
