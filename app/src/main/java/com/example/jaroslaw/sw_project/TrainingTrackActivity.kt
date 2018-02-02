package com.example.jaroslaw.sw_project

import android.database.sqlite.SQLiteDatabase
import android.nfc.Tag
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.android.gms.maps.OnMapReadyCallback
import kotlinx.android.synthetic.main.activity_training_track.*
import java.util.*
import com.google.android.gms.maps.GoogleMap


class TrainingTrackActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training_track)
        var list : LinkedList<String> = LinkedList()
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

        training_chooser_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                try {
                    val selectedItem = parent!!.getItemAtPosition(position).toString()
                    getTrainingFromDatabase(selectedItem.toLong())
                }catch (e : NumberFormatException){
                    e.printStackTrace()
                }
            }
        }
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)
    }

    private fun getTrainingFromDatabase(trainingID : Long){
        synchronized(this){
            val databaseManager = DatabaseManager(this)
            val readDatabase : SQLiteDatabase = databaseManager!!.readableDatabase
            val training = databaseManager!!.selectTraining(readDatabase, trainingID)
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
    }
}
