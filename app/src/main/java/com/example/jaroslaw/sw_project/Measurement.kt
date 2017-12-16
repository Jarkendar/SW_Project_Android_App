package com.example.jaroslaw.sw_project

import android.location.Location

class Measurement constructor(private var location: Location, private var time: Long) {

    fun getLocation(): Location {
        return location
    }

    fun getTime(): Long {
        return time
    }

}