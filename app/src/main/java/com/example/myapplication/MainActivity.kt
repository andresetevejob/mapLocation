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
        polyLine1 = mMap.addPolyline(chatGare.clickable(true))
        polyLine2 = mMap.addPolyline(gareReneArcos.clickable(true))
        polyLine3 = mMap.addPolyline(reneArcosCreteil.clickable(true))

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
                        polyLine1.color = -0x1000000
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

    override fun onPolylineClick(p0: Polyline) {
        Log.d(TAG,"Le traffic est pertubé")
        Toast.makeText(this, "Le traffic est pertubé",
                Toast.LENGTH_SHORT).show()

    }
}