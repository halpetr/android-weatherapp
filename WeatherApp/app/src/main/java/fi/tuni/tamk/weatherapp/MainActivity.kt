package fi.tuni.tamk.weatherapp

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.fasterxml.jackson.databind.ObjectMapper
import com.squareup.picasso.Picasso
import fi.tuni.tamk.weatherapp.weatherData.WeatherObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    private lateinit var weatherData : TextView
    lateinit var sDefSystemLanguage: String
    lateinit var countryCodeValue : String
    lateinit var cityName : TextView
    lateinit var iconUrl : String
    lateinit var iconView : ImageView
    lateinit var mainLayout : View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Initialize Views and variables:
        initElements()
        mainLayout.setBackgroundColor(Color.parseColor("#76381A"))
    }

    override fun onResume() {
        super.onResume()
        fetchWeatherAsync(this, "https://api.openweathermap.org/data/2.5/weather?q=Tampere&appid=a287e2a5822a417191893749dedd8978&units=metric") {
            println(it)
        }
    }

    private fun initElements() {
        weatherData = findViewById(R.id.weatherData)
        cityName = findViewById(R.id.headerText)
        cityName.setTextColor(Color.WHITE)
        iconView = findViewById(R.id.icon)
        mainLayout = findViewById(R.id.mainLayout)
        // Set locale and country:
        sDefSystemLanguage = Locale.getDefault().language
        val tm = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        countryCodeValue = tm.networkCountryIso
    }

    // If user changes language from device settings, update language:
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        sDefSystemLanguage = newConfig.locales.get(0).language
    }

    private fun fetchWeatherAsync(context : Activity, url : String, callback: (rs : WeatherObject) -> Unit) {
        thread {
            val responsebody = processUrl(url)
            val weather: WeatherObject = responsebody
            // Set Date:
            val timestamp = weather.dt.toLong()
            val weatherNow = "${setDate(timestamp)}\n${weather.weather?.get(0)?.main.toString()}\n${weather.weather?.get(0)?.description.toString()}" +
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

    private fun setDate(timestamp : Long) : String {
        val sdf = SimpleDateFormat("dd.MM.yy, HH:mm", Locale(sDefSystemLanguage, countryCodeValue.uppercase()))
        val date = Date(timestamp * 1000)
        return sdf.format(date)
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
