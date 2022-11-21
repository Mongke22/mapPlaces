package com.example.mapplaces.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.example.mapplaces.models.PlaceModel

class DataBaseHandler(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    companion object{
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "PlacesDataBase"
        private const val TABLE_PLACE = "PlacesTable"

        private const val KEY_ID = "_id"
        private const val KEY_TITLE = "title"
        private const val KEY_IMAGE = "image"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_DATE = "date"
        private const val KEY_LOCATION = "location"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_PLACE_TABLE = ("CREATE TABLE $TABLE_PLACE($KEY_ID INTEGER PRIMARY KEY, $KEY_TITLE TEXT, $KEY_IMAGE TEXT," +
                "$KEY_DESCRIPTION TEXT, $KEY_DATE TEXT, $KEY_LOCATION TEXT, $KEY_LONGITUDE TEXT, $KEY_LATITUDE TEXT)")
        db?.execSQL(CREATE_PLACE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PLACE")
        onCreate(db)
    }
    fun addPlace(place: PlaceModel): Long{
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_DATE, place.date)
        contentValues.put(KEY_DESCRIPTION, place.description)
        contentValues.put(KEY_IMAGE, place.image)
        contentValues.put(KEY_TITLE, place.title)
        contentValues.put(KEY_LOCATION, place.location)
        contentValues.put(KEY_LONGITUDE, place.longitude)
        contentValues.put(KEY_LATITUDE, place.latitude)
        val result = db.insert(TABLE_PLACE, null, contentValues)
        db.close()
        return result
    }
    fun updatePlace(place: PlaceModel): Int{
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_DATE, place.date)
        contentValues.put(KEY_DESCRIPTION, place.description)
        contentValues.put(KEY_IMAGE, place.image)
        contentValues.put(KEY_TITLE, place.title)
        contentValues.put(KEY_LOCATION, place.location)
        contentValues.put(KEY_LONGITUDE, place.longitude)
        contentValues.put(KEY_LATITUDE, place.latitude)
        val result = db.update(TABLE_PLACE,  contentValues, "$KEY_ID=${place.id}", null)
        db.close()
        return result
    }

    fun deletePlace(place: PlaceModel): Int{
        val db = this.writableDatabase
        val result = db.delete(TABLE_PLACE, "$KEY_ID=${place.id}", null)
        db.close()
        return result
    }
    @SuppressLint("Range")
    fun getPlacesList(): ArrayList<PlaceModel>{
        val placeList: ArrayList<PlaceModel> = ArrayList<PlaceModel>()
        val db = this.readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_PLACE"
        try{
            val cursor: Cursor = db.rawQuery(selectQuery, null)
            if(cursor.moveToFirst()){
                do{
                    val place = PlaceModel(
                        cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                        cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                        cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(KEY_IMAGE)),
                        cursor.getString(cursor.getColumnIndex(KEY_DATE)),
                        cursor.getString(cursor.getColumnIndex(KEY_LOCATION)),
                        cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE)),
                    )
                    placeList.add(place)

                }while (cursor.moveToNext())
            }
            cursor.close()
        }catch (e: SQLiteException){
            db.execSQL(selectQuery)
            e.printStackTrace()
            return ArrayList()
        }
        return placeList
    }
}