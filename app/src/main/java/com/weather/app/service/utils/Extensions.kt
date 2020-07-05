package com.weather.app.service.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

fun Int.getDate(): String {
    try {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = this * 1000.toLong()
        val outputDateFormat = SimpleDateFormat("EEE\ndd", Locale.getDefault())
        outputDateFormat.timeZone = TimeZone.getDefault() // user's default time zone
        return outputDateFormat.format(calendar.time)
    } catch (e: Exception) {
        Log.e("Extensions", "getDate: " + e.message)
    }
    return this.toString()
}

fun Int.getTime(): String {
    try {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = this * 1000.toLong()
        val outputDateFormat = SimpleDateFormat("hh:mm\na", Locale.ENGLISH)
        outputDateFormat.timeZone = TimeZone.getDefault()
        return outputDateFormat.format(calendar.time)
    } catch (e: Exception) {
        Log.e("Extensions", "getTime: " + e.message)
    }
    return this.toString()
}

fun Int.getSunriseSunsetTime(): String {
    try {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = this * 1000.toLong()
        val outputDateFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        outputDateFormat.timeZone = TimeZone.getDefault()
        return outputDateFormat.format(calendar.time)
    } catch (e: Exception) {
        Log.e("Extensions", "getSunriseSunsetTime: " + e.message)
    }
    return this.toString()
}

/**
 * The temperature T in degrees Celsius (°C) is equal to the temperature T in Kelvin (K) minus 273.15:
 * T(°C) = T(K) - 273.15
 *
 * Example
 * Convert 300 Kelvin to degrees Celsius:
 * T(°C) = 300K - 273.15 = 26.85 °C
 */
fun Double.kelvinToCelsius(): Int {
    return (this - 273.15).toInt()
}

fun Int.getVisibility(): Int {
    return (this / 1000)
}