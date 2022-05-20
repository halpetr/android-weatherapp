package fi.tuni.tamk.weatherapp

import android.content.Intent
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fasterxml.jackson.databind.ObjectMapper
import com.squareup.picasso.Picasso
import fi.tuni.tamk.weatherapp.ForecastWeatherData.ForecastDataObject
import fi.tuni.tamk.weatherapp.ForecastWeatherData.ForecastItem
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


class ForecastActivity : AppCompatActivity() {

    //
    lateinit var city: String

    lateinit var headerView: TextView

    lateinit var location: Location

    lateinit var url: String

    // Recycler view:
    private lateinit var recyclerView: RecyclerView

    // Forecast recycler adapter:
    private lateinit var forecastRecyclerAdapter: ForecastRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forecast)
        headerView = findViewById(R.id.headerText)
        headerView.setTextColor(Color.WHITE)
        setupIntent(intent)
        setForecastData(url) {
            initRecyclerView(it)
        }
        this.title = "WeatherApp Forecast"

    }

    private fun initRecyclerView(ls: List<ForecastItem>) {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.apply {
            runOnUiThread {
                forecastRecyclerAdapter = ForecastRecyclerAdapter(ls)
                this.adapter = forecastRecyclerAdapter
                this.layoutManager = LinearLayoutManager(this@ForecastActivity)
            }
        }
    }

    private fun setupIntent(intent: Intent) {
        if(intent.extras?.get("city") != null) {
            city = intent.extras?.get("city") as String
            url = "https://api.openweathermap.org/data/2.5/forecast?q=${city}&appid=a287e2a5822a417191893749dedd8978&units=metric&cnt=17"
        } else {
            location = intent.extras?.get("coords") as Location
            url = "https://api.openweathermap.org/data/2.5/forecast?lon=${location.longitude}&lat=${location.latitude}&appid=a287e2a5822a417191893749dedd8978&units=metric&cnt=17"
        }
    }

    private fun setForecastData(url: String, callback: (ls: List<ForecastItem>) -> Unit) {
        fetchForecastAsync(url) { forecastObject ->
            if(forecastObject != null) {
                runOnUiThread {
                    headerView.text = forecastObject.city?.name
                }
                val list = forecastObject.list as List<ForecastItem>
                callback(list)
                Log.d("LIST", list.toString())
            }
        }
    }

    private fun fetchForecastAsync(
        url: String,
        callback: (rs: ForecastDataObject?) -> Unit
    ) {
        var response: ForecastDataObject? = null
        thread {
            val weather: ForecastDataObject? = processUrl(url)
            Log.d("fetchWeatherAsync", weather.toString())
            if (weather != null) {
                response = weather
            }
            callback(response)
        }
    }

    private fun processUrl(url: String): ForecastDataObject? {
        val URI = URL(url)
        val conn: HttpURLConnection = URI.openConnection() as HttpURLConnection
        if (conn.responseCode == 200) {
            try {
                val mp = ObjectMapper()
                return mp.readValue(conn.inputStream, ForecastDataObject::class.java)
            } finally {
                conn.disconnect()
            }
        }
        return null
    }

}