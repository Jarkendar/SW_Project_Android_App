package com.example.jaroslaw.sw_project

import android.Manifest
import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast


class Localizer(private val context: Context) : Service(), LocationListener {

    private var isGPSEnabled: Boolean = false
    private var canGetLocation: Boolean = false
    private var isPassiveEnabled: Boolean = false
    private var isNetworkEnabled: Boolean = false

    private var location: Location? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var height: Double = 0.0

    private val MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 1.0f
    private val MIN_TIME_BW_UPDATES: Long = 1000 * 5 * 1

    private var locationManager: LocationManager? = null

    init {
        getLocation()
    }

    fun getLocation(): Location? {
        var loc: Location? = null
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "PLZ open up GPS To Access", Toast.LENGTH_SHORT).show()
            return null
        }
        try {
            locationManager = context.applicationContext
                    .getSystemService(Context.LOCATION_SERVICE) as LocationManager

            isGPSEnabled = locationManager!!
                    .isProviderEnabled(LocationManager.GPS_PROVIDER)

            isPassiveEnabled = locationManager!!
                    .isProviderEnabled(LocationManager.PASSIVE_PROVIDER)

            isNetworkEnabled = locationManager!!
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (isGPSEnabled || isNetworkEnabled || isPassiveEnabled) {

                this.canGetLocation = true
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled && loc == null) {
                    locationManager!!.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this)
                    Log.d("GPS", "GPS Enabled")
                    if (locationManager != null) {
                        loc = locationManager!!
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    }
                }
                if (isPassiveEnabled && loc == null) {
                    locationManager!!.requestLocationUpdates(
                            LocationManager.PASSIVE_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this)
                    Log.d("Network", "Network Enabled")
                    if (locationManager != null) {
                        loc = locationManager!!
                                .getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                    }
                }

                if (isNetworkEnabled && loc == null) {
                    locationManager!!.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this)
                    Log.d("Network", "Network Enabled")
                    if (locationManager != null) {
                        loc = locationManager!!
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    }
                }

            } else {
                return null
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (loc != null) {
            location = loc
        }
        return loc
    }

    fun getLatitude(): Double {
        if (location != null) {
            latitude = location!!.latitude
        }
        return latitude
    }

    fun getLongitude(): Double {
        if (location != null) {
            longitude = location!!.longitude
        }
        return longitude
    }

    fun getAltitude(): Double {
        if (location != null) {
            height = location!!.altitude
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
        this.location = location
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