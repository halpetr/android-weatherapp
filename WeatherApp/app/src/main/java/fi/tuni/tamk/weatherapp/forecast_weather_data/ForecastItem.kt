package fi.tuni.tamk.weatherapp.forecast_weather_data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import fi.tuni.tamk.weatherapp.weather_data_common.Weather
import fi.tuni.tamk.weatherapp.weather_data_common.Wind

/**
 * Forecast item data class
 *
 * @property dt Long that has the Unix timestamp of the time when the weather was logged
 * @property main Temp object containing the temperature values
 * @property weather MutableList of Weather objects
 * @property wind Wind object containing the values for wind
 * @constructor Create empty Forecast item
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ForecastItem(
    var dt: Long? = null,
    var main: Temp? = null,
    var weather: MutableList<Weather>? = null,
    var wind: Wind? = null
)
