package com.weather.app.view.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.weather.app.R
import com.weather.app.WeatherApplication
import com.weather.app.service.model.WeatherModel
import com.weather.app.service.utils.*
import com.weather.app.view.callbacks.OnOkButtonClickListener
import com.weather.app.view.constants.Constants
import com.weather.app.view.dialog.DialogUtils
import com.weather.app.viewmodel.WeatherViewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener {
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private var mylocation: Location? = null
    private var googleApiClient: GoogleApiClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {
            setSupportActionBar(toolbar)
            ivMoreDetails.setOnClickListener(this)
            consWeather.visibility = View.GONE
            if (NetworkUtil.isConnectedToWifi(this)) {
                setUpGClient()
            } else {
                showAlertMessage()
            }
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: " + e.message)
        }
    }

    /**
     * This method is used to setup google client
     */
    private fun setUpGClient() {
        try {
            googleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
            googleApiClient?.connect()
        } catch (e: Exception) {
            Log.e(TAG, "setUpGClient: " + e.message)
        }
    }

    /**
     * This method is used to show alert message for no wifi connectivity
     */
    private fun showAlertMessage() {
        try {
            DialogUtils.showAlertDialog(this,
                getString(R.string.title_no_wifi_network),
                getString(R.string.msg_no_wifi_connectivity),
                getString(R.string.btn_ok_bye),
                object : OnOkButtonClickListener {
                    override fun onOkButtonCLicked() {
                        finish()
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "setUpGClient: " + e.message)
        }
    }

    override fun onConnected(p0: Bundle?) {
        checkPermissions()
    }

    /**
     * This method is used to check the permission
     */
    private fun checkPermissions() {
        try {
            val permission =
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    Constants.REQUEST_CODE_LOCATION
                )
            } else {
                getLocation()
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkPermissions: " + e.message)
        }
    }

    /**
     * This method is used to get location
     */
    @Suppress("DEPRECATION")
    private fun getLocation() {
        try {
            if (googleApiClient != null) {
                if (googleApiClient?.isConnected!!) {
                    val permissionLocation = ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                        mylocation =
                            LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
                        val locationRequest = LocationRequest()
                        locationRequest.interval = Constants.LOCATION_INTERVAL
                        locationRequest.fastestInterval = Constants.LOCATION_INTERVAL
                        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                        val builder =
                            LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
                        builder.setAlwaysShow(true)
                        LocationServices.FusedLocationApi.requestLocationUpdates(
                            googleApiClient,
                            locationRequest,
                            this
                        )
                        val result: PendingResult<LocationSettingsResult> =
                            LocationServices.SettingsApi
                                .checkLocationSettings(googleApiClient, builder.build())
                        result.setResultCallback { resultL ->
                            val status: Status = resultL.status
                            when (status.statusCode) {
                                LocationSettingsStatusCodes.SUCCESS -> {
                                    // All location settings are satisfied.
                                    // You can initialize location requests here.
                                    mylocation = LocationServices.FusedLocationApi.getLastLocation(
                                        googleApiClient
                                    )
                                }
                                LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                                    // Location settings are not satisfied.
                                    // But could be fixed by showing the user a dialog.
                                    try {
                                        // Show the dialog by calling startResolutionForResult(),
                                        // and check the result in onActivityResult().
                                        // Ask to turn on GPS automatically
                                        status.startResolutionForResult(
                                            this@MainActivity,
                                            Constants.REQUEST_CHECK_GPS_SETTINGS
                                        )
                                    } catch (e: SendIntentException) {
                                        Log.e(TAG, "RESOLUTION_REQUIRED: " + e.message)
                                    }
                                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "getLocation: " + e.message)
        }
    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    override fun onLocationChanged(location: Location?) {
        try {
            val lat = location?.latitude
            val long = location?.longitude
            val result = "LAT: $lat\nLONG: $long"
            Log.d(TAG, result)

            progressBar.visibility = View.VISIBLE
            fetchWeatherDataAsPerLocation(lat, long)
        } catch (e: Exception) {
            Log.e(TAG, "onLocationChanged: " + e.message)
        }
    }

    /**
     * This method is used to fetch weather based on location coordinates
     *
     * @param lat This param defines the latitude of current location
     * @param long This param defines the longitude of current location
     */
    private fun fetchWeatherDataAsPerLocation(lat: Double?, long: Double?) {
        if (null != lat && null != long) {
            val factory = WeatherViewModel.Factory(
                WeatherApplication.getInstance(this),
                lat,
                long,
                Constants.ID,
                Constants.APP_ID
            )
            val weatherViewModel =
                ViewModelProviders.of(this, factory).get(WeatherViewModel::class.java)
            //                mBinding.weatherViewModel = weatherViewModel
            observeViewModel(weatherViewModel)
        }
    }

    /**
     * This methood is used to get observable data from API
     *
     * @param weatherViewModel
     */
    private fun observeViewModel(weatherViewModel: WeatherViewModel) {
        try {
            weatherViewModel.getObservableWeatherModel()?.observe(this, Observer { weatherModel ->
                if (weatherModel != null) {
                    progressBar.visibility = View.GONE
//                    weatherViewModel.setWeatherModel(weatherModel)
                    setWeatherInfo(weatherModel)
                } else {
                    Toast.makeText(this, getString(R.string.msg_api_error), Toast.LENGTH_SHORT)
                        .show()
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "observeViewModel: " + e.message)
        }
    }

    /**
     * This method is used to set weather info
     *
     * @param weatherModel This param has all weather information
     */
    private fun setWeatherInfo(weatherModel: WeatherModel) {
        try {
            consWeather.visibility = View.VISIBLE
            // Set day name
            val day = Calendar.getInstance()
                .getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
            tvDay.text = day
            // Set weather type
            val weatherType = weatherModel.weather?.get(0)?.main
            tvThunderStorm.text = weatherType
            // Set weather icon
            setWeatherIcon(weatherType)
            // Set city name
            val cityName = weatherModel.name
            tvCity.text = cityName
            // Set visibility
            val visibility =
                "Visibility: " + weatherModel.visibility?.getVisibility().toString() + " Km"
            tvVisibility.text = visibility
            // Set temperature
            setTemperature(weatherModel)
            // Set time
            val time = weatherModel.dt?.getTime().toString()
            tvTime.text = time
            // Set date
            val date = weatherModel.dt?.getDate().toString()
            tvDate.text = date
            // Set wind
            val wind = weatherModel.wind?.speed.toString() + " Km/h"
            tvWindValue.text = wind
            // Set humidity
            val humidity = weatherModel.main?.humidity.toString() + " %"
            tvHumidityValue.text = humidity
            // Set pressure
            val pressure = weatherModel.main?.pressure.toString() + " hPa"
            tvPressureValue.text = pressure
            // Set sunrise
            val sunrise = weatherModel.sys?.sunrise?.getSunriseSunsetTime()
            tvSunriseTime.text = sunrise
            // Set sunset
            val sunset = weatherModel.sys?.sunset?.getSunriseSunsetTime()
            tvSunsetTime.text = sunset
        } catch (e: Exception) {
            Log.e(TAG, "setWeatherInfo: " + e.message)
        }
    }

    /**
     * THis method is used to set weather type icon
     *
     * @param weatherType This param gets the weather type
     */
    private fun setWeatherIcon(weatherType: String?) {
        try {
            when (weatherType) {
                getString(R.string.weather_sunny) -> {
                    ivWeather.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.weather_sunny
                        )
                    )
                }
                getString(R.string.weather_partially_clouds) -> {
                    ivWeather.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.weather_partial_cloudy
                        )
                    )
                }
                getString(R.string.weather_clouds), getString(R.string.weather_cloudy), getString(R.string.weather_haze) -> {
                    ivWeather.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.weather_cloudy
                        )
                    )
                }
                getString(R.string.weather_thinderstorm) -> {
                    ivWeather.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.weather_lightning
                        )
                    )
                }
                getString(R.string.weather_rain), getString(R.string.weather_showers) -> {
                    ivWeather.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.weather_raining
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "setWeatherIcon: " + e.message)
        }
    }

    /**
     * This method is used to set temperature
     */
    private fun setTemperature(weatherModel: WeatherModel) {
        try {// Set temperature, min & max temperature & progress
            val temp = weatherModel.main?.temp?.kelvinToCelsius().toString()
            tvTemperature.text = getString(R.string.temperature_degree, temp)
            tempProgress.progress = temp.toInt()
            val minTemp = weatherModel.main?.tempMin?.kelvinToCelsius().toString()
            tvTempMin.text = getString(R.string.min_temperature_degree, minTemp)
            val maxTemp = weatherModel.main?.tempMax?.kelvinToCelsius().toString()
            tvTempMax.text = getString(R.string.max_temperature_degree, maxTemp)
        } catch (e: Exception) {
            Log.e(TAG, "setTemperature: " + e.message)
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        when (requestCode) {
            Constants.REQUEST_CHECK_GPS_SETTINGS -> when (resultCode) {
                Activity.RESULT_OK -> getLocation()
                Activity.RESULT_CANCELED -> {
                    Toast.makeText(this, getString(R.string.msg_not_enabled), Toast.LENGTH_SHORT)
                        .show()
                }
            }
            Constants.REQUEST_CODE_LOCATION -> when (resultCode) {
                Activity.RESULT_CANCELED -> {
                    Toast.makeText(
                        this,
                        getString(R.string.msg_permission_denied),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val permissionLocation = ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
            getLocation()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuSettings -> {
                Toast.makeText(this, getString(R.string.msg_menu_cliked), Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivMoreDetails -> {
                Toast.makeText(this, getString(R.string.msg_weather_details), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}