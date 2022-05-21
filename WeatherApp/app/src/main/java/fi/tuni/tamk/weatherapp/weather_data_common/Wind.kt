package fi.tuni.tamk.weatherapp.weather_data_common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Wind data class
 *
 * @property speed Double containing the wind speed
 * @constructor Create empty Wind
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Wind(var speed: Double = 0.0)
