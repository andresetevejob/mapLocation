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
        // Declare polyline object and set up color and width
        val options = PolylineOptions()
        options.color(Color.RED)
        options.width(5f)
        // build URL to call API
        val url = getURL(chatelet, creteil)
        lifecycleScope.launch(Dispatchers.IO){
            val apiKey = getString(R.string.google_maps_key)
            val waypoints = arrayListOf<String>()
            waypoints.addAll(
                (CollectionUtils.listOf(
                    "Picpus",
                    "Canton de Vincennes",
                    "Nogent-sur-Marne"
                ))
            )
            val result = retrieveData(
                url + "&key=AIzaSyCrGLvUa3Im7LiMSf4fnX46lu2_yH8R20A" + handleWayPoints(
                    waypointsPlaces = waypoints
                )
            )

            // When API call is done, create parser and convert into JsonObjec
            val parser: Parser = Parser()
            val stringBuilder: StringBuilder = StringBuilder(result)
            val json: JsonObject = parser.parse(stringBuilder) as JsonObject
            // get to the correct element in JsonObject
            val routes = json.array<JsonObject>("routes")
            val points = routes!!["legs"]["steps"][0] as JsonArray<JsonObject>
            // For every element in the JsonArray, decode the polyline string and pass all points to a List
            val polypts = points.flatMap { decodePoly(it.obj("polyline")?.string("points")!!)  }
            // Add  points to polyline and bounds
            options.add(chatelet)
            LatLongB.include(chatelet)
            for (point in polypts)  {
                options.add(point)
                LatLongB.include(point)
            }
            options.add(creteil)
            LatLongB.include(creteil)
            // build bounds
            val bounds = LatLongB.build()
            // add polyline to the map
            withContext(Dispatchers.Main) {
                 mMap.addPolyline(options)
                // show map with route centered
                val width = resources.displayMetrics.widthPixels
                val height = resources.displayMetrics.heightPixels
                val padding = (width * 0.12).toInt()
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,width,height, padding))
            }
        }


    }
    fun retrieveData(url: String):String{
        val res = URL(url).readText();
        return res;
    }
    private fun getURL(from: LatLng, to: LatLng) : String {
        val origin = "origin=" + from.latitude + "," + from.longitude
        val dest = "destination=" + to.latitude + "," + to.longitude
        val sensor = "sensor=false"
        val params = "$origin&$dest&$sensor"
        return "https://maps.googleapis.com/maps/api/directions/json?$params"
    }

    /**
     * Method to decode polyline points
     * Courtesy : https://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     */
    private fun decodePoly(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(
                lat.toDouble() / 1E5,
                lng.toDouble() / 1E5
            )
            poly.add(p)
        }

        return poly
    }
    private fun handleWayPoints(waypointsPlaces: ArrayList<String>): String {
        var wayPoints = ""
        val wayPointsStringList = arrayListOf<String>()
        if(waypointsPlaces.isNotEmpty()){
            wayPointsStringList.addAll(waypointsPlaces)
        }
        if(wayPointsStringList.isNotEmpty()){
            wayPoints += "&waypoints="
        }
        wayPointsStringList.forEachIndexed { index, s ->
            wayPoints += if(index == wayPointsStringList.lastIndex){
                s
            }else{
                "$s|"
            }
        }

        return wayPoints
    }
}