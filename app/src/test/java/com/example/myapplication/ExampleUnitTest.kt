package com.example.myapplication

import com.google.android.gms.common.util.CollectionUtils.listOf
import org.junit.Assert.*
import org.junit.Test
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        //assertEquals(4, 2 + 2)
        val wapoints = arrayListOf<String>()
        wapoints.addAll((listOf("Abc","test")))
        handleWayPoints(waypointsPlaces = wapoints)

    }
    private fun handleWayPoints( waypointsPlaces: ArrayList<String>): String {
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