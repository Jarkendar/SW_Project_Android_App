package com.example.jaroslaw.sw_project

import android.app.Service
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.widget.Toast
import android.Manifest
import android.content.pm.PackageManager

/**
 * Created by jaroslaw on 12/12/17.
 */

class Localizer(private val context: Context) : Service(), LocationListener {

    private var checkGPS: Boolean = false
    private var checkNetwork: Boolean = false
    private var canGetLocation: Boolean = false

    private var loc: Location
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var height: Double = 0.0

    private final val MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 10f
    private final val MIN_TIME_BW_UPDATES: Long = 1000 * 60 * 1

    protected var locationManager: LocationManager? = null

    private fun getLocation(): Location {
        try {
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            checkGPS = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)

            Log.d(TAG, " getLocation GPS: " + checkGPS)

            checkNetwork = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            Log.d(TAG, "getLocation network: " + checkNetwork)

            if (!checkGPS and !checkNetwork){
                Toast.makeText(context,"No Service Provider is available",Toast.LENGTH_SHORT).show()
            } else{
                this.canGetLocation = true
                if (checkGPS) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    }
                    locationManager!!.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            this)
                    Log.d(TAG, "getLocation location manager: " + (locationManager == null))
                    Log.d(TAG, "getLocation location manager: " + locationManager!!)
                    if (locationManager != null){
                        val locationProvider = LocationManager.NETWORK_PROVIDER
                        loc = locationManager!!.getLastKnownLocation(locationProvider)
                        Log.d(TAG, "getLocation: loc " + loc!!)
                        Log.d(TAG, "getLocation: lastKnow " + locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER))
                        if (loc != null){
                            latitude = loc!!.latitude
                            longitude = loc!!.longitude
                            height = loc!!.latitude
                        }
                    }

                }
            }
        }catch (e : Exception){
            e.printStackTrace()
        }
    }

}