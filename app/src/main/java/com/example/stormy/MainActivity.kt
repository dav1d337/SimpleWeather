package com.example.stormy

import android.annotation.SuppressLint
import android.content.Context
import android.databinding.DataBindingUtil
import android.location.Location
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.stormy.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import android.location.Geocoder
import java.util.*


class MainActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private var binding: ActivityMainBinding? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    public var locationAsCity: String = "DefaultCity"

    private var latitudeHard: Double? = 49.59099
    private var longitudeHard: Double? = 11.00783
    private val apiKey = "32c8d57464870b4b153d614ae0c38e22"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getForecast(latitudeHard!!, longitudeHard!!)

    }


    @SuppressLint("MissingPermission")
    private fun getLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->

                var latitude = location!!.latitude
                var longitude = location!!.longitude
                println("Test: $latitude and $longitude")


                if (latitude != null && longitude != null) {
                    getForecast(latitude, longitude)
                }
            }

    }


    private fun getForecast(latitude: Double, longitude: Double) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        var darkSky = findViewById<TextView>(R.id.darkSkyAttribution)
        darkSky.movementMethod = LinkMovementMethod.getInstance()

        var forecastUrl = "https://api.darksky.net/forecast/" + apiKey + "/" + latitude + "," + longitude

        if (isNetworkAvailable()) {
            getDarkskyResponse(forecastUrl)
        }
    }

    private fun longLatToCity(latitude: Double, longitude: Double): String {
        val gcd = Geocoder(this, Locale.getDefault())
        val addresses = gcd.getFromLocation(latitude, longitude, 1)
        println(addresses)
        if (addresses.size > 0) {
            return addresses[0].locality
        } else {
            return "DefaultCity"
        }
    }

    private fun isNetworkAvailable(): Boolean {
        var manager: ConnectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var networkInfo = manager.activeNetworkInfo

        if (networkInfo != null && networkInfo.isConnected) {
            return true
        }

        Toast.makeText(this, getString(R.string.network_unavailable_message), Toast.LENGTH_LONG).show()
        return false
    }

    private fun getDarkskyResponse(url: String) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                try {
                    val jsonData = response.body()?.string()
                    println(jsonData)
                    val currentWeather = getCurrentDetails(jsonData)
                    binding!!.weather = currentWeather

                    runOnUiThread(Runnable {
                        var drawable = resources.getDrawable(currentWeather!!.getIconID())
                        iconImageView.setImageDrawable(drawable)
                    })
                } catch (e: IOException) {
                    println("!! IO EXCEPTION !! what to do?")
                } catch (e: JSONException) {
                    println("!! JSON EXCEPTION !! what to do?")
                }
            }
        })
    }

    private fun getCurrentDetails(jsonData: String?): CurrentWeather? {
        val forecast = JSONObject(jsonData)
        val currently = forecast.getJSONObject("currently")
        val humidity = currently.getDouble("humidity")
        val icon = currently.getString("icon")
        val locationLabel = longLatToCity(forecast.getDouble("latitude"), forecast.getDouble("longitude"))
        val time = currently.getLong("time")
        val precipChance = currently.getDouble("precipProbability")
        val summary = currently.getString("summary")
        val temperature = (currently.getDouble("temperature") - 32) / 1.8
        val timeZone = forecast.getString("timezone")
        return CurrentWeather(locationLabel, icon, time, temperature, humidity, precipChance, summary, timeZone)
    }

    fun refreshOnClick(view: View) {
        getLocation()

        Toast.makeText(this, "Weather updated!", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 100
    }
}