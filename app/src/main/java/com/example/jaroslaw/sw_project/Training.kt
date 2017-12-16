package com.example.jaroslaw.sw_project

import android.content.ContentValues.TAG
import android.util.Log
import java.util.*

class Training constructor(trainingNumber: Long) {

    private val ID: Long = trainingNumber

    private var measurementHistory: LinkedList<Measurement> = LinkedList<Measurement>()

    fun addMeasurement(measurement: Measurement) {
        measurementHistory.add(measurement)
    }

    fun getTrainingHistory(): LinkedList<Measurement> {
        return measurementHistory
    }

    fun getTrainingID(): Long {
        return ID
    }

    fun getTrainingDistance(): Float {
        var distance = 0f
        if (measurementHistory.size > 1) {
            for (i in 1 until measurementHistory.size) {
                distance += measurementHistory[i].getLocation().distanceTo(measurementHistory[i - 1].getLocation())
                Log.d(TAG, "Actual distance = " + distance.toString())
            }
        }
        return distance
    }

}