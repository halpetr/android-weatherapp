package fi.tuni.tamk.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.telephony.TelephonyManager
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.textfield.TextInputEditText
import com.squareup.picasso.Picasso
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import fi.tuni.tamk.weatherapp.WeatherData.WeatherObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


/*
* Main class of the application.*/
class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    // Variables:
    private val PERMISSION_LOCATION_REQUEST_CODE = 1

    // Textview for displaying weather data as text:
    private lateinit var weatherData: TextView

    // String variable for storing system language:
    lateinit var sDefSystemLanguage: String

    // String variable for storing country based on
    // phone network user is connected to:
    lateinit var countryCodeValue: String

    // TextView for displaying the of the city:
    lateinit var cityName: TextView

    // ImageView for displaying the weather icon:
    lateinit var iconView: ImageView

    // Variable used to check if the SettingsDialog was created already or not && to store said Dialog:
    var settingsDialog: SettingsDialog? = null

    // TextInputEditText for city name:
    lateinit var searchText: TextInputEditText

    // TextView for latitude:
    lateinit var lat: TextView

    // TextView for longitude:
    lateinit var lon: TextView

    // Button for searching by city:
    lateinit var searchButton: Button

    // Url that is used to fetch data with
    private var myURL: String = ""

    // LocationClient for accessing location info:
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var devLatitude: Double? = null
    private var devLongitude: Double? = null

    protected var mLastLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Initialize Views and variables:
        initElements()
        setOnClickListeners()
        getLocation()
    }

    private val cancellationTokenSource = CancellationTokenSource()

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        // Not actually missing any permissions:
        if (hasLocationPermission()) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener {
                if (it != null) {
                    val lt = "Latitude: " + it.latitude.toString().substring(0, 6)
                    val ln = "Longitude: " + it.longitude.toString().substring(0, 6)
                    lat.text = lt
                    lon.text = ln
                    devLatitude = it.latitude
                    devLongitude = it.longitude
                    Log.d("TAG", it.latitude.toString())
                    Log.d("TAG", it.longitude.toString())
                    getData("https://api.openweathermap.org/data/2.5/weather?lat=${it.latitude}&lon=${it.longitude}&appid=a287e2a5822a417191893749dedd8978&units=metric")
                    Log.d("TAG", "https://api.openweathermap.org/data/2.5/weather?lat=${it.latitude}&lon=${it.longitude}&appid=a287e2a5822a417191893749dedd8978&units=metric")
                }
            }
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            requestLocationPermission()
        }
    }

    override fun onResume() {
        super.onResume()
        if (devLatitude == null && devLongitude == null) {
            getLocation()
        }
    }

    override fun onPause() {
        super.onPause()
        cancellationTokenSource.cancel()
    }

    // Initialize variables and views:
    private fun initElements() {
        weatherData = findViewById(R.id.weatherData)
        cityName = findViewById(R.id.headerText)
        cityName.setTextColor(Color.WHITE)
        iconView = findViewById(R.id.icon)
        searchText = findViewById(R.id.cityInput)
        searchButton = findViewById(R.id.searchBtn)
        lat = findViewById(R.id.lat)
        lon = findViewById(R.id.lon)
        // Set locale and country:
        sDefSystemLanguage = Locale.getDefault().language
        val tm = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        countryCodeValue = tm.networkCountryIso
    }

    private fun setOnClickListeners() {
        searchButton.setOnClickListener {
            myURL =
                "https://api.openweathermap.org/data/2.5/weather?q=${searchText.text.toString()}&appid=a287e2a5822a417191893749dedd8978&units=metric"
            getData(myURL)
        }


    }

    private fun getData(url: String) {
        // If the app has permission to use location info then fetch data, else request the permission:
        if (hasLocationPermission()) {
            fetchWeatherAsync(
                this,
                url
            ) {
                if (!it) {
                    this.runOnUiThread {
                        Toast.makeText(
                            applicationContext,
                            "Not a valid city!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } else {
            requestLocationPermission()
        }
        // Hide keyboard when button is pressed:
        try {
            val imm: InputMethodManager =
                getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        } catch (e: Exception) {
        }
    }

    private fun fetchWeatherAsync(
        context: Activity,
        url: String,
        callback: (rs: Boolean) -> Unit
    ) {
        var isValid = false
        thread {
            val responsebody = processUrl(url)
            val weather: WeatherObject? = responsebody
            if (weather != null) {
                isValid = true
                // Set Date:
                val weatherNow =
                    "${setDate(weather.dt)}\n${weather.weather?.get(0)?.main.toString()}\n${
                        weather.weather?.get(0)?.description.toString()
                    }" +
                            "\n${weather.main?.temp.toString()} °C\n${weather.wind?.speed.toString()} m/s"
                val lt = "Latitude: " + weather.coord?.lat.toString().substring(0, 6)
                val ln = "Longitude: " + weather.coord?.lon.toString().substring(0, 6)
                context.runOnUiThread() {
                    weatherData.text = weatherNow
                    cityName.text = weather.name
                    lat.text = lt
                    lon.text = ln
                    Picasso.get()
                        .load("http://openweathermap.org/img/w/${weather.weather?.get(0)?.icon}.png")
                        .into(iconView)
                }
            }
            callback(isValid)
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

    private fun processUrl(url: String): WeatherObject? {
        val URI = URL(url)
        val conn: HttpURLConnection = URI.openConnection() as HttpURLConnection
        if (conn.responseCode == 200) {
            try {
                val mp = ObjectMapper()
                return mp.readValue(conn.inputStream, WeatherObject::class.java)
            } finally {
                conn.disconnect()
            }
        }
        return null
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

}
