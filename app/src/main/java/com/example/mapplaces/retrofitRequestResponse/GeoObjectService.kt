package com.example.mapplaces.retrofitRequestResponse

import com.example.mapplaces.models.GetAddressFromLonLatResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GeoObjectService {
    //v2/informers
    @GET("3.0/items/geocode")
    suspend fun getAddressByLonLat(@Query("lon") lon: Double,
                   @Query("lat") lat: Double,
                   @Query("fields") fields: String,
                   @Query("key") key: String
    ): Response<GetAddressFromLonLatResponse>
}