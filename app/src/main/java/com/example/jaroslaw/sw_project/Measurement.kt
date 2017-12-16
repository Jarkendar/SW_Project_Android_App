package com.example.jaroslaw.sw_project

import android.location.Location

/**
 * Created by jaroslaw on 16/12/17.
 */
class Measurement constructor(location: Location, time: Long) {

    private var location: Location = location
    private var time: Long = time

    fun getLocation(): Location {
        return location
    }

    fun getTime(): Long {
        return time
    }

}