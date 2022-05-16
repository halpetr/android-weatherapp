package fi.tuni.tamk.weatherapp.WeatherData

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Temp(var temp: Double = 0.0)
