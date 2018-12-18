package com.nanotrick.nearrbyplaceskotlim.Common

import com.nanotrick.nearrbyplaceskotlim.Remote.IGoogleAPIService
import com.nanotrick.nearrbyplaceskotlim.Remote.RetrofitClient
import retrofit2.create

object Common {
    private val  GOOGLE_API_URL="https://maps.googleapis.com/"
    val googleApiService:IGoogleAPIService
        get()= RetrofitClient.getClient(GOOGLE_API_URL).create(IGoogleAPIService::class.java)

}