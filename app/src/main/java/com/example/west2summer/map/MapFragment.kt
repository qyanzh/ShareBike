package com.example.west2summer.map


import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.MyLocationStyle
import com.example.west2summer.R
import com.example.west2summer.database.BikeInfo
import com.example.west2summer.databinding.MapFragmentBinding


class MapFragment : Fragment() {

    private lateinit var binding: MapFragmentBinding

    private lateinit var map: AMap

    private val viewModel: MapViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProviders.of(this, MapViewModel.Factory(activity.application))
            .get(MapViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        subscribeUi()
        return binding.root
    }

    private fun subscribeUi() {
        viewModel.markerMapping.observe(this, Observer {
            refreshMarkers()
        })

        binding.fab.setOnClickListener {
            navigateToAddFragment()
        }
    }

    private fun refreshMarkers() {
        with(map) {
            clear(true)
            viewModel.markerMapping.value?.keys?.forEach {
                addMarker(it)
            }
            setOnMarkerClickListener() {
                if (viewModel.markerMapping.value?.containsKey(it.options)!!) {
                    findNavController().navigate(
                        MapFragmentDirections.actionMapFragmentToEditBikeInfoFragment(
                            viewModel.markerMapping.value!![it.options]!!
                        )
                    )
                }
                true
            }
        }
    }

    private fun navigateToAddFragment() {
        val bikeInfo = BikeInfo(123)
        map.myLocation?.let {
            bikeInfo.apply {
                latitude = it.latitude
                longitude = it.longitude
            }
        }
        findNavController().navigate(
            MapFragmentDirections.actionMapFragmentToEditBikeInfoFragment(
                bikeInfo
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermission()
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.map_fragment,
            null,
            false
        )
        map = binding.mapView.map.apply {
            uiSettings?.apply {
                isMyLocationButtonEnabled = true
                isZoomControlsEnabled = false
                isRotateGesturesEnabled = false
            }
            myLocationStyle = MyLocationStyle().apply {
                myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE)
            }
            if (haveLocatePermission()) {
                isMyLocationEnabled = true
            }
            moveCamera(CameraUpdateFactory.zoomTo(17f))
        }
        binding.mapView.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions = mutableListOf<String>()
            if (ContextCompat.checkSelfPermission(
                    context!!,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            if (ContextCompat.checkSelfPermission(
                    context!!,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(
                    context,
                    getString(R.string.location_permission_needed),
                    Toast.LENGTH_SHORT
                ).show()
            }
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(
                    context,
                    getString(R.string.strorage_permission_needed),
                    Toast.LENGTH_SHORT
                ).show()
            }
            if (permissions.size != 0) {
                requestPermissions(permissions.toTypedArray(), 0)
            }
        }
    }

    private fun haveLocatePermission(): Boolean {
        return (ContextCompat.checkSelfPermission(
            context!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}
