package com.example.mapplaces.activities

import SwipeToDeleteCallback
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mapplaces.R
import com.example.mapplaces.activities.AddPlaceActivity
import com.example.mapplaces.adapters.PlacesAdapter
import com.example.mapplaces.database.DataBaseHandler
import com.example.mapplaces.models.PlaceModel
import com.yandex.mapkit.MapKitFactory
import kotlinx.android.synthetic.main.activity_main.*
import pl.kitek.rvswipetodelete.SwipeToEditCallback

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MapKitFactory.setApiKey("4ed0e2f3-609a-4662-bc77-1fb7a28bf454")
        MapKitFactory.setLocale("ru_RU")
        fabAddPlace.setOnClickListener{
            val intent = Intent(this, AddPlaceActivity::class.java)
            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }
        fabViewAllPlaces.setOnClickListener{
            val intent = Intent(this, AllPlacesOnMapActivity::class.java)
            startActivity(intent)
        }
        getPlacesListFromLocalDB()
    }
    private fun setUpPlacesRecyclerView(placeList: ArrayList<PlaceModel>){
        rvPlacesList.layoutManager = LinearLayoutManager(this)
        rvPlacesList.setHasFixedSize(true)
        val placesAdapter = PlacesAdapter(this,placeList)
        rvPlacesList.adapter = placesAdapter
        placesAdapter.setOnClickListener(object: PlacesAdapter.OnClickListener{
            override fun onClick(position: Int, model: PlaceModel) {
                val intent = Intent(this@MainActivity, PlaceDetailActivity::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS, model)
                startActivity(intent)
            }
        })
        val editSwipeHandler = object: SwipeToEditCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rvPlacesList.adapter as PlacesAdapter
                adapter.notifyEditItem(this@MainActivity, viewHolder.absoluteAdapterPosition, ADD_PLACE_ACTIVITY_REQUEST_CODE)
            }
        }
        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(rvPlacesList)

        val deleteSwipeHandler = object : SwipeToDeleteCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rvPlacesList.adapter as PlacesAdapter
                adapter.notifyDeleteItem(viewHolder.absoluteAdapterPosition)
                getPlacesListFromLocalDB()
            }
        }
        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(rvPlacesList)
    }
    private fun getPlacesListFromLocalDB(){
        val dbHandler = DataBaseHandler(this)
        val placesList: ArrayList<PlaceModel> = dbHandler.getPlacesList()
        if(placesList.size > 0){
            rvPlacesList.visibility = View.VISIBLE
            tvNoRecordsAvailable.visibility = View.GONE
            setUpPlacesRecyclerView(placesList)
        }else{
            rvPlacesList.visibility = View.GONE
            tvNoRecordsAvailable.visibility = View.VISIBLE
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                getPlacesListFromLocalDB()
            }else{
                Log.e("Activity", "Canceled")
            }
        }
    }
    companion object{
        const val ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
        const val EXTRA_PLACE_DETAILS = "extra_place_details"
    }
}