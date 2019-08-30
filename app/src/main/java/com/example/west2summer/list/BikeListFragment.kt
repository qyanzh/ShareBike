package com.example.west2summer.list


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.west2summer.databinding.BikeListFragmentBinding

/**
 * A simple [Fragment] subclass.
 */
class BikeListFragment : Fragment() {

    private lateinit var binding: BikeListFragmentBinding

    private val viewModel: BikeListViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProviders.of(
            this,
            BikeListViewModel.Factory(
                activity.application
            )
        ).get(BikeListViewModel::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BikeListFragmentBinding.inflate(inflater)
        binding.viewModel = viewModel
        val adapter = BikeAdapter(context!!, BikeListener {
            findNavController().navigate(BikeListFragmentDirections.actionGlobalBikeInfoDialog(it))
        })
        binding.recyclerView.adapter = adapter
        viewModel.bikes.observe(this, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })
        binding.lifecycleOwner = this
        // Inflate the layout for this fragment
        return binding.root
    }


}
