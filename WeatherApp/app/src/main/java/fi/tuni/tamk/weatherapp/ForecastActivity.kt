package fi.tuni.tamk.weatherapp

import android.content.Intent
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.fasterxml.jackson.databind.ObjectMapper
import fi.tuni.tamk.weatherapp.ForecastWeatherData.ForecastDataObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


class ForecastActivity : AppCompatActivity() {

    lateinit var city: String

    lateinit var headerView: TextView

    lateinit var location: Location

    lateinit var url: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forecast)
        headerView = findViewById(R.id.headerText)
        headerView.setTextColor(Color.WHITE)
        setupIntent(intent)
    }

    private fun setupIntent(intent: Intent) {
        if(intent.extras?.get("city") != null) {
            city = intent.extras?.get("city") as String
            headerView.text = city
            url = "https://api.openweathermap.org/data/2.5/forecast?q=${city}&appid=a287e2a5822a417191893749dedd8978&units=metric&cnt=12"
        } else {
            location = intent.extras?.get("coords") as Location
            url = "https://api.openweathermap.org/data/2.5/forecast?lon=${location.longitude}&lat=${location.latitude}&appid=a287e2a5822a417191893749dedd8978&units=metric&cnt=12"
        }
        setForecastData(url)
    }

    private fun setForecastData(url: String) {
        fetchForecastAsync(url) { forecastObject ->
            if(forecastObject != null) {
                this.runOnUiThread {
                    headerView.text = forecastObject.city?.name.toString()
                }
            }
        }
    }

    private fun setDate(timestamp: Long): String {
        val sdf = SimpleDateFormat(
            "dd.MM.yy, HH:mm",
            Locale("fi","FI")
        )
        val date = Date(timestamp * 1000)
        return sdf.format(date)
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