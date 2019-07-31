package com.example.west2summer.map


import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.MyLocationStyle
import com.example.west2summer.R
import com.example.west2summer.database.BikeInfo
import com.example.west2summer.databinding.MapFragmentBinding
import org.jetbrains.annotations.TestOnly
import kotlin.random.Random


class MapFragment : Fragment() {

    val TAG = "MapFragment"

    private lateinit var binding: MapFragmentBinding

    private lateinit var map: AMap

    private val viewModel: MapViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProviders.of(this, MapViewModel.Factory(activity.application))
            .get(MapViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
//        binding = MapFragmentBinding.inflate(inflater)
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.map_fragment,
            null,
            false
        )
        // init mapview
        map = binding.mapView.map.apply {
            cameraPosition?.let {
                moveCamera(CameraUpdateFactory.newCameraPosition(it))
            }
            moveCamera(CameraUpdateFactory.zoomTo(17f))
            myLocationStyle = MyLocationStyle().apply {
                //                interval(2000)
//                myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
                myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE)
            }
            uiSettings?.apply {
                isMyLocationButtonEnabled = true
                isZoomControlsEnabled = false
                isRotateGesturesEnabled = false
//                setMapStatusLimits(
//                    LatLngBounds(
//                        southwestLatLng,
//                        northeastLatLng
//                    )
//                )
            }
            isMyLocationEnabled = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(
            "MapFragment", "onCreateView: " +
                    ""
        )
        binding.mapView.onCreate(savedInstanceState)

        //setup viewmodel
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        viewModel.navigateToAdd.observe(this, Observer { shouldNavigateToAdd ->
            if (shouldNavigateToAdd) {
                navigateToAdd()
                viewModel.doneNavigating()
            }
        })


        addMarkersRandomly()
        return binding.root
    }

    private fun navigateToAdd() {
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



    companion object {
        var cameraPosition: CameraPosition? = null
        val southwestLatLng = LatLng(26.041229, 119.175761)
        val northeastLatLng = LatLng(26.07757, 119.210339)
    }

    @TestOnly
    private fun addMarkersRandomly() {
        for (i in 1..20) {
            val lat = Random.nextDouble(26.050055, 26.056851)
            val lng = Random.nextDouble(
                119.189396,
                119.19266
            )
            val latLng = LatLng(lat, lng)
            map.addMarker(
                MarkerOptions().position(latLng)
            )
        }
        // 定义 Marker 点击事件监听
        val markerClickListener = AMap.OnMarkerClickListener {
            // marker 对象被点击时回调的接口
            // 返回 true 则表示接口已响应事件，否则返回false
            true
        }
        // 绑定 Marker 被点击事件
        map.setOnMarkerClickListener(markerClickListener)
    }

    override fun onResume() {
        super.onResume()
//        hideKeyboard()
        Log.d(
            "MapFragment", "onResume: " +
                    ""
        )
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        Log.d(
            "MapFragment", "onPause: " +
                    ""
        )
        cameraPosition = map.cameraPosition
        binding.mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(
            "MapFragment", "onSaveInstanceState: " +
                    ""
        )
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
        Log.d(
            "MapFragment", "onDestroy: " +
                    ""
        )
    }




}
