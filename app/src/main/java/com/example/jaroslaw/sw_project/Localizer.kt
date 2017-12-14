package com.example.jaroslaw.sw_project

import android.app.Service
import android.content.ContentValues.TAG
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.widget.Toast
import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings


class Localizer(private val context: Context) : Service(), LocationListener {

    private var checkGPS: Boolean = false
    private var checkNetwork: Boolean = false
    private var canGetLocation: Boolean = false

    private var loc: Location? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var height: Double = 0.0

    private val MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 10f
    private val MIN_TIME_BW_UPDATES: Long = 1000 * 5 * 1

    protected var locationManager: LocationManager? = null

    init {
        getLocation()
    }

    fun getLocation() {
        try {
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            checkGPS = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)

            Log.d(TAG, " getLocation GPS: " + checkGPS)

            checkNetwork = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            Log.d(TAG, "getLocation network: " + checkNetwork)

            if (!checkGPS and !checkNetwork) {
                Toast.makeText(context, "No Service Provider is available", Toast.LENGTH_SHORT).show()
            } else {
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
                    if (locationManager != null) {
                        val locationProvider = LocationManager.NETWORK_PROVIDER
                        loc = locationManager!!.getLastKnownLocation(locationProvider)
                        Log.d(TAG, "getLocation: loc " + loc!!)
                        Log.d(TAG, "getLocation: lastKnow " + locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER))
                        if (loc != null) {
                            latitude = loc!!.latitude
                            longitude = loc!!.longitude
                            height = loc!!.altitude
                        }
                    }

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getLatitude(): Double {
        if (loc != null) {
            latitude = loc!!.latitude
        }
        return latitude
    }

    fun getLongitude(): Double {
        if (loc != null) {
            longitude = loc!!.longitude
        }
        return longitude
    }

    fun getAltitude(): Double {
        if (loc != null) {
            height = loc!!.altitude
        }
        return height
    }

    fun canGetLocation(): Boolean {
        return this.canGetLocation
    }

    fun showSettingsAlert() {
        val alertDialog = AlertDialog.Builder(context)

        alertDialog.setTitle("GPS is not Enabled!")
        alertDialog.setMessage("Do you want to turn on GPS?")
        alertDialog.setPositiveButton("Yes") { dialog, which ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            context.startActivity(intent)
        }
        alertDialog.setNegativeButton("No") { dialog, which ->
            dialog.cancel()
        }
        alertDialog.show()
    }

    fun stopListener() {
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            locationManager!!.removeUpdates(this@Localizer)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onLocationChanged(location: Location) {
        latitude = location.latitude
        longitude = location.longitude
        height = location.altitude
    }

    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {
    }

    override fun onProviderEnabled(s: String) {
    }

    override fun onProviderDisabled(s: String) {
    }

}