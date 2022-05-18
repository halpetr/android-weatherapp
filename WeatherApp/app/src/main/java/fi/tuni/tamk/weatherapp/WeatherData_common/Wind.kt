package fi.tuni.tamk.weatherapp.WeatherData_common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Wind(var speed: Double = 0.0)
