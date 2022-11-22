package com.example.mapplaces.activities

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.mapplaces.R
import com.example.mapplaces.models.Point
import com.example.mapplaces.retrofitRequestResponse.SharedViewModel
import com.example.mapplaces.utils.Constants
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.map.*
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.search.*
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import com.yandex.runtime.network.NetworkError
import com.yandex.runtime.network.RemoteError
import kotlinx.android.synthetic.main.activity_yandex_map.*


class YandexMapActivity : AppCompatActivity(),Session.SearchListener, CameraListener {

    private var searchManager: SearchManager? = null
    private var searchSession: Session? = null
    private val markerDataList = mutableListOf<PlacemarkMapObject>()
    private var currentCameraPosition = com.yandex.mapkit.geometry.Point(0.0,0.0)
    private val viewModel: SharedViewModel by lazy{
        ViewModelProvider(this).get(SharedViewModel::class.java)
    }
    private var resultPoint = Point(0.0,0.0)
    private var resultAddress = "Error"

    private fun submitQuery(query: String) {
        searchSession = searchManager!!.submit(
            query,
            VisibleRegionUtils.toPolygon(mvYandex.map.visibleRegion),
            SearchOptions(),
            this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_yandex_map)

        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
        mvYandex.map.addCameraListener(this)
        currentCameraPosition = mvYandex.map.cameraPosition.target

        initializeMapWithData()

        etYandex.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                if (event.action == KeyEvent.ACTION_DOWN &&
                    keyCode == KeyEvent.KEYCODE_ENTER
                ) {
                    submitQuery(etYandex.text.toString())
                    etYandex.clearFocus()
                    etYandex.isCursorVisible = false

                    return true
                }
                return false
            }
        })

        //Изменение прозрачности поля для ввода
        etYandex.setOnFocusChangeListener{ _, hasFocus ->
            if(hasFocus){
                etYandex.alpha = 1F
            }
            else{
                etYandex.alpha = 0.5F
                hideKeyBoard()
            }

        }
        ivSearch.setOnClickListener{
            submitQuery(etYandex.text.toString())
            etYandex.clearFocus()
            etYandex.isCursorVisible = false
        }
        fabChoosePlace.setOnClickListener{
            currentCameraPosition = mvYandex.map.cameraPosition.target
            val mapObjects: MapObjectCollection = mvYandex.getMap().getMapObjects()
            mapObjects.clear()
            markerDataList.add(mapObjects.addPlacemark(
                currentCameraPosition,
                ImageProvider.fromResource(this, R.drawable.place_mark)
            ))
            etYandex.setText("lat:${String.format("%.3f", currentCameraPosition.latitude)} lon:${String.format("%.3f", currentCameraPosition.longitude)}")
            /*if(Constants.isNetWorkAvailable(this@YandexMapActivity)) {
                setAddressByPoint(currentCameraPosition)
            }
            else{
                Constants.hideProgressDialog()
                Constants.makeToast("Отсутствует интернет соединение", this@YandexMapActivity)
            }*/
            resultPoint.lat = currentCameraPosition.latitude
            resultPoint.lon = currentCameraPosition.longitude
        }
        fabSubmitPlace.setOnClickListener{
            checkIfPlaceSelectedAndSelect()
            val resultAddressIntent = Intent()
            Constants.ADDRESS_DATA = resultAddress
            Constants.RESULT_POINT = resultPoint
            setResult(RESULT_OK, resultAddressIntent)
            finish()
        }
        ivTilt.setOnClickListener{
            mvYandex.map.move(
                CameraPosition(
                    currentCameraPosition,
                    mvYandex.map.cameraPosition.zoom, 0.0f, 0.0f
                ), Animation(Animation.Type.SMOOTH,3.0f), null
            )
        }

    }

    private fun initializeMapWithData(){
        val lon = intent.getDoubleExtra("lon",0.0)
        val lat = intent.getDoubleExtra("lat",0.0)
        if(lon != 0.0 || lat != 0.0) {
            currentCameraPosition = com.yandex.mapkit.geometry.Point(lat,lon)
            resultPoint = Point(lat,lon)
            resultAddress = intent.getStringExtra("Address")!!
            mvYandex.map.move(
                CameraPosition(
                    com.yandex.mapkit.geometry.Point(
                        lat, lon
                    ),
                    14.0f, 0.0f, 0.0f
                )
            )
            markerDataList.add(
                mvYandex.map.mapObjects.addPlacemark(
                    currentCameraPosition,
                    ImageProvider.fromResource(this, R.drawable.place_mark)
                )
            )
            etYandex.setText(resultAddress)
        }
    }
    private fun checkIfPlaceSelectedAndSelect(){
        if(resultPoint.lat == 0.0 && resultPoint.lon == 0.0){
           fabChoosePlace.callOnClick()
        }
    }

    private fun setAddressByPoint(point: com.yandex.mapkit.geometry.Point){
        viewModel.refreshAddress(point.longitude, point.latitude)
        viewModel.addressByLonLatLiveData.observe(this){ response ->
            if(response == null){
                etYandex.setText("Error")
                Constants.hideProgressDialog()
                return@observe
            }
            if(response.meta.code == 404){
                etYandex.setText("lat:${String.format("%.3f", point.latitude)} lon:${String.format("%.3f", point.longitude)}")
                resultAddress = etYandex.text.toString()
            }else {
                resultAddress = response.result.items[0].full_name
                etYandex.setText(resultAddress)
            }
            Constants.hideProgressDialog()
        }
    }

    private fun hideKeyBoard(){
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(
            etYandex.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }
//
    override fun onStop() {
        mvYandex.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mvYandex.onStart()
    }

    override fun onSearchResponse(response: Response) {
        val mapObjects: MapObjectCollection = mvYandex.getMap().getMapObjects()
        if(response.collection.children.isNotEmpty()) {
            val resultLocation = response.collection.children[0].obj!!.geometry[0].point
            if (resultLocation != null) {
                    mvYandex.map.move(
                        CameraPosition(
                            resultLocation,
                            14.0f, 0.0f, 0.0f
                        )
                    )
                    mapObjects.clear()
                    markerDataList.add(
                        mapObjects.addPlacemark(
                            resultLocation,
                            ImageProvider.fromResource(this, R.drawable.place_mark)
                        )
                    )

                    resultPoint.lat = resultLocation.latitude
                    resultPoint.lon = resultLocation.longitude
                    resultAddress = etYandex.text.toString()

            }
        }
    }


    override fun onSearchError(error: Error) {
        var errorMessage = getString(R.string.unknown_error_message)
        if (error is RemoteError) {
            errorMessage = getString(R.string.remote_error_message)
        } else if (error is NetworkError) {
            errorMessage = getString(R.string.network_error_message)
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onCameraPositionChanged(
        map: Map,
        cameraPosition: CameraPosition,
        cameraUpdateReason: CameraUpdateReason,
        finished: Boolean
    ) {

    }

}