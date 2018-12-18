package com.nanotrick.nearrbyplaceskotlim.Remote

import com.nanotrick.nearrbyplaceskotlim.Model.MyPlaces
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface IGoogleAPIService {
    @GET
    fun getNearbyPlace(@Url url:String) :Call<MyPlaces>
}