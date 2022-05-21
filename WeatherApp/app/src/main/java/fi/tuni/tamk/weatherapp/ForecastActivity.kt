package fi.tuni.tamk.weatherapp

import android.content.Intent
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fasterxml.jackson.databind.ObjectMapper
import fi.tuni.tamk.weatherapp.forecast_weather_data.ForecastDataObject
import fi.tuni.tamk.weatherapp.forecast_weather_data.ForecastItem
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread


/**
 * Forecast displaying activity
 *
 * @constructor Create empty Forecast activity
 */
class ForecastActivity : AppCompatActivity() {

    // String that stores city name as string:
    lateinit var city: String

    // TextView for the Header containing city name:
    lateinit var headerView: TextView

    // Location that contains the location for intent.extras:
    lateinit var location: Location

    // String url that contains the string used to get data from API
    lateinit var url: String

    // Recycler view:
    private lateinit var recyclerView: RecyclerView

    // Forecast recyclerView adapter:
    private lateinit var forecastRecyclerAdapter: ForecastRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forecast)
        headerView = findViewById(R.id.headerText)
        headerView.setTextColor(Color.WHITE)
        parseIntent(intent)
        setForecastData(url) {
            initRecyclerView(it)
        }
        this.title = "WeatherApp Forecast"

    }

    /**
     * Initialize the RecyclerView.
     * Apply an Adapter and a LinearLayoutManager to RecyclerView.
     * Pass List of ForecastItems to the ForecastRecyclerAdapter.
     *
     * @param ls List of ForecastItems to be displayed in the RecyclerView
     */
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

    /**
     * Parse the received intent based on which extra was received.
     *
     * @param intent
     */
    private fun parseIntent(intent: Intent) {
        if (intent.extras?.get("city") != null) {
            city = intent.extras?.get("city") as String
            url =
                "https://api.openweathermap.org/data/2.5/forecast?q=${city}&appid=a287e2a5822a417191893749dedd8978&units=metric&cnt=17"
        } else {
            location = intent.extras?.get("coords") as Location
            url =
                "https://api.openweathermap.org/data/2.5/forecast?lon=${location.longitude}&lat=${location.latitude}&appid=a287e2a5822a417191893749dedd8978&units=metric&cnt=17"
        }
    }

    /**
     * Function used to get the forecast data from the API.
     * Passes the list of forecasts through the callback to be used to create RecyclerView ForecastRecyclerAdapter.
     *
     * @param url the url string used by fetchForecastAsync function to retrieve forecast data from the API
     * @param callback callback function that is used to pass data as a ForecastDataObject
     * @receiver
     */
    private fun setForecastData(url: String, callback: (ls: List<ForecastItem>) -> Unit) {
        fetchForecastAsync(url) { forecastObject ->
            if (forecastObject != null) {
                runOnUiThread {
                    headerView.text = forecastObject.city?.name
                }
                val list = forecastObject.list as List<ForecastItem>
                callback(list)
            }
        }
    }

    /**
     * Fetch forecast data asynchronously.
     *
     * @param url the url string used by processUrl function to retrieve forecast data from the API
     * @param callback callback function that is used to pass data as a ForecastDataObject
     * @receiver
     */
    private fun fetchForecastAsync(
        url: String,
        callback: (rs: ForecastDataObject?) -> Unit
    ) {
        var response: ForecastDataObject? = null
        thread {
            val weather: ForecastDataObject? = processUrl(url)
            if (weather != null) {
                response = weather
            }
            callback(response)
        }
    }

    /**
     * Create a ForecastDataObject based on the url string.
     *
     * @param url the url string that is used to retrieve forecast data from the API
     * @return ForecastDataObject containing the data
     */
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
// End of File