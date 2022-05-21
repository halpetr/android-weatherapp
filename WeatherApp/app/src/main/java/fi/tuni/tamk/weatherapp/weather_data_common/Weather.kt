package fi.tuni.tamk.weatherapp.weather_data_common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Weather data class
 *
 * @property main String containing the weather conditions
 * @property description String containing the weather description
 * @property icon String containing the icon name
 * @constructor Create empty Weather
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Weather(
    var main: String? = null,
    var description: String? = null,
    var icon: String? = null
)
