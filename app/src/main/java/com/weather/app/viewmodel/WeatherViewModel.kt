package com.weather.app.viewmodel

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory
import com.weather.app.WeatherApplication
import com.weather.app.service.model.WeatherModel
import com.weather.app.service.repository.WeatherRepository

class WeatherViewModel(
    application: WeatherApplication,
    lat: Double?,
    long: Double?,
    id: String?,
    appId: String?
) : AndroidViewModel(application) {
    private var weatherObservable: LiveData<WeatherModel?>? = null
//    var weatherModel: ObservableField<WeatherModel> = ObservableField()

    init {
        weatherObservable = WeatherRepository.instance?.getWeatherDetails(lat, long, id, appId)
    }

    fun getObservableWeatherModel(): LiveData<WeatherModel?>? {
        return weatherObservable
    }

//    fun setWeatherModel(weatherModel: WeatherModel) {
//        this.weatherModel.set(weatherModel)
//    }

    /**
     * A creator is used to inject the project ID into the ViewModel
     */
    class Factory(
        private val application: WeatherApplication,
        private val lat: Double,
        private val long: Double,
        private val id: String,
        private val appId: String
    ) :
        NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return WeatherViewModel(application, lat, long, id, appId) as T
        }
    }
}