package com.example.jaroslaw.sw_project

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.location.Location
import android.util.Log

class DatabaseManager constructor(var context: Context) : SQLiteOpenHelper(context, "TrainingDataBase", null, 1) {

    private val TABLE_NAME_TRAININGS: String = "TRAININGS"
    private val FIELD_TRAINING_ID: String = "TRAINING_ID"
    private val FIELD_ROW_ID: String = "_id"
    private val FIELD_LATITUDE: String = "LATITUDE"
    private val FIELD_LONGITUDE: String = "LONGITUDE"
    private val FIELD_TIME: String = "TIME"

    override fun onCreate(sqLiteDatabase: SQLiteDatabase?) {
        upgradeDatabase(sqLiteDatabase!!, 0, 1)
        Log.d(TAG, "create $sqLiteDatabase")
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        upgradeDatabase(sqLiteDatabase!!, oldVersion, newVersion)
        Log.d(TAG, "upgrade $sqLiteDatabase; old  $oldVersion; new $newVersion")
    }

    private fun upgradeDatabase(sqLiteDatabase: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 1) {
            //todo create table in database
            val sqlQuery: String = "CREATE TABLE " + TABLE_NAME_TRAININGS + " (" + FIELD_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    FIELD_TRAINING_ID + " LONG," +
                    FIELD_LATITUDE + " DOUBLE, " +
                    FIELD_LONGITUDE + " DOUBLE, " +
                    FIELD_TIME + " LONG" +
                    " );"
            sqLiteDatabase!!.execSQL(sqlQuery)
            Log.d(TAG, "create database table = $sqlQuery")
        }
    }

    fun insertTraining(sqLiteDatabase: SQLiteDatabase, training: Training) {
        Log.d(TAG, "insert: $sqLiteDatabase; training: $training")
        val trainingID = training.getTrainingID()

        for (measure in training.getTrainingHistory()) {
            //todo insert measurement to database
            var contentValues = ContentValues()
            contentValues.put(FIELD_TRAINING_ID, trainingID)
            contentValues.put(FIELD_LATITUDE, measure.getLocation().latitude)
            contentValues.put(FIELD_LONGITUDE, measure.getLocation().longitude)
            contentValues.put(FIELD_TIME, measure.getLocation().time)
            sqLiteDatabase.insert(TABLE_NAME_TRAININGS, null, contentValues)
            Log.d(TAG, "insert to $TABLE_NAME_TRAININGS measurement = $contentValues")
        }
    }

    fun selectTraining(sqLiteDatabase: SQLiteDatabase, trainingID: Long): Training {
        Log.d(TAG, "select: $sqLiteDatabase; training ID: $trainingID")
        //todo select training with specific ID
        //sqLiteDatabase!!.query()
        var cursor: Cursor = sqLiteDatabase.query(TABLE_NAME_TRAININGS,
                arrayOf(FIELD_ROW_ID, FIELD_LATITUDE, FIELD_LONGITUDE, FIELD_TIME),
                FIELD_TRAINING_ID + "=?",
                arrayOf(trainingID.toString()),
                null,
                null,
                FIELD_TIME)
        var loadingTraining : Training = Training(trainingID)
        while (cursor.moveToNext()){
            var location : Location = Location("")
            location.latitude = cursor.getDouble(cursor.getColumnIndex(FIELD_LATITUDE))
            location.longitude = cursor.getDouble(cursor.getColumnIndex(FIELD_LONGITUDE))
            location.time = cursor.getLong(cursor.getColumnIndex(FIELD_TIME))
            loadingTraining.addMeasurement(Measurement(location))
        }
        Log.d(TAG, "end select: $sqLiteDatabase; training $loadingTraining")
        return loadingTraining
    }
}