package com.example.mapplaces.activities

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.*
import com.example.mapplaces.R
import com.example.mapplaces.database.DataBaseHandler
import com.example.mapplaces.models.PlaceModel
import com.example.mapplaces.utils.Constants
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.*
import com.yandex.runtime.image.ImageProvider
import com.yandex.runtime.ui_view.ViewProvider
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_all_places_on_map.*
import kotlinx.android.synthetic.main.activity_yandex_map.*

class AllPlacesOnMapActivity : AppCompatActivity() {
    private var viewMarkers: MutableMap<MapObject, MapObject> = mutableMapOf()

    private var lastMark: MapObject? = null

    val tapListener = MapObjectTapListener(){ mapObject, point ->
        viewMarkers[mapObject]!!.isVisible = !viewMarkers[mapObject]!!.isVisible
        if(lastMark != null && lastMark != mapObject){
            viewMarkers[lastMark!!]!!.isVisible = false
        }
        lastMark = mapObject
        true

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_places_on_map)

        val mapObjects: MapObjectCollection = mvAllPlacesYandexMap.getMap().getMapObjects()

        val dbHandler = DataBaseHandler(this)
        val placesList: ArrayList<PlaceModel> = dbHandler.getPlacesList()
        for(place in placesList){
            val placeMarker = mapObjects.addPlacemark(
                Point(place.latitude,place.longitude),
                ImageProvider.fromResource(this, R.drawable.place_mark)
            )
            createPlacemarkMapObjectWithViewProvider(placeMarker, place)

            placeMarker.addTapListener (tapListener)
        }
        ivTiltAllPlacesOnMap.setOnClickListener{
            mvAllPlacesYandexMap.map.move(
                CameraPosition(
                    mvAllPlacesYandexMap.map.cameraPosition.target,
                    mvAllPlacesYandexMap.map.cameraPosition.zoom, 0.0f, 0.0f
                ), Animation(Animation.Type.SMOOTH,3.0f), null
            )
        }
    }
    private fun createPlacemarkMapObjectWithViewProvider(mapObject: MapObject, place: PlaceModel){
        val mapObjects: MapObjectCollection = mvAllPlacesYandexMap.getMap().getMapObjects()
        var viewProvider = ViewProvider(initializeViewElement(place))
        viewMarkers[mapObject] = mapObjects.addPlacemark(
            Point(place.latitude, place.longitude),
            viewProvider
        )
        viewMarkers[mapObject]!!.isVisible = false
    }
    private fun initializeViewElement(place: PlaceModel): FrameLayout {
        var frameLayout = FrameLayout(this@AllPlacesOnMapActivity)

        val pic = ImageView(this)
        pic.setImageResource(R.drawable.speech_bubble)
        val layoutParams =
            FrameLayout.LayoutParams(1024, 1024)
        layoutParams.gravity = Gravity.CENTER
        pic.setPadding(512,0,0,512)
        pic.layoutParams = layoutParams
        pic.alpha = 0.8f

        val textView = TextView(this)
        textView.setText(place.title)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16.0f)
        textView.gravity = Gravity.CENTER_HORIZONTAL

        val CircleImageView = CircleImageView(this)
        CircleImageView.setImageURI(Uri.parse(place.image))


        val llView = LinearLayout(this)
        val llParams =
            FrameLayout.LayoutParams(312,286)
        llParams.gravity = Gravity.CENTER
        llParams.leftMargin = 256
        llParams.bottomMargin = 289
        llView.layoutParams = llParams
        llView.orientation = LinearLayout.VERTICAL
        llView.addView(textView)
        llView.addView(CircleImageView)

        frameLayout.addView(pic)
        frameLayout.addView(llView)
        return frameLayout
    }
}