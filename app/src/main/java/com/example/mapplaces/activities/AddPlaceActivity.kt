package com.example.mapplaces.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.mapplaces.R
import com.example.mapplaces.database.DataBaseHandler
import com.example.mapplaces.models.PlaceModel
import com.example.mapplaces.models.Point
import com.example.mapplaces.retrofitRequestResponse.SharedViewModel
import com.example.mapplaces.utils.Constants
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.search.SearchFactory
import kotlinx.android.synthetic.main.activity_add_place.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AddPlaceActivity : AppCompatActivity(), View.OnClickListener {

    private var cal = Calendar.getInstance()
    private lateinit var  dateSetListener: DatePickerDialog.OnDateSetListener

    private var saveImageToInternalStorage: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0


    private var mPlaceDetails: PlaceModel? = null
    private val viewModel: SharedViewModel by lazy{
        ViewModelProvider(this).get(SharedViewModel::class.java)
    }

    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    //Как получить строку из файла со строками
    //private var str: String = resources.getString(R.string.app_name)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapKitFactory.initialize(this)
        SearchFactory.initialize(this)

        setContentView(R.layout.activity_add_place)
        setSupportActionBar(tbAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        tbAddPlace.setNavigationOnClickListener{
            onBackPressed()
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        dateSetListener = DatePickerDialog.OnDateSetListener {
                _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateView()
        }
        updateDateView()

        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mPlaceDetails = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as PlaceModel?
            supportActionBar?.title = "Edit Place"
            et_title.setText(mPlaceDetails!!.title)
            et_description.setText(mPlaceDetails!!.description)
            et_date.setText(mPlaceDetails!!.date)
            et_location.setText(mPlaceDetails!!.location)
            mLatitude = mPlaceDetails!!.latitude
            mLongitude = mPlaceDetails!!.longitude

            saveImageToInternalStorage = Uri.parse(mPlaceDetails!!.image)
            iv_place_image.setImageURI(saveImageToInternalStorage)
            btn_save.text = "UPDATE"
        }

        et_date.setOnClickListener(this)
        tv_add_image.setOnClickListener(this)
        btn_save.setOnClickListener(this)
        et_location.setOnClickListener(this)
        tvSelectCurrentLocation.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.et_date -> {
                DatePickerDialog(this@AddPlaceActivity, dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
            }
            R.id.tv_add_image -> {
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems = arrayOf("Select photo from gallery","Capture photo from camera")
                pictureDialog.setItems(pictureDialogItems){
                        _, which ->
                    when(which){
                        0 -> {
                            choosePhotoFromGallery()
                        }
                        1 -> {
                            takePhotoFromCamera()
                        }
                    }
                }
                pictureDialog.show()
            }
            R.id.btn_save -> {
                when {
                    et_title.text.isNullOrEmpty() -> {
                        Constants.makeToast("Please enter title", this@AddPlaceActivity)
                        et_title.error = "Shouldn't be empty"
                    }
                    et_description.text.isNullOrEmpty() -> {
                        Constants.makeToast("Please enter description",this@AddPlaceActivity)
                        et_description.error = "Shouldn't be empty"
                    }
                    et_location.text.isNullOrEmpty() ->{
                        Constants.makeToast("Please enter location",this@AddPlaceActivity)
                        et_location.error = "Shouldn't be empty"
                    }
                    saveImageToInternalStorage == null -> {
                        Constants.makeToast("Please select image", this@AddPlaceActivity)
                    }
                    else -> {
                        val placeModel = PlaceModel(
                            if(mPlaceDetails == null) 0 else mPlaceDetails!!.id,
                            et_title.text.toString(),
                            et_description.text.toString(),
                            saveImageToInternalStorage.toString(),
                            et_date.text.toString(),
                            et_location.text.toString(),
                            mLatitude, mLongitude
                        )
                        val dbHandler = DataBaseHandler(this)
                        if(mPlaceDetails == null) {
                            val addPlace = dbHandler.addPlace(placeModel)
                            if(addPlace > 0){
                                setResult(RESULT_OK)
                            }else{
                                Constants.makeToast("Something went wrong with adding a place",this@AddPlaceActivity)
                            }
                        } else{
                            val updatePlace = dbHandler.updatePlace(placeModel)
                            if(updatePlace > 0){
                                setResult(RESULT_OK)
                            }else{
                                Constants.makeToast("Something went wrong with updating a place",this@AddPlaceActivity)
                            }
                        }
                        finish()
                    }
                }
            }
            R.id.et_location -> {
                val intent = Intent(this@AddPlaceActivity, YandexMapActivity::class.java)
                intent.putExtra("Address",et_location.text.toString())
                intent.putExtra("lat",mLatitude)
                intent.putExtra("lon",mLongitude)
                startActivityForResult(intent,PLACE_AUTOCOMPLETE_REQUEST_CODE)
            }
            R.id.tvSelectCurrentLocation -> {
                if(!isLocationEnabled()){
                    Constants.makeToast("Location locked", this@AddPlaceActivity)
                    showRationalDialogForLocation()
                }else{
                    Dexter.withContext(this@AddPlaceActivity).withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ).withListener(object: MultiplePermissionsListener{
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            if(report!!.areAllPermissionsGranted()){
                                requestNewLocationData()
                            }
                        }
                        override fun onPermissionRationaleShouldBeShown(
                            permissions: MutableList<PermissionRequest>?,
                            token: PermissionToken?
                        ) {
                            showRationalDialogForPermissions()
                        }

                    }).withErrorListener{
                        Constants.makeToast("Error with location permissions",this@AddPlaceActivity)
                    }.check()
                }

            }
        }
    }

    private fun isLocationEnabled(): Boolean{
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun takePhotoFromCamera() {
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).withListener(object: MultiplePermissionsListener {
            override fun  onPermissionsChecked(report: MultiplePermissionsReport?) {
                if(report!!.areAllPermissionsGranted()){
                    val cameraIntent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, CAMERA)
                }
            }
            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>,token: PermissionToken) {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check();
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == GALLERY){
                if(data != null){
                    val contentURI = data.data
                    try{
                        val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                        iv_place_image.setImageBitmap(selectedImageBitmap)
                        saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap)
                        Log.i("Gallery img", "Path: $saveImageToInternalStorage")
                    }catch (e: IOException){
                        e.printStackTrace()
                        Constants.makeToast("Something went wrong with loading pict",this@AddPlaceActivity,  true)
                    }
                }
            } else if(requestCode == CAMERA){
                val thumbNail: Bitmap = data!!.extras!!.get("data") as Bitmap
                iv_place_image.setImageBitmap(thumbNail)
                saveImageToInternalStorage =  saveImageToInternalStorage(thumbNail)
                Log.i("Camera img", "Path: $saveImageToInternalStorage")
            } else if(requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE){

                mLatitude = Constants.RESULT_POINT.lat
                mLongitude = Constants.RESULT_POINT.lon
                et_location.setText(Constants.ADDRESS_DATA)
                //et_title.setText(mLongitude.toString())
                //et_title.setText(mLatitude.toString())
            }
        }
    }
    private fun choosePhotoFromGallery(){
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object: MultiplePermissionsListener {
            override fun  onPermissionsChecked(report: MultiplePermissionsReport?) {
                if(report!!.areAllPermissionsGranted()){
                    val galleryIntent: Intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY)
                }
            }
            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>,token: PermissionToken) {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check();
    }
    private fun showRationalDialogForPermissions(){
        AlertDialog.Builder(this).setMessage("Please turn on location status").setPositiveButton("Go to settings"){
            _,_ ->
            try {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            } catch(e: ActivityNotFoundException){
                e.printStackTrace()
            }
        }.setNegativeButton("Cancel"){
            dialog, _ ->
            dialog.dismiss()
        }.show()
    }
    private fun showRationalDialogForLocation(){

    }

    private fun updateDateView(){
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        et_date.setText(sdf.format(cal.time).toString())
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri{
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")
        try{
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        }catch (e: IOException){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }


    @SuppressLint("MissingPermission")
    private fun requestNewLocationData(){
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 1000
        mLocationRequest.numUpdates = 1

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallBack, Looper.myLooper())
    }

    private val mLocationCallBack = object : LocationCallback(){
        override fun onLocationResult(result: LocationResult) {
            val mLastLocation: Location = result!!.lastLocation!!
            mLatitude = mLastLocation.latitude
            mLongitude = mLastLocation.longitude
            setAddressFromLonLat(mLatitude, mLongitude)

        }
    }

    private fun setAddressFromLonLat(lon: Double, lat: Double){
        Constants.showCustomProgressDialog(this@AddPlaceActivity)
        if(Constants.isNetWorkAvailable(this@AddPlaceActivity)){
            getAddressFromLonLat(lon,lat)
        } else{
            Constants.hideProgressDialog()
            Constants.makeToast("Отсутствует интернет соединение", this@AddPlaceActivity)

        }
    }

    private fun getAddressFromLonLat(lat: Double, lon: Double){
        viewModel.refreshAddress(lon, lat)
        viewModel.addressByLonLatLiveData.observe(this){ response ->
            Constants.hideProgressDialog()
            if(response == null){
                return@observe
            }
            if(response.meta.code == 404){
                et_location.setText("Not found")
            }else {
                et_location.setText(response.result.items[0].full_name)
            }
        }
    }


    companion object{
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "PlacesImages"
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
    }


}