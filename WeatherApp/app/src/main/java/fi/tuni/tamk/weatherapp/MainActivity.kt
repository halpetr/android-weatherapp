package fi.tuni.tamk.weatherapp

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.fasterxml.jackson.databind.ObjectMapper
import com.squareup.picasso.Picasso
import fi.tuni.tamk.weatherapp.weatherData.WeatherObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {private lateinit var weatherData : TextView
    lateinit var cityName : TextView
    lateinit var iconUrl : String
    lateinit var iconView : ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        weatherData = findViewById(R.id.weatherData)
        cityName = findViewById(R.id.headerText)
        cityName.setTextColor(Color.WHITE)
        iconView = findViewById<ImageView>(R.id.icon)
        val mainLayout = findViewById<View>(R.id.mainLayout)
        mainLayout.setBackgroundColor(Color.parseColor("#76381A"))
    }

    override fun onResume() {
        super.onResume()
        fetchWeatherAsync(this, "https://api.openweathermap.org/data/2.5/weather?q=Tampere&appid=a287e2a5822a417191893749dedd8978&units=metric") {
            println(it)
        }
    }

    private fun fetchWeatherAsync(context : Activity, url : String, callback: (rs : WeatherObject) -> Unit) {
        thread {
            val responsebody = processUrl(url)
            val weather: WeatherObject = responsebody
            val weatherNow = "${weather.weather?.get(0)?.main.toString()}\n${weather.weather?.get(0)?.description.toString()}" +
                    "\n${weather.main?.temp.toString()} °C\n${weather.wind?.speed.toString()} m/s"
            iconUrl = "http://openweathermap.org/img/w/${weather.weather?.get(0)?.icon}.png"
            context.runOnUiThread() {
                weatherData.text = weatherNow
                cityName.text = weather.name
                Picasso.get().load(iconUrl).into(iconView)
            }
            callback(responsebody)
        }
    }

    private fun processUrl(url : String) : WeatherObject {
        val URI = URL(url)
        val conn: HttpURLConnection = URI.openConnection() as HttpURLConnection
        try {
            val mp = ObjectMapper()
            return mp.readValue(conn.inputStream, WeatherObject::class.java)
        } finally {
            conn.disconnect()
        }
    }
}
