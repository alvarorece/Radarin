package es.uniovi.asw.radarinen3b.models

data class Friend(
    val webId: String,
    val fn: String?,
    var location: Coords? = null,
    var distance: Int? = null
)
