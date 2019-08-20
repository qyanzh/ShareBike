package com.example.west2summer.login


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.west2summer.R
import com.example.west2summer.databinding.LoginFragmentBinding


class LoginFragment : Fragment() {

    private lateinit var binding: LoginFragmentBinding

    private val viewModel: LoginViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProviders.of(this, LoginViewModel.Factory(activity.application))
            .get(LoginViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.login_fragment, container, false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        subscribeUi()
        viewModel.autoComplete()
        return binding.root
    }

    private fun subscribeUi() {
        viewModel.message.observe(this, Observer {
            if (!it.isNullOrBlank()) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        })
        viewModel.loginSuccess.observe(this, Observer { loginSuccess ->
            if (loginSuccess) {
                Toast.makeText(context, "登陆成功", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        })
        viewModel.shouldNavigateToRegister.observe(this, Observer { should ->
            if (should) {
                findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
                viewModel.onRegisterNavigated()
            }
        })
    }

}
