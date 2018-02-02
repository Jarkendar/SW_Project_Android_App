package com.example.jaroslaw.sw_project

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.activity_training_track.*
import java.util.*


class TrainingTrackActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private val TAG = "TrainingTrackActivity"
    private var latLngList: LinkedList<LatLng>? = LinkedList()
    private var bundle: Bundle? = null
    private val CENTER_POLAND: LatLng = LatLng(51.9194, 19.1451)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training_track)
        bundle = savedInstanceState
        var list: LinkedList<String> = LinkedList()
        synchronized(this) {
            val databaseManager = DatabaseManager(this)
            val readDatabase: SQLiteDatabase = databaseManager!!.readableDatabase
            list = databaseManager!!.getAllTrainingNumbers(readDatabase)
            list.addFirst(getString(R.string.first_element_list))
            readDatabase.close()
            databaseManager!!.close()
        }
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, list.toArray())
        training_chooser_spinner.adapter = arrayAdapter

        training_chooser_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                try {
                    val selectedItem = parent!!.getItemAtPosition(position).toString()
                    getTrainingFromDatabase(selectedItem.toLong())
                } catch (e: NumberFormatException) {
                    latLngList = LinkedList()
                    mapView.onDestroy()
                    mapView.onCreate(bundle)
                    mapView.onResume()
                    mapView.getMapAsync(this@TrainingTrackActivity)
                    e.printStackTrace()
                }
            }
        }
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)
    }

    private fun getTrainingFromDatabase(trainingID: Long) {
        synchronized(this) {
            val databaseManager = DatabaseManager(this)
            val readDatabase: SQLiteDatabase = databaseManager!!.readableDatabase
            val training = databaseManager!!.selectTraining(readDatabase, trainingID)
            latLngList = LinkedList()
            training.getTrainingHistory()
                    .map { it.getLocation() }
                    .forEach { latLngList!!.addLast(LatLng(it.latitude, it.longitude)) }
            Log.d(TAG, "latLngList : $latLngList")
            mapView.onDestroy()
            mapView.onCreate(bundle)
            mapView.onResume()
            mapView.getMapAsync(this)
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
        mMap!!.addPolyline(PolylineOptions().addAll(latLngList))
        if (latLngList!!.isEmpty()) {
            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(CENTER_POLAND, 5.5f))
        } else {
            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngList!!.first, 10f))
        }
    }
}
