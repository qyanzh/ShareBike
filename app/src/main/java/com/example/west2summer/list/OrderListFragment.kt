package com.example.west2summer.list


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.west2summer.component.toast
import com.example.west2summer.databinding.OrderListFragmentBinding
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 */
class OrderListFragment : Fragment() {

    lateinit var binding: OrderListFragmentBinding

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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = OrderListFragmentBinding.inflate(inflater)
        val adapter = OrderAdapter(context!!, OrderRecordListener {
            viewModel.uiScope.launch {
                viewModel.getBikeInfo(it.bikeId)?.let {
                    try {
                        findNavController().navigate(
                            OrderListFragmentDirections.actionGlobalBikeInfoDialog(it)
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
        subscribeUi()
        binding.recyclerView.adapter = adapter
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        viewModel.records.observe(this, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })
        return binding.root
    }

    private fun subscribeUi() {
        binding.swipeRefresh.let {
            it.setOnRefreshListener {
                viewModel.refreshList()
            }
        }
        viewModel.isRefreshing.observe(this, Observer {
            it?.let {
                binding.swipeRefresh.isRefreshing = it
            }
        })
        viewModel.message.observe(this, Observer {
            it?.let {
                toast(context!!, it)
                viewModel.onMessageShowed()
            }
        })
    }

}
