package fi.tuni.tamk.weatherapp.current_weather_data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import fi.tuni.tamk.weatherapp.weather_data_common.Coord
import fi.tuni.tamk.weatherapp.weather_data_common.Weather
import fi.tuni.tamk.weatherapp.weather_data_common.Wind

/**
 * WeatherObject data class
 *
 * @property coord Coord object containing the location coordinates
 * @property weather MutableList of Weather objects
 * @property main Temp object containing the temperature values
 * @property wind Wind object containing the wind speed
 * @property dt Long that has the Unix timestamp of the time when the weather was logged
 * @property cod Int response code of the http request
 * @property name String name of the city
 * @constructor Create empty Weather object
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class WeatherObject(
    var coord: Coord? = null,
    var weather: MutableList<Weather>? = null,
    var main: Temp? = null,
    var wind: Wind? = null,
    var dt: Long = 0,
    var cod: Int = 0,
    var name: String = ""
)