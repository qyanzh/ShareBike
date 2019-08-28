package com.example.west2summer.list


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.west2summer.R
import com.example.west2summer.databinding.OrderListFragmentBinding
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 */
class OrderListFragment : Fragment() {

    private val viewModel: OrderListViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProviders.of(
            this,
            OrderListViewModel.Factory(
                activity.application
            )
        )
            .get(OrderListViewModel::class.java)
    }

    lateinit var binding: OrderListFragmentBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = OrderListFragmentBinding.inflate(inflater)
        val adapter = OrderAdapter(OrderRecordListener {
            viewModel.uiScope.launch {
                viewModel.getBikeInfo(it.bikeId)?.let {
                    try {
                        findNavController().navigate(
                            OrderListFragmentDirections.actionOrderListFragmentToBikeInfoDialog(-1)
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } ?: Toast.makeText(
                    context,
                    getString(R.string.network_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
        binding.recyclerView.adapter = adapter
        viewModel.records.observe(this, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

}
