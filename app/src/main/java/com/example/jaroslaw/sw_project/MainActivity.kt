package com.example.jaroslaw.sw_project

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderApi
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private var permissionToRequest: ArrayList<String>? = null
    private var permissionRejected: ArrayList<String> = ArrayList()
    private var permissions: ArrayList<String> = ArrayList()

    private var fusLocationProviderAPI: FusedLocationProviderApi = LocationServices.FusedLocationApi
    private var googleApiClient: GoogleApiClient? = null
    private var locationRequest: LocationRequest = LocationRequest()
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private var training: Training = Training(0)
    private val TRAINING_ID = 0L
    private var isTraining = false
    private var databaseManager: DatabaseManager? = null

    private val TAG = "MyActivity"
    private val ALL_PERMISSIONS_RESULT: Int = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissions.add(ACCESS_FINE_LOCATION)
        permissions.add(ACCESS_COARSE_LOCATION)

        permissionToRequest = findUnAskedPermissions(permissions)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionToRequest!!.size > 0) {
                requestPermissions(permissionToRequest!!.toTypedArray<String>(), ALL_PERMISSIONS_RESULT)
            }
        }

        googleApiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()

        Log.d(ContentValues.TAG, "googleApiClient" + googleApiClient.toString())

        locationRequest.setInterval(10 * 1000)
        locationRequest.setFastestInterval(5 * 1000)
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    }

    fun startTraining(view: View) {
        isTraining = true
        training = Training(TRAINING_ID)

        start_button.isEnabled = false
        stop_button.isEnabled = true
    }

    fun stopTraining(view: View) {
        isTraining = false

        //todo save to databaseManager training, write function
        for (measure in training.getTrainingHistory()) {
            Log.d(TAG, "measure : " + measure)
        }

        val str: String = getString(R.string.training_save_to_datebase)
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show()
        start_button.isEnabled = true
        stop_button.isEnabled = false

        insertTrainingToDatabase()
    }

    fun readTrainingFromBase(view: View) {
        synchronized(this) {
            databaseManager = DatabaseManager(this)
            var readerDatabase: SQLiteDatabase = databaseManager!!.readableDatabase
            var training: Training = databaseManager!!.selectTraining(readerDatabase, 0)
            for (measure in training.getTrainingHistory()) {
                Log.d(TAG, "loaded measure : " + measure)
            }
            readerDatabase.close()
            databaseManager!!.close()
        }
    }

    fun synchronizeWithServer(view: View) {

    }

    private fun insertTrainingToDatabase() {
        synchronized(this) {
            databaseManager = DatabaseManager(this)
            var writeDatabase: SQLiteDatabase = databaseManager!!.writableDatabase
            databaseManager!!.insertTraining(writeDatabase, training)
            writeDatabase.close()
            databaseManager!!.close()
        }
    }


    override fun onConnected(p0: Bundle?) {
        requestLocationUpdates()
    }

    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission
                .ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission
                        .ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusLocationProviderAPI.requestLocationUpdates(googleApiClient, locationRequest, this)
    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    override fun onLocationChanged(location: Location?) {
        latitude = location!!.latitude
        longitude = location!!.longitude
        val message: String = "Latitude: " + latitude.toString() + "\nLongitude: " + longitude.toString() + "\nAltitude: " + location!!.altitude.toString() + "\nTime " + location!!.time
        location_textView.text = message
        if (isTraining) {
            training.addMeasurement(Measurement(location))
            Log.d(TAG, "save location: " + location)
        }
        Log.d(TAG, "time " + System.currentTimeMillis().toString())
    }

    override fun onStart() {
        super.onStart()
        if (googleApiClient != null) {
            googleApiClient!!.connect()
        }
    }

    override fun onResume() {
        super.onResume()
        if (googleApiClient!!.isConnected) {
            requestLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        if (googleApiClient!!.isConnected) {
            fusLocationProviderAPI.removeLocationUpdates(googleApiClient, this)
        }
    }

    override fun onStop() {
        super.onStop()
        googleApiClient!!.disconnect()
    }

    private fun findUnAskedPermissions(wanted: ArrayList<String>): ArrayList<String> {
        return wanted.filterNotTo(ArrayList()) { hasPermission(it) }
    }

    private fun hasPermission(permission: String): Boolean {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
            }
        }
        return true
    }

    private fun canMakeSmores(): Boolean {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            ALL_PERMISSIONS_RESULT -> {
                permissionToRequest!!
                        .filterNot { hasPermission(it) }
                        .forEach { permissionRejected.add(it) }
                if (permissionRejected.size > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionRejected[0])) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    DialogInterface.OnClickListener { _, _ ->
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            requestPermissions(permissionRejected.toTypedArray(), ALL_PERMISSIONS_RESULT)
                                        }
                                    })
                            return
                        }
                    }
                }
            }
        }
    }

    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this@MainActivity)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show()
    }

}
