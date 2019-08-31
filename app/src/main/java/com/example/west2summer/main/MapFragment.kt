package com.example.west2summer.main


import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.TextureMapView
import com.amap.api.maps.model.*
import com.example.west2summer.R
import com.example.west2summer.component.convertLatLngToPlaceAsync
import com.example.west2summer.component.toast
import com.example.west2summer.databinding.MapFragmentBinding
import com.example.west2summer.source.BikeInfo
import com.example.west2summer.source.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MapFragment : Fragment() {

    private lateinit var binding: MapFragmentBinding

    private lateinit var amap: AMap

    private lateinit var mapView: TextureMapView

    private var centerMarker: Marker? = null

    private val viewModel: MapViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProviders.of(
            this,
            MapViewModel.Factory(activity.application)
        )
            .get(MapViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        binding = MapFragmentBinding.inflate(layoutInflater)
        mapView = binding.mapView
        amap = mapView.map.apply {
            isMyLocationEnabled = true
            myLocationStyle = MyLocationStyle().apply {
                myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE)
                interval(5000L)
            }
            setOnMyLocationChangeListener {
                if (myLocationStyle.myLocationType == MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE) {
                    myLocationStyle = MyLocationStyle().apply {
                        myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
                        interval(5000L)
                    }
                    moveCamera(CameraUpdateFactory.zoomTo(17f))
                }
            }
            uiSettings?.apply {
                isMyLocationButtonEnabled = true
                isZoomControlsEnabled = false
                isRotateGesturesEnabled = false
            }
        }
        mapView.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.refreshList()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.swipeRefresh.isEnabled = false
        subscribeUi()
        return binding.root
    }

    private fun subscribeUi() {

        User.currentUser.observe(this, Observer {
            refreshMarkers()
        })

        viewModel.markerMapping.observe(this, Observer {
            refreshMarkers()
        })

        viewModel.centerMarkerIsVisible.observe(this, Observer { shouldAddCenterMarker ->
            centerMarker?.remove()
            if (shouldAddCenterMarker) {
                centerMarker = amap.addMarker(MarkerOptions()).apply {
                    position = amap.cameraPosition.target
                    setIcon(iconAlphaMarker)
                    setupCenterMarkerInfoWindow()
                }
            }
        })

        viewModel.message.observe(this, Observer { msg ->
            msg?.let {
                toast(context!!, msg)
                viewModel.onMessageShowed()
            }
        })

        viewModel.isRefreshing.observe(this, Observer { refreshing ->
            refreshing?.let {
                binding.swipeRefresh.isRefreshing = it
            }
        })

        amap.setInfoWindowAdapter(object : AMap.InfoWindowAdapter {
            override fun getInfoContents(marker: Marker?) = null

            override fun getInfoWindow(marker: Marker?): View {
                val view =
                    LayoutInflater.from(context).inflate(R.layout.map_marker_window, mapView, false)
                view.findViewById<TextView>(R.id.tvPlace).text = marker?.title
                return view
            }
        })

        amap.setOnCameraChangeListener(object : AMap.OnCameraChangeListener {
            override fun onCameraChange(p0: CameraPosition?) {
                centerMarker?.hideInfoWindow()
            }

            override fun onCameraChangeFinish(cameraPosition: CameraPosition?) {
                if (centerMarker != null) {
                    centerMarker!!.position = cameraPosition?.target
                    setupCenterMarkerInfoWindow()
                }
            }
        })

        amap.setOnInfoWindowClickListener {
            if (User.isLoginned()) {
                navigateToAddFragment()
                viewModel.onFabClicked()
            } else {
                Toast.makeText(context, getString(R.string.please_login), Toast.LENGTH_SHORT).show()
                findNavController().navigate(MapFragmentDirections.actionGlobalLoginFragment())
            }
        }

        amap.setOnMarkerClickListener {
            if (it != centerMarker || viewModel.centerMarkerIsVisible.value == false) {
                amap.animateCamera(CameraUpdateFactory.newLatLng(it.position))
                findNavController().navigate(
                    MapFragmentDirections.actionMapFragmentToBikeInfoDialog(
                        viewModel.markerMapping.value!![it.options]!!
                    )
                )
            }
            true
        }
    }

    private fun setupCenterMarkerInfoWindow() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                withContext(Dispatchers.IO) {
                    centerMarker?.title = convertLatLngToPlaceAsync(
                        context!!,
                        centerMarker!!.position.latitude,
                        centerMarker!!.position.longitude
                    ).pois[0].toString()
                }
                centerMarker?.showInfoWindow()
            } catch (e: Exception) {
                toast(context!!, getString(R.string.exam_network))
            }
        }
    }

    private fun refreshMarkers() {
        amap.clear(true)
        viewModel.markerMapping.value?.entries?.forEach {
            if (User.currentUser.value?.id == it.value.ownerId) {
                it.key.icon(iconYellowMarker)
            } else {
                it.key.icon(iconBlueMarker)
            }
            amap.addMarker(it.key)
        }
    }

    private fun navigateToAddFragment() {
        centerMarker?.let {
            val bikeInfo = BikeInfo(
                it.position.latitude,
                it.position.longitude,
                User.currentUser.value!!.id
            )
            findNavController().navigate(
                MapFragmentDirections.actionMapFragmentToEditBikeInfoFragment(
                    bikeInfo
                )
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.refresh -> {
                viewModel.refreshList()
            }
            else -> {
                return false
            }
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    private val iconYellowMarker by lazy {
        BitmapDescriptorFactory.fromBitmap(
            BitmapFactory
                .decodeResource(
                    resources,
                    R.drawable.marker_yellow,
                    BitmapFactory.Options().apply {
                        inDensity = 450
                    })
        )
    }
    private val iconRedMarker by lazy {
        BitmapDescriptorFactory.fromBitmap(
            BitmapFactory
                .decodeResource(
                    resources,
                    R.drawable.marker_red,
                    BitmapFactory.Options().apply {
                        inDensity = 450
                    })
        )
    }

    private val iconBlueMarker by lazy {
        BitmapDescriptorFactory.fromBitmap(
            BitmapFactory
                .decodeResource(
                    resources,
                    R.drawable.marker_blue,
                    BitmapFactory.Options().apply {
                        inDensity = 450
                    })
        )
    }

    val iconAlphaMarker by lazy {
        BitmapDescriptorFactory.fromBitmap(
            BitmapFactory
                .decodeResource(
                    resources,
                    R.drawable.alpha,
                    BitmapFactory.Options().apply {
                        inDensity = 450
                    })
        )
    }
}
