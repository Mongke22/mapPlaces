package com.example.mapplaces.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mapplaces.R
import com.example.mapplaces.models.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.image.ImageProvider
import kotlinx.android.synthetic.main.activity_place_on_map.*
import kotlinx.android.synthetic.main.activity_yandex_map.*

class PlaceOnMap : AppCompatActivity() {

    private val markerDataList = mutableListOf<PlacemarkMapObject>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_on_map)
        val lon = intent.getDoubleExtra("lon",0.0)
        val lat = intent.getDoubleExtra("lat",0.0)
        mvPlaceOnMap.map.move(
            CameraPosition(
                com.yandex.mapkit.geometry.Point(
                    lat, lon
                ),
                8.0f, 0.0f, 0.0f
            )
        )
        markerDataList.add(
            mvPlaceOnMap.map.mapObjects.addPlacemark(
                com.yandex.mapkit.geometry.Point(lat,lon),
                ImageProvider.fromResource(this, R.drawable.place_mark)
            )
        )

    }
}