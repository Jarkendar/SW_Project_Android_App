package com.example.jaroslaw.sw_project

import android.location.Location

class Measurement constructor(private var location: Location) {

    fun getLocation(): Location {
        return location
    }

    override fun toString(): String{
        return "Location: longitude="+location.longitude + "; latitude="+location.latitude+"; timeLocation="+location.time
    }

}