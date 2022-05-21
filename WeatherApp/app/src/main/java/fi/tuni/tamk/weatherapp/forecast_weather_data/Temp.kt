package fi.tuni.tamk.weatherapp.forecast_weather_data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Temp data class
 *
 * @property temp Double containing the temperature value
 * @property temp_min Double containing the min temperature value
 * @property temp_max Double containing the max temperature value
 * @constructor Create empty Temp
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Temp(
    var temp: Double? = null,
    var temp_min: Double? = null,
    var temp_max: Double? = null
)
