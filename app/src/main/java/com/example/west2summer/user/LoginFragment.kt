package com.example.west2summer.user


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.west2summer.databinding.LoginFragmentBinding


class LoginFragment : Fragment() {

    private lateinit var binding: LoginFragmentBinding

    private val viewModel: LoginViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProviders.of(
            this,
            LoginViewModel.Factory(activity.application)
        )
            .get(LoginViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LoginFragmentBinding.inflate(inflater)
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
                viewModel.onMessageShowed()
            }
        })
        viewModel.loginSuccess.observe(this, Observer { loginSuccess ->
            if (loginSuccess) {
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

    override fun onStop() {
        hideKeyboard()
        super.onStop()
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }

}
