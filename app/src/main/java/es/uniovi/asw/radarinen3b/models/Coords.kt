package es.uniovi.asw.radarinen3b.models

data class Coords(
    val accuracy: Double? = null,
    val altitude: Double? = null,
    val altitudeAccuracy: Double? = null,
    val heading: Double? = null,
    val latitude: Double,
    val longitude: Double,
    val speed: Double? = null
)
