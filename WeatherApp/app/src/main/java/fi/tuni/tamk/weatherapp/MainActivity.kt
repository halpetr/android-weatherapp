package fi.tuni.tamk.weatherapp

import android.Manifest
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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.fasterxml.jackson.databind.ObjectMapper
import com.squareup.picasso.Picasso
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import fi.tuni.tamk.weatherapp.weatherData.WeatherObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private val PERMISSION_LOCATION_REQUEST_CODE = 1
    private lateinit var weatherData: TextView
    lateinit var sDefSystemLanguage: String
    lateinit var countryCodeValue: String
    lateinit var cityName: TextView
    lateinit var iconUrl: String
    lateinit var iconView: ImageView
    lateinit var mainLayout: View
    var settingsDialog: SettingsDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Initialize Views and variables:
        initElements()
        mainLayout.setBackgroundColor(Color.parseColor("#76381A"))
    }

    override fun onResume() {
        super.onResume()
        if (hasLocationPermission()) {
            fetchWeatherAsync(
                this,
                "https://api.openweathermap.org/data/2.5/weather?q=Tampere&appid=a287e2a5822a417191893749dedd8978&units=metric"
            ) {
                println(it)
            }
        } else {
            requestLocationPermission()
        }
    }

    // Initialize variables and views:
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

    private fun hasLocationPermission() =
        EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)

    private fun requestLocationPermission() {
        EasyPermissions.requestPermissions(
            this, "Application requires Location Permission to work.",
            PERMISSION_LOCATION_REQUEST_CODE, Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        if (hasLocationPermission()) {
            Toast.makeText(applicationContext, "Permission Granted!", Toast.LENGTH_SHORT).show()
            println("IM HERE")
        }
    }

    // TODO: Possibly make app work without location permission but then ask for permission when a button is pressed.
    // If user denies permission:
    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        // If it is permanently denied ("don't show this again") is selected.
        // Then need to show app settings because it will not work without permission:
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            if (settingsDialog == null) {
                settingsDialog = SettingsDialog.Builder(this).build()
                settingsDialog?.show()
            }
        } else {
            requestLocationPermission()
        }
    }

    /* Pass parameters to EasyPermissions library to handle permissions */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    // If user changes language from device settings, update language:
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        sDefSystemLanguage = newConfig.locales.get(0).language
    }

    private fun fetchWeatherAsync(
        context: Activity,
        url: String,
        callback: (rs: WeatherObject) -> Unit
    ) {
        thread {
            val responsebody = processUrl(url)
            val weather: WeatherObject = responsebody
            // Set Date:
            val timestamp = weather.dt.toLong()
            val weatherNow = "${setDate(timestamp)}\n${weather.weather?.get(0)?.main.toString()}\n${
                weather.weather?.get(0)?.description.toString()
            }" +
                    "\n${weather.main?.temp.toString()} °C\n${weather.wind?.speed.toString()} m/s"
            iconUrl = "http://openweathermap.org/img/w/${weather.weather?.get(0)?.icon}.png"
            context.runOnUiThread() {
                weatherData.text = weatherNow
                cityName.text = weather.name
                Picasso.get()
                    .load("http://openweathermap.org/img/w/${weather.weather?.get(0)?.icon}.png")
                    .into(iconView)
            }
            callback(responsebody)
        }
    }

    private fun setDate(timestamp: Long): String {
        val sdf = SimpleDateFormat(
            "dd.MM.yy, HH:mm",
            Locale(sDefSystemLanguage, countryCodeValue.uppercase())
        )
        val date = Date(timestamp * 1000)
        return sdf.format(date)
    }

    private fun processUrl(url: String): WeatherObject {
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
