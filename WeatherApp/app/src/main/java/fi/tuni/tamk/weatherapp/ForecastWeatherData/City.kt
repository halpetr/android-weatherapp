package fi.tuni.tamk.weatherapp.ForecastWeatherData

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import fi.tuni.tamk.weatherapp.WeatherData_common.Coord

@JsonIgnoreProperties(ignoreUnknown = true)
data class City(var name: String? = null, var coord: Coord? = null)
