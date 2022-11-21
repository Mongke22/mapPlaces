package com.example.mapplaces.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mapplaces.R
import com.example.mapplaces.models.PlaceModel
import kotlinx.android.synthetic.main.activity_add_place.*
import kotlinx.android.synthetic.main.activity_place_detail.*
import kotlinx.android.synthetic.main.activity_place_detail.iv_place_image

class PlaceDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_detail)

        var placeDetailModel: PlaceModel? = null
        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            placeDetailModel = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as PlaceModel?
        }
        if(placeDetailModel != null){
            setSupportActionBar(tbPlaceDetail)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = placeDetailModel.title
            tbPlaceDetail.setNavigationOnClickListener{
                onBackPressed()
            }
            iv_place_image.setImageURI(Uri.parse(placeDetailModel.image))
            tv_description.text = placeDetailModel.description
            tv_location.text = placeDetailModel.location
            tv_latitude.text = "lat: ${placeDetailModel.latitude.toString()}"
            tv_longitude.text = "lon: ${placeDetailModel.longitude.toString()}"
        }

        btn_view_on_map.setOnClickListener{
            if(placeDetailModel != null) {
                val intent = Intent(this@PlaceDetailActivity, PlaceOnMap::class.java)
                intent.putExtra("lat", placeDetailModel.latitude)
                intent.putExtra("lon", placeDetailModel.longitude)
                startActivity(intent)
            }
        }
    }
}
