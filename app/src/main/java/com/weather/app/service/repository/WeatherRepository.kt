package com.weather.app.service.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.weather.app.service.model.WeatherModel
import com.weather.app.view.constants.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRepository private constructor() {
    private var apiService: APIService? = null

    fun getWeatherDetails(
        lat: Double?,
        long: Double?,
        id: String?,
        appId: String?
    ): LiveData<WeatherModel?> {
        val data: MutableLiveData<WeatherModel?> = MutableLiveData()
        apiService?.getWeather(lat, long, id, appId)?.enqueue(object : Callback<WeatherModel?> {
            override fun onResponse(call: Call<WeatherModel?>?, response: Response<WeatherModel?>) {
                if (response.code() == 200) {
                    Log.d(TAG, "onResponse: " + response.code())
                    data.value = response.body()
                } else {
                    data.value = null
                }
            }

            override fun onFailure(call: Call<WeatherModel?>?, t: Throwable?) {
                Log.e(TAG, "onFailure: " + t?.message)
                data.value = null
            }
        })
        return data
    }

    companion object {
        private val TAG = WeatherRepository::class.java.simpleName
        private var weatherRepository: WeatherRepository? = null

        @get:Synchronized
        val instance: WeatherRepository?
            get() {
                if (weatherRepository == null) {
                    weatherRepository = WeatherRepository()
                }
                return weatherRepository
            }
    }

    init {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create<APIService>(APIService::class.java)
    }
}