package com.example.stormy

import java.text.SimpleDateFormat
import java.util.*

data class CurrentWeather (val locationLabel: String, val icon: String, val time: Long,
                           val temperature:Double, val humidity:Double, val precipChance: Double,
                           val summary:String, val timeZone: String) {

    fun getFormattedTime():String {
        val formatter: SimpleDateFormat = SimpleDateFormat("h:mm a")
        formatter.timeZone = TimeZone.getTimeZone(timeZone)
        val dateTime = Date(time*1000)
        return formatter.format(dateTime)
    }

    fun getIconID():Int {
         return when (icon) {
            "clear-day" -> R.drawable.clear_day
            "clear-night" -> R.drawable.clear_night
            "rain" -> R.drawable.rain
            "snow" -> R.drawable.snow
            "sleet" -> R.drawable.sleet
            "wind" -> R.drawable.wind
            "fog" -> R.drawable.fog
            "cloudy" -> R.drawable.cloudy
            "partly-cloudy-day" -> R.drawable.partly_cloudy
            else -> R.drawable.cloudy_night
        }
    }
}

