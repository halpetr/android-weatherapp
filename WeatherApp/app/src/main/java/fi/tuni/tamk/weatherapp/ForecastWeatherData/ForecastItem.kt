package fi.tuni.tamk.weatherapp.ForecastWeatherData

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import fi.tuni.tamk.weatherapp.WeatherData_common.Weather
import fi.tuni.tamk.weatherapp.WeatherData_common.Wind

@JsonIgnoreProperties(ignoreUnknown = true)
data class ForecastItem(
    var dt: Long? = null,
    var main: Temp? = null,
    var weather: MutableList<Weather>? = null,
    var wind: Wind? = null
)
