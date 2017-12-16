package com.example.jaroslaw.sw_project

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.TargetApi
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.pm.PackageManager
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

class MainActivity : AppCompatActivity() {

    private var permissionToRequest: ArrayList<String>? = null
    private var permissionRejected: ArrayList<String> = ArrayList()
    private var permissions: ArrayList<String> = ArrayList()

    private var trening : Trening? = Trening(1)

    private var automaticRefresher: AutomaticAsker? = null

    private val REFRESH_TIME: Long = 1000 * 1
    private val ALL_PERMISSIONS_RESULT: Int = 101
    var localizer: Localizer? = null

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
            automaticRefresher = AutomaticAsker()
            automaticRefresher!!.execute(REFRESH_TIME)
        }

        stop_button.setOnClickListener {
            automaticRefresher!!.cancel(true)
            start_button.isEnabled = true
            stop_button.isEnabled = false
            Log.d(TAG, "Trening "+ trening!!.getTreningID().toString())
            for(x in trening!!.getTreningHistory()){
                Log.d(TAG, "location "+ x.getLocation()+" \ntime"+x.getTime())
            }
            Log.d(TAG, "distance " +trening!!.getTreningDistance().toString())
        }
    }

    override fun onResume() {
        super.onResume()
        localizer = Localizer(this@MainActivity)
    }

    private fun findUnAskedPermissions(wanted: ArrayList<String>): ArrayList<String> {
        val result: ArrayList<String> = ArrayList()

        for (perm in wanted) {
            if (!hasPermission(perm)) {
                result.add(perm)
            }
        }
        return result
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
                for (perm in permissionToRequest!!) {
                    if (!hasPermission(perm)) {
                        permissionRejected.add(perm)
                    }
                }
                if (permissionRejected.size > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    DialogInterface.OnClickListener { dialog, which ->
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


    inner class AutomaticAsker() : AsyncTask<Long, Int, Void>() {

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

                val measurement : Measurement = Measurement(localizer!!.getLocaton(), System.currentTimeMillis())
                trening!!.addMeasurement(measurement)

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

        override fun doInBackground(vararg t: Long?): Void {
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
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

}
