package fi.tuni.tamk.weatherapp.ForecastWeatherData

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Temp(
    var temp: Double? = null,
    var temp_min: Double? = null,
    var temp_max: Double? = null
)
