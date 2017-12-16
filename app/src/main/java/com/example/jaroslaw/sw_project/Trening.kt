package com.example.jaroslaw.sw_project

import android.content.ContentValues.TAG
import android.util.Log
import java.util.*

/**
 * Created by jaroslaw on 16/12/17.
 */

class Trening constructor(treningNumber : Long) {

    private val ID : Long = treningNumber

    private var measurementHistory : LinkedList<Measurement> = LinkedList<Measurement>()

    fun addMeasurement(measurement: Measurement){
        measurementHistory.add(measurement)
    }

    fun getTreningHistory(): LinkedList<Measurement>{
        return measurementHistory
    }

    fun getTreningID(): Long{
        return ID
    }

    fun getTreningDistance():Float{
        var distance =0f
        if (measurementHistory.size > 1) {
            for (i in 1 until measurementHistory.size) {
                distance += measurementHistory[i].getLocation().distanceTo(measurementHistory[i-1].getLocation())
                Log.d(TAG,"Actual distance = "+ distance.toString())
            }
        }
        return distance
    }

}