package com.example.west2summer.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.example.west2summer.R
import com.example.west2summer.databinding.BikeInfoDialogFragmentBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BikeInfoDialogFragment: BottomSheetDialogFragment() {

    lateinit var binding:BikeInfoDialogFragmentBinding

    private val viewModel : BikeInfoDialogViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProviders.of(
            this,
            BikeInfoDialogViewModel.Factory(
                activity.application,
                BikeInfoDialogFragmentArgs.fromBundle(arguments!!).bikeinfo
            )
        )
            .get(BikeInfoDialogViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BikeInfoDialogFragmentBinding.inflate(inflater)

        binding.viewModel = viewModel

        return binding.root
    }
}