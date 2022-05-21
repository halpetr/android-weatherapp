package fi.tuni.tamk.weatherapp.weather_data_common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Coord data class
 *
 * @property lon Double containing the latitude value
 * @property lat Double containing the longitude value
 * @constructor Create empty Coord
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Coord(var lon: Double = 0.0, var lat: Double = 0.0)
