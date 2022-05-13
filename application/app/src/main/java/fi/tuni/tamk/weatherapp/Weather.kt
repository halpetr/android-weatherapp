package fi.tuni.tamk.weatherapp

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Weather(var coord : Any? = null, var weather: Any? = null, var main: Any? = null, var wind: Any? = null,
                   var sys: Any? = null, var dt: Int = 0, var cod: Int = 0, var name: String = "")
