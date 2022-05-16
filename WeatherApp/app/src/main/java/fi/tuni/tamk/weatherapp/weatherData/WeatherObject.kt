package fi.tuni.tamk.weatherapp.weatherData

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class WeatherObject(
    var coord: Coord? = null,
    var weather: MutableList<Weather>? = null,
    var main: Temp? = null,
    var wind: Wind? = null,
    var dt: Int = 0,
    var cod: Int = 0,
    var name: String = ""
)