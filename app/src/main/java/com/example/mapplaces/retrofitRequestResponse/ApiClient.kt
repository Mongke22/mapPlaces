package com.example.mapplaces.retrofitRequestResponse

import com.example.mapplaces.models.GetAddressFromLonLatResponse
import com.example.mapplaces.utils.Constants
import retrofit2.Response

class ApiClient(
    private val geoObjectService: GeoObjectService
) {
    suspend fun getAddressByLonLat(lon: Double, lat: Double): Response<GetAddressFromLonLatResponse>{
        return geoObjectService.getAddressByLonLat(lon,lat,"items.point", Constants.API_KEY)
    }
}