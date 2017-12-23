package com.example.jaroslaw.sw_project

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ContentValues.TAG
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), SensorEventListener {

    private var permissionToRequest: ArrayList<String>? = null
    private var permissionRejected: ArrayList<String> = ArrayList()
    private var permissions: ArrayList<String> = ArrayList()

    private var training: Training? = Training(1)

    private var automaticRefresher: AutomaticRefresher? = null

    private val REFRESH_TIME: Long = 1000 * 1
    private val ALL_PERMISSIONS_RESULT: Int = 101
    private var localizer: Localizer? = null
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

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

        start_button.setOnClickListener {
            automaticRefresher = AutomaticRefresher()
            automaticRefresher!!.execute(REFRESH_TIME)
        }

        stop_button.setOnClickListener {
            automaticRefresher!!.cancel(true)
            start_button.isEnabled = true
            stop_button.isEnabled = false
            Log.d(TAG, "Training " + training!!.getTrainingID().toString())
            for (x in training!!.getTrainingHistory()) {
                Log.d(TAG, "location " + x.getLocation() + " \ntime" + x.getTime())
            }
            Log.d(TAG, "distance " + training!!.getTrainingDistance().toString())
            Toast.makeText(this,training!!.getTrainingDistance().toString(), Toast.LENGTH_SHORT ).show()
        }
        initSensors()
    }

    @SuppressLint("MissingPermission")
    private fun initSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        for (x in sensorManager!!.getSensorList(Sensor.TYPE_ALL)) {
            Log.d(TAG, "sensor Type: " + x.toString())
        }
        accelerometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onAccuracyChanged(sensor: Sensor?, p1: Int) {}

    override fun onSensorChanged(sensorEvent: SensorEvent?) {
        if (sensorEvent!!.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            var vector = sensorEvent.values
            var x = vector[0].toDouble()
            var y = vector[1].toDouble()
            var z = vector[2].toDouble()
            var l = Math.sqrt((Math.pow(x, 2.0) + Math.pow(y, 2.0)) + Math.pow(z, 2.0))
            Log.d(TAG, "earth velocity = " + l.toString() + "; time = " + System.currentTimeMillis())
        }
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onResume() {
        super.onResume()
        localizer = Localizer(applicationContext)
        sensorManager!!.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager!!.unregisterListener(this)
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

    override fun onPause() {
        super.onPause()
        localizer!!.stopListener()
    }


    inner class AutomaticRefresher : AsyncTask<Long, Int, Void>() {

        private var running: Boolean = true

        override fun onPreExecute() {
            super.onPreExecute()
            start_button.isEnabled = false
            stop_button.isEnabled = true
            Log.d(TAG, this.toString() + " is refreshing ")
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            if (localizer!!.canGetLocation()) {
                localizer!!.getLocation()
                Log.d(TAG, "location1 " + localizer!!.getLocation().toString())
                val measurement = Measurement(localizer!!.getLocation() as Location, System.currentTimeMillis())
                training!!.addMeasurement(measurement)
                val longitude = localizer!!.getLongitude()
                val latitude = localizer!!.getLatitude()
                val height = localizer!!.getAltitude()
                val message: String = "Longitude:" + java.lang.Double.toString(longitude) + "\nLatitude:" + java.lang.Double.toString(latitude) + "\nAltitude:" + java.lang.Double.toString(height)
                Log.d(TAG, message)
                Log.d(TAG, "refresh " + localizer)
                location_textView.text = message
            } else {
                localizer!!.showSettingsAlert()
            }
            Log.d(TAG, "onProgress")
        }

        override fun onCancelled() {
            super.onCancelled()
            running = false
            Log.d(TAG, this.toString() + "is canceled")
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            Log.d(TAG, this.toString() + "is not refreshing")
        }

        override fun doInBackground(vararg t: Long?): Void? {
            var i = 0
            val time: Long = t[0] as Long
            while (running) {
                Log.d(TAG, "running  " + running)
                try {
                    Thread.sleep(time)
                    publishProgress(i++)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    running = false
                }
            }
            return null
        }
    }

}
