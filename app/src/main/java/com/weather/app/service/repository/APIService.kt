package com.weather.app.service.repository

import com.weather.app.service.model.WeatherModel
import com.weather.app.view.constants.Constants
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface APIService {
    @GET(Constants.WEATHER_URL)
    fun getWeather(
        @Query("lat") lat: Double?,
        @Query("lon") lon: Double?,
        @Query("id") id: String?,
        @Query("appid") appId: String?
    ): Call<WeatherModel>
}