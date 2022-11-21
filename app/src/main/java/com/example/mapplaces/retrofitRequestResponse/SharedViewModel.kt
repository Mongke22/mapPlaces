package com.example.mapplaces.retrofitRequestResponse

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapplaces.models.GetAddressFromLonLatResponse
import kotlinx.coroutines.launch

class SharedViewModel: ViewModel() {
    private val repository = SharedRepository()

    private val _addressByLonLatLiveData = MutableLiveData<GetAddressFromLonLatResponse?>()
    val addressByLonLatLiveData: LiveData<GetAddressFromLonLatResponse?> = _addressByLonLatLiveData

    fun refreshAddress(lon: Double, lat: Double){
        viewModelScope.launch {
            val response = repository.getAddressByLonLat(lon, lat)

            _addressByLonLatLiveData.postValue(response)
        }
    }
}