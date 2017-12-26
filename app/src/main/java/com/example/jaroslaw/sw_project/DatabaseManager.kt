package com.example.jaroslaw.sw_project

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

private val DB_NAME = "TrainingDataBase"
private val DB_VERSION = 1

class DatabaseManager (context: Context): SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION){

    override fun onCreate(sqLiteDatabase: SQLiteDatabase?) {
        upgradeDatabase(sqLiteDatabase, 0, DB_VERSION)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        upgradeDatabase(sqLiteDatabase, oldVersion, newVersion)
    }

    private fun upgradeDatabase(sqLiteDatabase: SQLiteDatabase?, oldVersion: Int, newVersion: Int){
        if (oldVersion < 1){
            //todo create table in database
        }
    }

    fun insertTraining(sqLiteDatabase: SQLiteDatabase ,training: Training){
        val trainingID = training.getTrainingID()
        for(measure in training.getTrainingHistory()){
            //todo insert measurement to database
            var contentValues :ContentValues = ContentValues()
            //contentValues.put(/*column name, value*/)
            //...
            //sqLiteDatabase!!.insert(/*table name, null, contentvalue*/)
        }
    }

    fun selectTraining(sqLiteDatabase: SQLiteDatabase, trainingID : Long){
        //todo select training with specific ID
        //sqLiteDatabase!!.query()
    }
}