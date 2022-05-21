package fi.tuni.tamk.weatherapp.forecast_weather_data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Forecast data object
 *
 * @property city City object containing the city data
 * @property list Mutable list of ForecastItems
 * @property cod Int response code of the http request
 * @constructor Create empty Forecast data object
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ForecastDataObject(
    var city: City? = null,
    var list: MutableList<ForecastItem>? = null,
    var cod: Int = 0
)
