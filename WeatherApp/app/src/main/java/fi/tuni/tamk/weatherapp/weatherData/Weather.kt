package fi.tuni.tamk.weatherapp.weatherData

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Weather(
    var main: String? = null,
    var description: String? = null,
    var icon: String? = null
)
