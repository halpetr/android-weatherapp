package fi.tuni.tamk.weatherapp.weatherData

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Coord(var lon: Double = 0.0, var lat: Double = 0.0)
