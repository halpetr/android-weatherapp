package fi.tuni.tamk.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.gms.location.*
import com.google.android.material.textfield.TextInputEditText
import com.squareup.picasso.Picasso
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import fi.tuni.tamk.weatherapp.current_weather_data.WeatherObject
import fi.tuni.tamk.weatherapp.R.id.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

// Location Interval requesting constants:
private const val INTERVAL_NORMAL: Long = 30
private const val FAST_INTERVAL: Long = 5

// Arbitrary constant integer used as permission request code for location.
// Could be any integer e.g. 99
private const val PERMISSION_LOCATION_REQUEST_CODE = 1

/**
 * Main activity
 * Implements EasyPermissions.PermissionCallbacks
 * @constructor Create empty Main activity
 */
class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    // Textview for displaying weather-data date:
    private lateinit var weatherDate: TextView

    // Textview for displaying weather-data conditions/description:
    private lateinit var weatherConditions: TextView

    // Textview for displaying weather-data temperature:
    private lateinit var weatherTemp: TextView

    // Textview for displaying weather-data wind speed:
    private lateinit var weatherWind: TextView

    // String variable for storing system language:
    private lateinit var sDefSystemLanguage: String

    // String variable for storing country based on
    // phone network user is connected to:
    private lateinit var countryCodeValue: String

    // TextView for displaying the name of the city:
    private lateinit var cityName: TextView

    // ImageView for displaying the weather icon:
    private lateinit var iconView: ImageView

    // Variable used to check if the SettingsDialog was created already or not && to store said Dialog:
    private var settingsDialog: SettingsDialog? = null

    // TextInputEditText for search:
    private lateinit var searchText: TextInputEditText

    // TextView for displaying latitude:
    private lateinit var lat: TextView

    // TextView for displaying longitude:
    private lateinit var lon: TextView

    // Button for searching by city that was :
    private lateinit var searchButton: Button

    // Button for getting weather at device location:
    private lateinit var getByLocationBtn: Button

    // Button for accessing starting forecast activity:
    private lateinit var forecastButton: Button

    // Url that is used to fetch data:
    private lateinit var myURL: String

    // Textview for displaying location data:
    private lateinit var locationTextView: TextView

    // Boolean for checking if user has searched for a city or used GPS to get weather data.
    // Used to determine which extra is added to intent: city name or coordinates:
    private var searched: Boolean = false

    // Google API LocationClient for accessing location info:
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // LocationCallback:
    private lateinit var locationCallback: LocationCallback

    // Last device location from LocationCallback, updated on app startup and every 30 seconds after that:
    private var latestLocation: Location? = null

    // LocationRequest config for settings:
    private var locationRequest = LocationRequest.create()

    /**
     * Function which is called when creating the activity:
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Set title of the activity:
        this.title = "WeatherApp Current Weather"
        // Initialize Views and variables:
        initElements()
        setOnClickListeners()
        // Update location immediately and after in interval of 30 sec:
        startGPSUpdate()
        // Get current weather based on gps location of the device:
        getGPSWeather()
    }

    override fun onResume() {
        super.onResume()
        initElements()
        // Get current weather based on gps location of the device:
        getGPSWeather()
    }

    /**
     * Initialize all elements/views and variables of the activity.
     * Start GPS location updates.
     * Get device system language and device country from network.
     */
    private fun initElements() {
        setLocationVariables()
        initWeatherDisplayViews()
        cityName = findViewById(headerText)
        cityName.setTextColor(Color.WHITE)
        iconView = findViewById(icon)
        searchText = findViewById(cityInput)
        searchButton = findViewById(searchBtn)
        getByLocationBtn = findViewById(gpsButton)
        forecastButton = findViewById(R.id.forecastButton)
        locationTextView = findViewById(locationHeader)
        lat = findViewById(R.id.lat)
        lon = findViewById(R.id.lon)
        // Set locale and country:
        sDefSystemLanguage = Locale.getDefault().language
        val tm = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        countryCodeValue = tm.networkCountryIso
    }

    /**
     * Init weather display views
     *
     */
    private fun initWeatherDisplayViews() {
        weatherDate = findViewById(date)
        weatherConditions = findViewById(weather_conditions)
        weatherTemp = findViewById(temp)
        weatherWind = findViewById(wind)
    }

    /**
     * Set location variables
     * Initialize FusedLocationProviderClient.
     */
    private fun setLocationVariables() {
        // set LocationRequest settings:
        locationRequest.interval = 1000 * INTERVAL_NORMAL
        locationRequest.fastestInterval = 1000 * FAST_INTERVAL
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        // initialize FusedLocationClient:
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    /**
     * Set onClickListeners for all 3 buttons:
     *
     */
    private fun setOnClickListeners() {
        // Get weather data and update UI values based on city that was searched for:
        searchButton.setOnClickListener {
            myURL =
                "https://api.openweathermap.org/data/2.5/weather?q=${searchText.text.toString()}&appid=a287e2a5822a417191893749dedd8978&units=metric"
            getSearchWeather(myURL) {
                updateUIValues(it)
                searched = true
                // Set location text to be "'City name' location:"
                val st = it?.name.toString() + " location:"
                locationTextView.text = st
            }

        }

        // Get weather data based on latestLocation. UI values are updated in getGPSWeather function:
        getByLocationBtn.setOnClickListener {
            getGPSWeather()
            searched = false
            // Set location text to be "My location:"
            val st = "My location:"
            locationTextView.text = st
        }

        // Start forecast activity. putExtra the intent based on the boolean searched:
        forecastButton.setOnClickListener {
            val intent = Intent(this, ForecastActivity::class.java)
            if (searched) {
                intent.putExtra("city", searchText.text.toString())
            } else {
                if(latestLocation != null) {
                    intent.putExtra("lat", latestLocation?.latitude?.toString())
                    intent.putExtra("lon", latestLocation?.longitude?.toString())
                } else {
                    intent.putExtra("city", cityName.text.toString())
                }
            }
            startActivity(intent)
        }
    }

    /**
     * Update User Interface values
     *
     * @param weather WeatherObject that contains the current weather information:
     */
    private fun updateUIValues(weather: WeatherObject?) {
        if (weather != null) {
            val time = setDate(weather.dt)
            val conditions = "${weather.weather?.get(0)?.main.toString()}\n${
                weather.weather?.get(0)?.description.toString()
            }"
            val temp = "${weather.main?.temp?.toString()} °C"
            val wind = "${weather.wind?.speed?.toString()} m/s"
            // Make latitude and logitude look better:
            val lt = if(weather.coord?.lat.toString().length < 6) {
                weather.coord?.lat.toString().substring(0, weather.coord?.lat.toString().length)
            } else {
                weather.coord?.lat.toString().substring(0, 6)
            }
            val ln = if(weather.coord?.lon.toString().length < 6) {
                weather.coord?.lon.toString().substring(0, weather.coord?.lon.toString().length)
            } else {
                weather.coord?.lon.toString().substring(0, 6)
            }
            // Run value updates on Ui thread
            this.runOnUiThread() {
                weatherDate.text = time
                weatherConditions.text = conditions
                weatherTemp.text = temp
                weatherWind.text = wind
                cityName.text = weather.name
                lat.text = lt
                lon.text = ln
                // Use Picasso to load the weather icon into the ImageView:
                Picasso.get()
                    .load("https://openweathermap.org/img/w/${weather.weather?.get(0)?.icon}.png")
                    .into(iconView)
                // Make forecast button visible when latestLocation has been set:
                forecastButton.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Function for starting GPS location updates by requesting them from LocationServices.FusedLocationProviderClient.
     * Not actually missing any permissions. Lint does not recognize EasyPermissions permission.
     * Check if app has location permission, initialize LocationCallback and start requesting location updates.
     * If permission not granted then request location permission.
     *
     */
    @SuppressLint("MissingPermission")
    private fun startGPSUpdate() {
        if (hasLocationPermission()) {
            // LocationCallback:
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult) {
                    super.onLocationResult(p0)
                    // Save last location in latestLocation variable:
                    latestLocation = p0.lastLocation
                }
            }
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            requestLocationPermission()
        }
    }

    /**
     * Function for getting current weather based on users gps location.
     * Not actually missing any permissions. Lint does not recognize EasyPermissions permission.
     *
     */
    @SuppressLint("MissingPermission")
    private fun getGPSWeather() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            myURL =
                "https://api.openweathermap.org/data/2.5/weather?lat=${location.latitude}&lon=${location.longitude}&appid=a287e2a5822a417191893749dedd8978&units=metric"
            fetchWeatherAsync(myURL) { weatherObject ->
                updateUIValues(weatherObject)
            }
        }
    }

    /**
     * Get weather data based on search result. If search word was not a city
     * the resulting WeatherObject is null, in which case show a Toast.
     *
     * @param url the url string used by fetchWeatherAsync function to retrieve weather data from the API
     * @param callback callback function that is used to pass data (WeatherObject)
     * @receiver
     */
    private fun getSearchWeather(url: String, callback: (rs: WeatherObject?) -> Unit) {
        fetchWeatherAsync(
            url
        ) {
            if (it == null) {
                this.runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "Not a valid city!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                callback(it)
            }
        }
        // Collapse keyboard when button is pressed:
        try {
            val imm: InputMethodManager =
                getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        } catch (e: Exception) {
        }
    }

    /**
     * Fetch weather data asynchronously.
     *
     * @param url the url string used by processUrl function to retrieve weather data from the API
     * @param callback callback function that is used to pass data as a WeatherObject
     * @receiver
     */
    private fun fetchWeatherAsync(
        url: String,
        callback: (rs: WeatherObject?) -> Unit
    ) {
        var response: WeatherObject? = null
        thread {
            val weather: WeatherObject? = processUrl(url)
            if (weather != null) {
                response = weather
            }
            callback(response)
        }
    }

    /**
     * Transform the Unix timestamp from the data into Date format:
     *
     * @param timestamp unix timestamp from the weather data
     * @return string containing the current date and time
     */
    private fun setDate(timestamp: Long): String {
        val sdf = SimpleDateFormat(
            "dd.MM.yy, HH:mm",
            Locale(sDefSystemLanguage, countryCodeValue.uppercase())
        )
        val date = Date(timestamp * 1000)
        return sdf.format(date)
    }

    /**
     * Create a WeatherObject based on the url string.
     *
     * @param url the url string that is used to retrieve weather data from the API
     * @return WeatherObject containing the data
     */
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

    /**
     * Function used to check if app has permission to use device location.
     *
     */
    private fun hasLocationPermission() =
        EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)

    /**
     * Function used to request location permission.
     * A message is shown to the user explaining what permission the app needs to work.
     *
     */
    private fun requestLocationPermission() {
        EasyPermissions.requestPermissions(
            this, "Application requires Location Permission to work.",
            PERMISSION_LOCATION_REQUEST_CODE, Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    /**
     * Function called when a permission request result is achieved.
     * Used to pass parameters to EasyPermissions library for it to the handle permissions
     *
     * @param requestCode the code associated with the permission
     * @param permissions array of requested permissions
     * @param grantResults array of results for permissions
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    /**
     * Function called when a permission was granted.
     *
     * @param requestCode the code associated with the permission
     * @param perms list of the permission which were granted
     */
    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Toast.makeText(applicationContext, "Permission Granted!", Toast.LENGTH_SHORT).show()
        startGPSUpdate()
        getGPSWeather()
    }

    /**
     * Function called when a permission was denied.
     *
     * @param requestCode the code associated with the permission
     * @param perms list of the permission which were denied
     */
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

    /**
     * Function called if user changes language from device settings, update language:
     *
     * @param newConfig new Configuration that is updated when user changes device language
     */
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        sDefSystemLanguage = newConfig.locales.get(0).language
    }
}
// End of File