package fi.tuni.tamk.weatherapp

import android.Manifest
import android.annotation.SuppressLint
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
import android.widget.*
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

// Location Intervals:
private const val INTERVAL_NORMAL: Long = 30
private const val FAST_INTERVAL: Long = 5

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
    private var settingsDialog: SettingsDialog? = null

    // TextInputEditText for city name:
    lateinit var searchText: TextInputEditText

    // TextView for latitude:
    lateinit var lat: TextView

    // TextView for longitude:
    lateinit var lon: TextView

    // Button for searching by city:
    lateinit var searchButton: Button

    // Button for getting weather at gps location:
    private lateinit var getByLocationBtn: Button

    // Url that is used to fetch data with
    private lateinit var myURL: String

    // "Location" textview
    lateinit var locationTextView: TextView

    // String used to indicate what location data is being displayed
    private lateinit var locationString: String

    // Google API LocationClient for accessing location info:
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // LocationCallback:
    private lateinit var locationCallback: LocationCallback

    // LastLocation from LocationCallback:
    private lateinit var latestLocation: Location

    // LocationRequest config for settings:
    private var locationRequest = LocationRequest.create()

    private val cancellationTokenSource = CancellationTokenSource()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Initialize Views and variables:
        initElements()
        setOnClickListeners()
        getGPSWeather(null)
    }

    override fun onPause() {
        super.onPause()
        cancellationTokenSource.cancel()
    }

    // Initialize variables and views:
    private fun initElements() {
        setLocationVariables()
        weatherData = findViewById(R.id.weatherData)
        cityName = findViewById(R.id.headerText)
        cityName.setTextColor(Color.WHITE)
        iconView = findViewById(R.id.icon)
        searchText = findViewById(R.id.cityInput)
        searchButton = findViewById(R.id.searchBtn)
        getByLocationBtn = findViewById(R.id.gpsButton)
        locationTextView = findViewById(R.id.locationHeader)
        lat = findViewById(R.id.lat)
        lon = findViewById(R.id.lon)
        // Update location in interval if permission granted:
        if(hasLocationPermission()) {
            startGPSUpdate()
        } else {
            stopGPSUpdate()
        }
        // Set locale and country:
        sDefSystemLanguage = Locale.getDefault().language
        val tm = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        countryCodeValue = tm.networkCountryIso
    }

    private fun setLocationVariables() {
        // set LocationRequest properties:
        locationRequest.interval = 1000 * INTERVAL_NORMAL
        locationRequest.fastestInterval = 1000 * FAST_INTERVAL
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        // initialize FusedLocationClient:
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // LocationCallback:
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                latestLocation = p0.lastLocation
                Log.d("TAG", "location updated")
            }
        }
    }

    private fun setOnClickListeners() {
        searchButton.setOnClickListener {
            myURL =
                "https://api.openweathermap.org/data/2.5/weather?q=${searchText.text.toString()}&appid=a287e2a5822a417191893749dedd8978&units=metric"
            getSearchWeather(myURL) {
                updateUIValues(it)
                val st = it?.name.toString() + " location:"
                locationTextView.text = st
            }

        }

        getByLocationBtn.setOnClickListener {
            if (hasLocationPermission()) {
                getGPSWeather(latestLocation)
            } else {
                requestLocationPermission()
            }

        }

    }

    private fun updateUIValues(weather: WeatherObject?) {
        if (weather != null) {
            this.runOnUiThread() {
                val weatherNow =
                    "${setDate(weather.dt)}\n${weather.weather?.get(0)?.main.toString()}\n${
                        weather.weather?.get(0)?.description.toString()
                    }" +
                            "\n${weather.main?.temp.toString()} °C\n${weather.wind?.speed.toString()} m/s"
                val lt = "Latitude: " + weather.coord?.lat.toString().substring(0, 6)
                val ln = "Longitude: " + weather.coord?.lon.toString().substring(0, 6)
                    weatherData.text = weatherNow
                    cityName.text = weather.name
                    lat.text = lt
                    lon.text = ln
                    Picasso.get()
                        .load("http://openweathermap.org/img/w/${weather.weather?.get(0)?.icon}.png")
                        .into(iconView)
                }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startGPSUpdate() {
        if (hasLocationPermission()) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } else {
            requestLocationPermission()
        }
    }

    private fun stopGPSUpdate() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d("TAG", "update stopped")
    }

    @SuppressLint("MissingPermission")
    private fun getGPSWeather(loc: Location?) {
        if (hasLocationPermission()) {
            if(loc == null) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    myURL = "https://api.openweathermap.org/data/2.5/weather?lat=${location.latitude}&lon=${location.longitude}&appid=a287e2a5822a417191893749dedd8978&units=metric"
                    fetchWeatherAsync(myURL) { weatherObject ->
                        updateUIValues(weatherObject)
                    }
                }
            }
            else {
                myURL = "https://api.openweathermap.org/data/2.5/weather?lat=${loc.latitude}&lon=${loc.longitude}&appid=a287e2a5822a417191893749dedd8978&units=metric"
                fetchWeatherAsync(myURL) { weatherObject ->
                    updateUIValues(weatherObject)
                }
            }
        }
        else {
            requestLocationPermission()
        }
    }

    private fun getSearchWeather(url: String, callback: (rs: WeatherObject?) -> Unit) {
        fetchWeatherAsync(
            url
        ) {
            println(it)
            if (it == null) {
                this.runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "Not a valid city!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else {
                callback(it)
            }
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
        url: String,
        callback: (rs: WeatherObject?) -> Unit
    ) {
        var response: WeatherObject? = null
        thread {
            val weather: WeatherObject? = processUrl(url)
            Log.d("fetchWeatherAsync", weather.toString())
            if (weather != null) {
                response = weather
            }
            callback(response)
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

    /* Pass parameters to EasyPermissions library to handle permissions */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Toast.makeText(applicationContext, "Permission Granted!", Toast.LENGTH_SHORT).show()
        getGPSWeather(null)
    }

// If user denies permission:
    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        // If it is permanently denied ("don't show this again") is selected.
        // Then need to show app settings because it will not work without permission:
    //TODO: App crashes if settings are changed and user returns to APP
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            if (settingsDialog == null) {
                settingsDialog = SettingsDialog.Builder(this).build()
                settingsDialog?.show()
            }
        } else {
            requestLocationPermission()
        }
    }

    // If user changes language from device settings, update language:
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        sDefSystemLanguage = newConfig.locales.get(0).language
    }

}