package fi.tuni.tamk.weatherapp.forecast_weather_data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import fi.tuni.tamk.weatherapp.weather_data_common.Coord

/**
 * City data class
 *
 * @property name String containing the name of the city
 * @property coord Coord object containing the location coordinates
 * @constructor Create empty City
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class City(var name: String? = null, var coord: Coord? = null)
