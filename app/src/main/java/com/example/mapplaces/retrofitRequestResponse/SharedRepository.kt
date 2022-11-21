package com.example.mapplaces.retrofitRequestResponse

import com.example.mapplaces.models.GetAddressFromLonLatResponse

class SharedRepository {
    suspend fun getAddressByLonLat(lon: Double, lat: Double): GetAddressFromLonLatResponse?{
        val request = NetworkLayer.apiClient.getAddressByLonLat(lon,lat)
        if(request.isSuccessful){
            return request.body()!!
        }
        return null
    }
}