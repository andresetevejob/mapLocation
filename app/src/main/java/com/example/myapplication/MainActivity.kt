package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.beust.klaxon.*
import com.google.android.gms.common.util.CollectionUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // declare bounds object to fit whole route in screen
        val LatLongB = LatLngBounds.Builder()
        // Add markers
        val chatelet = LatLng(48.8587782, 2.3474106)
        val creteil = LatLng(48.7771486, 2.4530731)
        mMap.addMarker(MarkerOptions().position(chatelet).title("Chatelet"))
        mMap.addMarker(MarkerOptions().position(creteil).title("Creteil"))

        val chatGare = PolylineOptions()
        chatGare.color(Color.RED)
        chatGare.width(5f)
        chatGare.add(LatLng(48.8587782,2.3474106))
        chatGare.add(LatLng(48.8448057,2.3734794))
        LatLongB.include(LatLng(48.8587782,2.3474106))
        LatLongB.include(LatLng(48.8448057,2.3734794))
        val gareReneArcos = PolylineOptions()
        gareReneArcos.color(Color.BLUE)
        gareReneArcos.width(5f)
        gareReneArcos.add(LatLng(48.8448057,2.3734794))
        gareReneArcos.add(LatLng(48.777885, 2.467415))
        LatLongB.include(LatLng(48.777885, 2.467415))

        val reneArcosCreteil = PolylineOptions()
        reneArcosCreteil.color(Color.YELLOW)
        reneArcosCreteil.width(5f)
        reneArcosCreteil.add(LatLng(48.777885, 2.467415))
        reneArcosCreteil.add(creteil)
        LatLongB.include(creteil)

        val bounds = LatLongB.build()
        // add polyline to the map
        mMap.addPolyline(chatGare)
        mMap.addPolyline(gareReneArcos)
        mMap.addPolyline(reneArcosCreteil)
        // show map with route centered
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val padding = (width * 0.12).toInt()
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,width,height, padding))
    }


}