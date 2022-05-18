package fi.tuni.tamk.weatherapp.CurrentWeatherData

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import fi.tuni.tamk.weatherapp.WeatherData_common.Coord
import fi.tuni.tamk.weatherapp.WeatherData_common.Weather
import fi.tuni.tamk.weatherapp.WeatherData_common.Wind

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