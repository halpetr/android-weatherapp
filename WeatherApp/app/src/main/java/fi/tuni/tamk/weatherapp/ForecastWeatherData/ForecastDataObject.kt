package fi.tuni.tamk.weatherapp.ForecastWeatherData

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ForecastDataObject(
    var city: City? = null,
    var list: MutableList<ForecastItem>? = null,
    var cod: Int = 0
)
