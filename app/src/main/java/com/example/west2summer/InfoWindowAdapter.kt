package com.example.west2summer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.amap.api.maps.AMap
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker

class InfoWindowAdapter(val context: Context) : AMap.InfoWindowAdapter {

    override fun getInfoContents(marker: Marker?): View? {
        return null
    }

    override fun getInfoWindow(marker: Marker?): View {
        val place = marker?.title
        val view = LayoutInflater.from(context).inflate(R.layout.map_info_window, null)
        view.findViewById<TextView>(R.id.tvPlace).text = place
        return view
    }

}