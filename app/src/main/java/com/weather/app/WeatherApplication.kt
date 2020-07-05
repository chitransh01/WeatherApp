package com.weather.app

import android.app.Application
import android.content.Context
import android.content.res.Configuration

class WeatherApplication : Application() {
    companion object {
        private var mInstance: WeatherApplication? = null

        /**
         * This method will return the instance of the application class
         *
         * @param context
         */
        fun getInstance(context: Context): WeatherApplication {
            if (mInstance == null) {
                mInstance = context.applicationContext as WeatherApplication
            }
            return mInstance as WeatherApplication
        }
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }
}