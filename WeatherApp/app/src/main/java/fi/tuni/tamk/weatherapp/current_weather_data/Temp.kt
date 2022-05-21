package fi.tuni.tamk.weatherapp.current_weather_data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Temp data class
 *
 * @property temp Double containing the temperature value
 * @constructor Create empty Temp
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Temp(var temp: Double = 0.0)