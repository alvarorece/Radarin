package es.uniovi.asw.radarinen3b.models

data class Friend(
    val webId: String,
    val fn: String?,
    var imgSrcUrl: String? = null,
    var location: Coords? = null,
    var distance: Int? = null
) {
    val isNear: Boolean
        get() = this.distance != null && distance!! < 2000
}
