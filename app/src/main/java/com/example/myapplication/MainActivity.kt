package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.*
class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnPolylineClickListener {
    private lateinit var mMap: GoogleMap
    private lateinit var client: MqttAndroidClient
    private lateinit var polyLine1 : Polyline
    private lateinit var polyLine2 : Polyline
    private lateinit var polyLine3 : Polyline
    private lateinit var polyLines:Array<Polyline>
    private val COLOR_DARK_GREEN_ARGB = -0xc771c4
    private val COLOR_DARK_ORANGE_ARGB = -0xa80e9
    val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        client = MqttAndroidClient(
                this.getApplicationContext(), "tcp://broker.hivemq.com:1883",
                "notification", MemoryPersistence(),MqttAndroidClient.Ack.AUTO_ACK)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        connectionToBrokerMQTT()
        mMap = googleMap
        // declare bounds object to fit whole route in screen
        val LatLongB = LatLngBounds.Builder()
        // Add markers
        val chatelet = LatLng(48.8587782, 2.3474106)
        val creteil = LatLng(48.7771486, 2.4530731)
        val gareLyon = LatLng(48.8448057,2.3734794)
        val reneArcos = LatLng(48.777885, 2.467415)
        mMap.addMarker(MarkerOptions().position(chatelet).title("Chatelet"))
        mMap.addMarker(MarkerOptions().position(creteil).title("Creteil"))
        mMap.addMarker(MarkerOptions().position(gareLyon).title("Gare de Lyon"))
        mMap.addMarker(MarkerOptions().position(reneArcos).title("Rene Arcos"))
        val chatGare = PolylineOptions()
        chatGare.width(15f)
        chatGare.add(chatelet)
        chatGare.add(gareLyon)
        LatLongB.include(LatLng(48.8587782,2.3474106))
        LatLongB.include(LatLng(48.8448057,2.3734794))
        val gareReneArcos = PolylineOptions()
        gareReneArcos.width(15f)
        gareReneArcos.add(gareLyon)
        gareReneArcos.add(reneArcos)
        LatLongB.include(LatLng(48.777885, 2.467415))
        val reneArcosCreteil = PolylineOptions()
        reneArcosCreteil.width(15f)
        reneArcosCreteil.add(LatLng(48.777885, 2.467415))
        reneArcosCreteil.add(creteil)
        LatLongB.include(creteil)
        val bounds = LatLongB.build()
        // add polyline to the map
        polyLine1 = mMap.addPolyline(chatGare.clickable(true))
        polyLine2 = mMap.addPolyline(gareReneArcos.clickable(true))
        polyLine3 = mMap.addPolyline(reneArcosCreteil.clickable(true))
        polyLine1.color = COLOR_DARK_GREEN_ARGB
        polyLine2.color = COLOR_DARK_GREEN_ARGB
        polyLine3.color = COLOR_DARK_GREEN_ARGB
        polyLines =  arrayOf(polyLine1,polyLine2,polyLine3)
        // show map with route centered
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val padding = (width * 0.12).toInt()
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,width,height, padding))
        mMap.setOnPolylineClickListener(this)
    }
    fun connectionToBrokerMQTT() {
        try {
            val opts = MqttConnectOptions()
            val token = client.connect(opts)
            token.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.d(TAG,"test de la connection :")
                    // We are connected
                    Log.d(TAG, "onSuccess")
                    try {
                        subTraffic()
                    } catch (e: MqttException) {
                        e.printStackTrace()
                    }
                }
                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure")
                }
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }
    fun subTraffic() {
        try {
            client.subscribe("traffic", 0)
            Log.d(TAG,"subscribed to the topic traffic")
            Log.d(TAG,"client connected : " + client)
            client.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable) {
                    Log.d(TAG,"connection lost")
                }
                override fun messageArrived(topic: String, message: MqttMessage) {
                    try {
                        val data = String(message.payload, charset("UTF-8"))
                        Log.d(TAG,  "message receive :"+data)
                        val rnds = (0..2).random()
                        if(data.equals("TRAFFIC_PERTUBE")){
                            polyLines[rnds].color = COLOR_DARK_ORANGE_ARGB
                        }else{
                            polyLines[rnds].color = COLOR_DARK_GREEN_ARGB
                        }

                    } catch (e: Exception) {
                        // Give your callback on error here
                    }
                }


                override fun deliveryComplete(token: IMqttDeliveryToken) {}
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }

    }

    override fun onPolylineClick(polyLine: Polyline) {
        var message = "Le trafic est normal"
        if(polyLine.color==COLOR_DARK_ORANGE_ARGB){
            message = "Le traffic est pertubé"
        }
        Toast.makeText(this, message,
                Toast.LENGTH_SHORT).show()

    }
}