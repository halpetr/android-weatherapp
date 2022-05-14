package fi.tuni.tamk.weatherapp

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.fasterxml.jackson.databind.ObjectMapper
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    lateinit var weatherData : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        fetchWeatherAsync(this, "https://api.openweathermap.org/data/2.5/weather?q=Tampere&appid=a287e2a5822a417191893749dedd8978&units=metric") {
            println(it)
        }
    }

    private fun fetchWeatherAsync(context : Activity, url : String, callback: (rs : Weather) -> Unit) {
        thread {
            var resonsebody = processUrl(url)
            val weather: Weather = resonsebody
            Log.d("String", weather.weather.toString())
            val weatherString = weather.weather.toString()
            context.runOnUiThread() {
                weatherData.text = weatherString
            }
            callback(resonsebody)
        }
    }

    private fun processUrl(url : String) : Weather {
        val URI = URL(url)
        val conn: HttpURLConnection = URI.openConnection() as HttpURLConnection
        try {
            val mp = ObjectMapper()
            return mp.readValue(conn.inputStream, Weather::class.java)
        } finally {
            conn.disconnect()
        }
    }
}